package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.NoteEntity
import com.example.data.local.QuestionSetEntity
import com.example.data.local.TestAttemptEntity
import com.example.data.local.UserProfilePrefs
import com.example.model.AppUiLanguage
import com.example.model.EducationBoard
import com.example.model.OutputLanguage
import com.example.ui.components.AcademicContextSelectorCard
import com.example.ui.components.AcademicIntegrityBanner
import com.example.viewmodel.AppScreen

@Composable
fun HomeDashboardScreen(
    prefs: UserProfilePrefs,
    notesCount: Int,
    questionSets: List<QuestionSetEntity>,
    attempts: List<TestAttemptEntity>,
    activeNoteTitle: String,
    isApiKeyReady: Boolean,
    onUpdateAcademic: (EducationBoard?, Int?, String?, String?) -> Unit,
    onUpdateOutputLanguage: (OutputLanguage) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onOpenSavedSet: (QuestionSetEntity) -> Unit,
    onLoadSampleNotes: () -> Unit
) {
    val isHi = prefs.uiLanguage == AppUiLanguage.HINDI

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
            if (!isApiKeyReady) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = "API Key Notice",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHi) {
                                    "AI Studio के Secrets पैनल में अपना GEMINI_API_KEY जोड़ें ताकि नोट्स OCR और प्रश्न जनरेशन काम कर सके।"
                                } else {
                                    "Add your GEMINI_API_KEY in the AI Studio Secrets panel to enable real-time OCR extraction & AI question generation."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Hero Card with Generated StudyForge AI Illustration
            item {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_banner_card")
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_studyforge),
                            contentDescription = "StudyForge AI Hero Illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(205.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(205.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xEE0F172A),
                                            Color(0xCC1E3A8A),
                                            Color(0x551E40AF)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (prefs.isGuestMode) {
                                        if (isHi) "अतिथि मोड सक्रिय • CBSE • JAC • BSEB • UP • ICSE"
                                        else "Guest Mode Ready • CBSE • JAC • BSEB • UP • ICSE"
                                    } else {
                                        if (isHi) "नमस्ते, ${prefs.studentName}!" else "Welcome back, ${prefs.studentName}!"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isHi) "अपने नोट्स से तुरंत बोर्ड-स्तरीय प्रश्नपत्र बनाएं"
                                else "Turn Study Notes into Smart Exam Papers",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isHi) "PDF, JPG या हस्तलिखित नोट्स अपलोड करें • हिंदी, अंग्रेज़ी और द्विभाषी आउटपुट"
                                else "Upload PDF, JPG or handwritten scans • Hindi, English & Bilingual AI",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onNavigate(AppScreen.UPLOAD) },
                                    modifier = Modifier.testTag("hero_upload_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudUpload,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isHi) "नोट्स अपलोड करें" else "Upload Notes")
                                }
                                OutlinedButton(
                                    onClick = {
                                        onLoadSampleNotes()
                                        onNavigate(AppScreen.GENERATE)
                                    },
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                    modifier = Modifier.testTag("try_sample_notes_button")
                                ) {
                                    Text(
                                        text = if (isHi) "सैंपल नोट्स आज़माएं" else "Try Sample Notes",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Board, Class, Subject, Chapter & Language Selector Card
            item {
                AcademicContextSelectorCard(
                    prefs = prefs,
                    onUpdateAcademic = onUpdateAcademic,
                    onUpdateOutputLanguage = onUpdateOutputLanguage
                )
            }

            // Active Note Status Pill if loaded
            if (activeNoteTitle.isNotBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigate(AppScreen.GENERATE) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isHi) "सक्रिय नोट्स तैयार हैं" else "Active Notes Ready for AI",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = activeNoteTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go to Generator",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Dashboard Quick Action Grid (Upload Notes, Generate Questions, My Tests, History, Settings)
            item {
                Text(
                    text = if (isHi) "डैशबोर्ड सुविधाएं" else "Study Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardTileCard(
                            title = if (isHi) "नोट्स अपलोड करें" else "Upload Notes",
                            subtitle = if (isHi) "PDF, JPG, PNG और स्कैन OCR ($notesCount सहेजे गए)" else "PDF, JPG, PNG & Scan OCR ($notesCount saved)",
                            icon = Icons.Filled.CloudUpload,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { onNavigate(AppScreen.UPLOAD) },
                            modifier = Modifier.weight(1f),
                            testTag = "dash_upload_card"
                        )
                        DashboardTileCard(
                            title = if (isHi) "प्रश्न बनाएं" else "Generate Questions",
                            subtitle = if (isHi) "MCQ, लघु, दीर्घ और मॉक टेस्ट" else "9 formats • 5 to 50 Qs",
                            icon = Icons.Filled.AutoAwesome,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = { onNavigate(AppScreen.GENERATE) },
                            modifier = Modifier.weight(1f),
                            testTag = "dash_generate_card"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val avgScore = if (attempts.isNotEmpty()) {
                            "${attempts.map { it.percentage }.average().toInt()}% Avg"
                        } else {
                            if (isHi) "${questionSets.size} टेस्ट उपलब्ध" else "${questionSets.size} Sets Ready"
                        }
                        DashboardTileCard(
                            title = if (isHi) "मेरे टेस्ट (My Tests)" else "My Tests",
                            subtitle = avgScore,
                            icon = Icons.Filled.Quiz,
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            onClick = { onNavigate(AppScreen.MY_TESTS) },
                            modifier = Modifier.weight(1f),
                            testTag = "dash_tests_card"
                        )
                        DashboardTileCard(
                            title = if (isHi) "इतिहास (History)" else "Saved History",
                            subtitle = if (isHi) "${notesCount + questionSets.size} आइटम प्रबंधित करें" else "${notesCount + questionSets.size} saved items",
                            icon = Icons.Filled.History,
                            accentColor = Color(0xFF0F766E),
                            onClick = { onNavigate(AppScreen.HISTORY) },
                            modifier = Modifier.weight(1f),
                            testTag = "dash_history_card"
                        )
                    }

                    DashboardTileCard(
                        title = if (isHi) "सेटिंग्स, खाता और गोपनीयता" else "Settings, Optional Profile & Privacy",
                        subtitle = if (isHi) "भाषा, अतिथि मोड, डेटा हटाने और गोपनीयता सूचना" else "Language switcher, Guest/Account profile & data controls",
                        icon = Icons.Filled.Settings,
                        accentColor = Color(0xFF475569),
                        onClick = { onNavigate(AppScreen.SETTINGS) },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "dash_settings_card"
                    )
                }
            }

            // Recent Question Sets Preview
            if (questionSets.isNotEmpty()) {
                item {
                    Text(
                        text = if (isHi) "हाल ही में बनाए गए प्रश्नपत्र" else "Recent Generated Papers",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(questionSets.take(3).size) { idx ->
                    val set = questionSets[idx]
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSavedSet(set) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = set.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${set.questionCount} Questions • ${set.difficultyId.uppercase()} • ${set.outputLanguageId.uppercase()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Question Set",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                AcademicIntegrityBanner(isHindi = isHi)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DashboardTileCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
