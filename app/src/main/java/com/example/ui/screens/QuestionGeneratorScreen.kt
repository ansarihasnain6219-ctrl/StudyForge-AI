package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfilePrefs
import com.example.model.AppUiLanguage
import com.example.model.DifficultyLevel
import com.example.model.EducationBoard
import com.example.model.IndianCurriculumCatalog
import com.example.model.OutputLanguage
import com.example.model.QuestionFormat
import com.example.ui.components.AcademicContextSelectorCard
import com.example.ui.components.AcademicIntegrityBanner
import com.example.viewmodel.GenerationUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionGeneratorScreen(
    prefs: UserProfilePrefs,
    activeNoteTitle: String,
    activeNoteText: String,
    selectedFormat: QuestionFormat,
    selectedCount: Int,
    selectedDifficulty: DifficultyLevel,
    generationState: GenerationUiState,
    onSelectFormat: (QuestionFormat) -> Unit,
    onSelectCount: (Int) -> Unit,
    onSelectDifficulty: (DifficultyLevel) -> Unit,
    onUpdateAcademic: (EducationBoard?, Int?, String?, String?) -> Unit,
    onUpdateOutputLanguage: (OutputLanguage) -> Unit,
    onGoToUpload: () -> Unit,
    onLoadSampleNotes: () -> Unit,
    onGenerateClick: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isHi = prefs.uiLanguage == AppUiLanguage.HINDI
    val isGenerating = generationState is GenerationUiState.Generating

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
                    text = if (isHi) "एआई प्रश्न जनरेटर" else "AI Question Generator",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHi) "प्रश्न का प्रकार, संख्या (5–50) और कठिनाई स्तर चुनें"
                    else "Configure question format, count (5–50), difficulty & bilingual preferences",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Source Notes Check Banner
            item {
                if (activeNoteText.trim().length < 25) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isHi) "कोई सक्रिय नोट्स लोड नहीं हैं" else "No Study Notes Loaded Yet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isHi) "कृपया पहले अपने PDF/JPG/PNG नोट्स अपलोड करें या डेमो के लिए सैंपल नोट्स लोड करें।"
                                else "Upload your PDF/JPG/PNG notes first, or load Class 10 Science sample notes to test immediately.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = onGoToUpload) {
                                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isHi) "नोट्स अपलोड करें" else "Upload Notes")
                                }
                                OutlinedButton(onClick = onLoadSampleNotes) {
                                    Text(if (isHi) "सैंपल नोट्स" else "Load Sample")
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHi) "स्रोत नोट्स:" else "Source Notes Grounding:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = activeNoteTitle.ifBlank { "Custom Study Notes" },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "${activeNoteText.length} characters ready",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            OutlinedButton(onClick = onGoToUpload) {
                                Text(if (isHi) "बदलें / देखें" else "Edit Notes")
                            }
                        }
                    }
                }
            }

            // Board, Class, Subject, Chapter & Language Context
            item {
                AcademicContextSelectorCard(
                    prefs = prefs,
                    onUpdateAcademic = onUpdateAcademic,
                    onUpdateOutputLanguage = onUpdateOutputLanguage
                )
            }

            // Question Count & Difficulty Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isHi) "प्रश्नों की संख्या (Question Count)" else "Number of Questions",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IndianCurriculumCatalog.questionCounts.forEach { count ->
                                FilterChip(
                                    selected = selectedCount == count,
                                    onClick = { onSelectCount(count) },
                                    label = {
                                        Text(
                                            text = if (isHi) "$count प्रश्न" else "$count Qs",
                                            fontWeight = if (selectedCount == count) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("count_chip_$count")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isHi) "कठिनाई स्तर (Difficulty)" else "Difficulty Level",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DifficultyLevel.entries.forEach { diff ->
                                val selected = selectedDifficulty == diff
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectDifficulty(diff) },
                                    label = {
                                        Text(
                                            text = if (isHi) diff.labelHi else diff.labelEn,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("difficulty_chip_${diff.id}")
                                )
                            }
                        }
                    }
                }
            }

            // All 9 Question Formats Selector
            item {
                Text(
                    text = if (isHi) "प्रश्न प्रारूप चुनें (9 विकल्प)" else "Select Question Format (9 Modes)",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(QuestionFormat.entries.size) { index ->
                val format = QuestionFormat.entries[index]
                val isSelected = selectedFormat == format
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(enabled = !isGenerating) { onSelectFormat(format) }
                        .testTag("format_card_${format.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${index + 1}. ${if (isHi) format.titleHi else format.titleEn}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHi) format.descriptionHi else format.descriptionEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Error State
            if (generationState is GenerationUiState.Error) {
                item {
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
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isHi) "जनरेशन में समस्या आई" else "Generation Error",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = generationState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = onDismissError) {
                                Text(if (isHi) "ठीक है" else "Dismiss")
                            }
                        }
                    }
                }
            }

            // Generate CTA Button
            item {
                Button(
                    onClick = onGenerateClick,
                    enabled = !isGenerating && activeNoteText.trim().length >= 25,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_questions_cta_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isHi) "Gemini AI द्वारा प्रश्न बनाए जा रहे हैं..."
                            else (generationState as GenerationUiState.Generating).statusMessage
                        )
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHi) "अभी प्रश्नपत्र जनरेट करें ($selectedCount प्रश्न)"
                            else "Generate $selectedCount Questions with Gemini AI",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            item {
                AcademicIntegrityBanner(isHindi = isHi)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
