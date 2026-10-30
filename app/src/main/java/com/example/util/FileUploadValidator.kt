package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ValidatedUploadFile(
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val bytes: ByteArray
)

object FileUploadValidator {
    private const val MAX_FILE_SIZE_BYTES = 18L * 1024L * 1024L // 18 MB safe limit for Gemini inline multimodal

    private val ALLOWED_MIME_TYPES = setOf(
        "application/pdf",
        "image/jpeg",
        "image/jpg",
        "image/png"
    )

    suspend fun validateAndReadUri(context: Context, uri: Uri): ValidatedUploadFile = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        var displayName = "uploaded_notes"
        var declaredSize = -1L

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIdx >= 0) {
                    displayName = cursor.getString(nameIdx) ?: displayName
                }
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                    declaredSize = cursor.getLong(sizeIdx)
                }
            }
        }

        val rawMime = contentResolver.getType(uri)?.lowercase()?.trim()
        val inferredMime = when {
            rawMime != null && rawMime in ALLOWED_MIME_TYPES -> {
                if (rawMime == "image/jpg") "image/jpeg" else rawMime
            }
            displayName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            displayName.endsWith(".jpg", ignoreCase = true) || displayName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            displayName.endsWith(".png", ignoreCase = true) -> "image/png"
            else -> throw IllegalArgumentException(
                "Unsupported file format (${rawMime ?: displayName}). Please upload a valid PDF, JPG, or PNG file."
            )
        }

        if (declaredSize > MAX_FILE_SIZE_BYTES) {
            val mb = String.format("%.1f", declaredSize / (1024.0 * 1024.0))
            throw IllegalArgumentException(
                "File '$displayName' is too large ($mb MB). Maximum allowed file size is 18 MB."
            )
        }

        val bytes = contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes()
        } ?: throw IllegalArgumentException("Unable to open '$displayName'. The file may have been moved or lacks read permission.")

        if (bytes.isEmpty()) {
            throw IllegalArgumentException("The selected file '$displayName' is empty (0 bytes). Please choose a valid notes file.")
        }

        if (bytes.size > MAX_FILE_SIZE_BYTES) {
            val mb = String.format("%.1f", bytes.size / (1024.0 * 1024.0))
            throw IllegalArgumentException(
                "File '$displayName' exceeds the 18 MB limit ($mb MB). Please compress or split the PDF/image."
            )
        }

        ValidatedUploadFile(
            fileName = displayName,
            mimeType = inferredMime,
            sizeBytes = bytes.size.toLong(),
            bytes = bytes
        )
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
