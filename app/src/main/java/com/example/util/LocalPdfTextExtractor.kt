package com.example.util

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

data class LocalPdfExtractionResult(
    val extractedText: String,
    val totalPages: Int,
    val pagesWithText: Int,
    val detectedLanguage: String,
    val isComplete: Boolean,
    val completenessWarning: String?,
    val summary: String,
    val isScannedImageOnlyPdf: Boolean
)

object LocalPdfTextExtractor {

    @Volatile
    private var isInitialized = false

    private fun ensureInitialized(context: Context) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    PDFBoxResourceLoader.init(context.applicationContext)
                    isInitialized = true
                }
            }
        }
    }

    suspend fun extractTextFromPdfBytes(
        context: Context,
        pdfBytes: ByteArray,
        fileName: String,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): LocalPdfExtractionResult = withContext(Dispatchers.IO) {
        ensureInitialized(context)
        onProgress(0.25f, "Opening PDF '$fileName' locally with PDFBox...")

        val document: PDDocument = try {
            PDDocument.load(ByteArrayInputStream(pdfBytes))
        } catch (e: InvalidPasswordException) {
            throw IllegalArgumentException(
                "PDF '$fileName' is password-protected. Please upload an unlocked PDF document."
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Unable to parse PDF '$fileName' locally (${e.localizedMessage ?: "corrupted or invalid PDF"})."
            )
        }

        document.use { doc ->
            if (doc.isEncrypted) {
                throw IllegalArgumentException(
                    "PDF '$fileName' is encrypted and restricts text extraction. Please upload an unencrypted PDF."
                )
            }

            val totalPages = doc.numberOfPages
            if (totalPages <= 0) {
                throw IllegalArgumentException("PDF '$fileName' contains 0 pages.")
            }

            val stripper = PDFTextStripper().apply {
                sortByPosition = true
            }

            val fullTextBuilder = StringBuilder()
            var pagesWithReadableText = 0
            val emptyPageNumbers = mutableListOf<Int>()

            for (pageIndex in 1..totalPages) {
                val progressFraction = 0.25f + (0.65f * (pageIndex.toFloat() / totalPages.toFloat()))
                onProgress(
                    progressFraction,
                    "Parsing page $pageIndex of $totalPages locally..."
                )

                stripper.startPage = pageIndex
                stripper.endPage = pageIndex

                val rawPageText = stripper.getText(doc)
                    ?.replace("\r\n", "\n")
                    ?.replace(Regex("[ \\t]+"), " ")
                    ?.replace(Regex("\\n{3,}"), "\n\n")
                    ?.trim()
                    .orEmpty()

                if (rawPageText.length >= 10) {
                    pagesWithReadableText++
                    if (fullTextBuilder.isNotEmpty()) {
                        fullTextBuilder.append("\n\n")
                    }
                    fullTextBuilder.append("[Page $pageIndex]\n")
                    fullTextBuilder.append(rawPageText)
                } else {
                    emptyPageNumbers.add(pageIndex)
                }
            }

            val combinedText = fullTextBuilder.toString().trim()
            val isScannedOnly = pagesWithReadableText == 0 || combinedText.length < 25

            val detectedLang = detectScriptLanguage(combinedText)

            val warnings = mutableListOf<String>()
            if (!isScannedOnly && emptyPageNumbers.isNotEmpty()) {
                val samplePages = emptyPageNumbers.take(5).joinToString(", ")
                val suffix = if (emptyPageNumbers.size > 5) "..." else ""
                warnings.add(
                    "${emptyPageNumbers.size} of $totalPages page(s) (Page $samplePages$suffix) contained little or no selectable text (may be diagrams or scanned images)."
                )
            }
            if (!isScannedOnly && combinedText.length < 120) {
                warnings.add(
                    "Extracted PDF text is very brief (${combinedText.length} chars). Larger question counts may have limited coverage."
                )
            }

            val summary = if (isScannedOnly) {
                "No embedded digital text found across $totalPages page(s) — scanned image PDF detected."
            } else {
                "Locally parsed $pagesWithReadableText/$totalPages page(s) (${combinedText.length} characters, $detectedLang) via PDFBox."
            }

            onProgress(0.95f, "Local PDF parsing complete.")

            LocalPdfExtractionResult(
                extractedText = combinedText,
                totalPages = totalPages,
                pagesWithText = pagesWithReadableText,
                detectedLanguage = detectedLang,
                isComplete = warnings.isEmpty() && !isScannedOnly,
                completenessWarning = warnings.joinToString(" ").ifBlank { null },
                summary = summary,
                isScannedImageOnlyPdf = isScannedOnly
            )
        }
    }

    private fun detectScriptLanguage(text: String): String {
        if (text.isBlank()) return "Unknown"
        var devanagariCount = 0
        var latinCount = 0
        for (ch in text) {
            when (ch.code) {
                in 0x0900..0x097F -> devanagariCount++
                in 'A'.code..'Z'.code, in 'a'.code..'z'.code -> latinCount++
            }
        }
        return when {
            devanagariCount > 20 && latinCount > 20 -> "Mixed (English + Hindi)"
            devanagariCount > latinCount -> "Hindi"
            else -> "English"
        }
    }
}
