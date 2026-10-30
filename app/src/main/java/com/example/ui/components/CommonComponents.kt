package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfilePrefs
import com.example.model.AppUiLanguage
import com.example.model.EducationBoard
import com.example.model.IndianCurriculumCatalog
import com.example.model.OutputLanguage

@Composable
fun StudyForgeTopBar(
    prefs: UserProfilePrefs,
    onToggleLanguage: () -> Unit,
    onProfileClick: () -> Unit
) {
    val isHi = prefs.uiLanguage == AppUiLanguage.HINDI
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "StudyForge AI Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "StudyForge AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isHi) "नोट्स से स्मार्ट प्रश्न जनरेटर" else "Indian Boards AI Study Studio",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language Switcher Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onToggleLanguage() }
                        .testTag("language_switcher_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = "Switch Language",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHi) "हिंदी | EN" else "EN | हिंदी",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Guest / Student Profile Badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onProfileClick() }
                        .testTag("profile_badge_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (prefs.isGuestMode || prefs.studentName.isBlank()) {
                            Icon(
                                imageVector = Icons.Outlined.PersonOutline,
                                contentDescription = "Guest Profile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = prefs.studentName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcademicContextSelectorCard(
    prefs: UserProfilePrefs,
    onUpdateAcademic: (EducationBoard?, Int?, String?, String?) -> Unit,
    onUpdateOutputLanguage: (OutputLanguage) -> Unit
) {
    val isHi = prefs.uiLanguage == AppUiLanguage.HINDI
    val subjects = IndianCurriculumCatalog.getSubjectsForClass(prefs.selectedClass)
    val currentSubjectObj = subjects.find { it.nameEn == prefs.selectedSubject } ?: subjects.first()
    var customChapterMode by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("academic_selector_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isHi) "बोर्ड, कक्षा और विषय चुनें" else "Board, Class & Subject Context",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${prefs.selectedBoard.code} • Class ${prefs.selectedClass}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 1: Board + Class
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SelectorDropdownBox(
                    label = if (isHi) "शिक्षा बोर्ड (Board)" else "Education Board",
                    selectedText = prefs.selectedBoard.code,
                    items = EducationBoard.entries,
                    itemLabel = { if (isHi) it.fullNameHi else it.fullNameEn },
                    onSelect = { onUpdateAcademic(it, null, null, null) },
                    modifier = Modifier.weight(1.3f),
                    testTag = "board_selector_dropdown"
                )

                SelectorDropdownBox(
                    label = if (isHi) "कक्षा (Class)" else "Class",
                    selectedText = if (isHi) "कक्षा ${prefs.selectedClass}" else "Class ${prefs.selectedClass}",
                    items = IndianCurriculumCatalog.classes,
                    itemLabel = { if (isHi) "कक्षा $it" else "Class $it" },
                    onSelect = { onUpdateAcademic(null, it, null, null) },
                    modifier = Modifier.weight(0.9f),
                    testTag = "class_selector_dropdown"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Subject + Output Language
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SelectorDropdownBox(
                    label = if (isHi) "विषय (Subject)" else "Subject",
                    selectedText = if (isHi) currentSubjectObj.nameHi else currentSubjectObj.nameEn,
                    items = subjects,
                    itemLabel = { if (isHi) it.nameHi else it.nameEn },
                    onSelect = { onUpdateAcademic(null, null, it.nameEn, null) },
                    modifier = Modifier.weight(1.1f),
                    testTag = "subject_selector_dropdown"
                )

                SelectorDropdownBox(
                    label = if (isHi) "प्रश्न भाषा (Output)" else "Question Language",
                    selectedText = if (isHi) prefs.outputLanguage.labelHi else prefs.outputLanguage.labelEn,
                    items = OutputLanguage.entries,
                    itemLabel = { if (isHi) it.labelHi else it.labelEn },
                    onSelect = { onUpdateOutputLanguage(it) },
                    modifier = Modifier.weight(1.1f),
                    testTag = "output_language_dropdown"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Chapter selector or custom chapter field
            if (!customChapterMode) {
                SelectorDropdownBox(
                    label = if (isHi) "अध्याय / टॉपिक (Chapter)" else "Chapter / Topic",
                    selectedText = prefs.selectedChapter,
                    items = currentSubjectObj.suggestedChapters,
                    itemLabel = { it },
                    onSelect = { chosen ->
                        if (chosen == "Custom Chapter from Notes") {
                            customChapterMode = true
                        } else {
                            onUpdateAcademic(null, null, null, chosen)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "chapter_selector_dropdown"
                )
            } else {
                OutlinedTextField(
                    value = prefs.selectedChapter,
                    onValueChange = { onUpdateAcademic(null, null, null, it) },
                    label = { Text(if (isHi) "अध्याय का नाम लिखें" else "Enter Chapter / Topic Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_chapter_input"),
                    trailingIcon = {
                        Text(
                            text = if (isHi) "सूची" else "List",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { customChapterMode = false }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun <T> SelectorDropdownBox(
    label: String,
    selectedText: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .testTag(testTag)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Expand $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemLabel(item),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(item)
                    }
                )
            }
        }
    }
}

@Composable
fun AcademicIntegrityBanner(isHindi: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.VerifiedUser,
                contentDescription = "Academic Honesty Notice",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isHindi) {
                    "सख्त नोट्स-आधारित एआई: प्रश्न केवल आपके अपलोड किए गए नोट्स से अभ्यास के लिए बनाए जाते हैं। ये गारंटीड बोर्ड प्रश्न या आधिकारिक पिछले वर्ष के प्रश्नपत्र (PYQ) नहीं हैं।"
                } else {
                    "Strictly Grounded AI: Questions are generated exclusively from your uploaded notes for practice. Never claimed as guaranteed board exam questions or official PYQs."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
