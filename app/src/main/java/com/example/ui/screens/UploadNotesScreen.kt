package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.FilePresent
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.NoteEntity
import com.example.data.local.UserProfilePrefs
import com.example.model.AppUiLanguage
import com.example.util.FileUploadValidator
import com.example.viewmodel.UploadProcessingState

@Composable
fun UploadNotesScreen(
    prefs: UserProfilePrefs,
    uploadState: UploadProcessingState,
    activeNoteText: String,
    activeNoteTitle: String,
    activeNoteWarning: String?,
    savedNotes: List<NoteEntity>,
    onFileSelected: (Uri) -> Unit,
    onManualTextChanged: (String) -> Unit,
    onSelectSavedNote: (NoteEntity) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onLoadSampleNotes: () -> Unit,
    onClearError: () -> Unit,
    onProceedToGenerator: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val isHi = prefs.uiLanguage == AppUiLanguage.HINDI

    // Zero-permission Android Photo Picker for Scanned JPG/PNG Notes (Play Policy Compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onFileSelected(uri)
    }

    // SAF Document Picker for PDF, JPG, PNG files (Zero storage permission required)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) onFileSelected(uri)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 780.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = if (isHi) "अध्ययन नोट्स अपलोड और OCR सिस्टम" else "Upload Notes & Multimodal OCR",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHi) {
                        "PDF दस्तावेज़ों को ऑन-डिवाइस PDFBox लाइब्रेरी से तुरंत पार्स किया जाता है और स्कैन की गई छवियों (JPG/PNG) के लिए स्मार्ट OCR का उपयोग होता है।"
                    } else {
                        "Digital PDFs are parsed locally on-device via PDFBox (preserving page markers) before sending extracted text to the Gemini API; scanned JPG/PNG notes use Vision OCR."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Upload Pickers Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    documentPickerLauncher.launch(
                                        arrayOf("application/pdf", "image/jpeg", "image/png")
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pick_pdf_document_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isHi) "PDF / फ़ाइल चुनें" else "Upload PDF / File")
                            }

                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pick_scanned_image_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isHi) "स्कैन फोटो (JPG/PNG)" else "Scanned Photo")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onLoadSampleNotes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("load_sample_science_notes_button")
                        ) {
                            Text(
                                if (isHi) "कक्षा 10 विज्ञान (Chemical Reactions) सैंपल नोट्स लोड करें"
                                else "Load Sample Class 10 Science Notes (Quick Demo)"
                            )
                        }
                    }
                }
            }

            // Upload Processing Progress / Error / Success Banner
            item {
                when (uploadState) {
                    is UploadProcessingState.Processing -> {
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.FilePresent,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = uploadState.fileName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { uploadState.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${(uploadState.progress * 100).toInt()}% • ${uploadState.statusMessage}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is UploadProcessingState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.ErrorOutline,
                                        contentDescription = "Upload Error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isHi) "फ़ाइल पढ़ने में त्रुटि" else "File Validation / OCR Error",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = uploadState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(onClick = onClearError) {
                                    Text(if (isHi) "बंद करें" else "Dismiss")
                                }
                            }
                        }
                    }

                    is UploadProcessingState.Success -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Success",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${uploadState.note.fileName} (${FileUploadValidator.formatFileSize(uploadState.note.fileSizeBytes)})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = uploadState.note.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    UploadProcessingState.Idle -> Unit
                }
            }

            // Incomplete Notes Warning if detected by OCR
            if (!activeNoteWarning.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = "Incomplete Notes Warning",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeNoteWarning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Extracted / Editable Study Notes Box
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isHi) "निकाला गया नोट्स टेक्स्ट (समीक्षा या संपादित करें)"
                                else "Extracted Notes Text (Review or Paste Directly)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${activeNoteText.length} chars",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = activeNoteText,
                            onValueChange = onManualTextChanged,
                            placeholder = {
                                Text(
                                    if (isHi) "ऊपर से PDF/JPG/PNG अपलोड करें या यहाँ अपने अध्याय के नोट्स पेस्ट करें..."
                                    else "Upload a PDF/JPG/PNG above, or paste/edit your study notes directly here..."
                                )
                            },
                            minLines = 7,
                            maxLines = 14,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("extracted_notes_textfield")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onProceedToGenerator,
                            enabled = activeNoteText.trim().length >= 25,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("proceed_to_generator_button")
                        ) {
                            Text(
                                text = if (isHi) "इन नोट्स से प्रश्न बनाएं" else "Continue to Question Generator",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            // Previously Uploaded Notes List with Delete control
            if (savedNotes.isNotEmpty()) {
                item {
                    Text(
                        text = if (isHi) "सहेजे गए नोट्स (${savedNotes.size})" else "Previously Uploaded Notes (${savedNotes.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(savedNotes.size) { index ->
                    val note = savedNotes[index]
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.fileName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${note.board} • Class ${note.studentClass} • ${note.subject}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { onSelectSavedNote(note) }
                            ) {
                                Text(if (isHi) "चुनें" else "Use")
                            }
                            IconButton(
                                onClick = { onDeleteNote(note.id) },
                                modifier = Modifier.testTag("delete_note_${note.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete Note",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
