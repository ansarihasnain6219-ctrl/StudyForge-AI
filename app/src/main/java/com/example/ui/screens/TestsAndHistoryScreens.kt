package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.NoteEntity
import com.example.data.local.QuestionSetEntity
import com.example.data.local.TestAttemptEntity
import com.example.data.local.UserProfilePrefs
import com.example.model.AppUiLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyTestsScreen(
    prefs: UserProfilePrefs,
    questionSets: List<QuestionSetEntity>,
    attempts: List<TestAttemptEntity>,
    onStartTest: (QuestionSetEntity) -> Unit,
    onReviewSetAnswers: (QuestionSetEntity) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onDeleteAttempt: (Long) -> Unit,
    onGoToGenerator: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isHi = prefs.uiLanguage == AppUiLanguage.HINDI
    val dateFmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

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
                    text = if (isHi) "मेरे टेस्ट और प्रदर्शन स्कोर" else "My Tests & Scorecards",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHi) "जनरेट किए गए प्रश्नपत्रों का अभ्यास करें और पिछले स्कोर ट्रैक करें"
                    else "Practice saved exam papers or review your past test performance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (questionSets.isEmpty() && attempts.isEmpty()) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Quiz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isHi) "अभी तक कोई टेस्ट नहीं बनाया गया है" else "No Practice Tests Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isHi) "अपने नोट्स अपलोड करें और अपना पहला मॉक टेस्ट या MCQ सेट जनरेट करें।"
                                else "Upload your chapter notes and generate your first MCQ set or Board Mock Test.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(onClick = onGoToGenerator) {
                                Text(if (isHi) "पहला टेस्ट बनाएं" else "Create First Test")
                            }
                        }
                    }
                }
            }

            if (questionSets.isNotEmpty()) {
                item {
                    Text(
                        text = if (isHi) "उपलब्ध प्रश्नपत्र (${questionSets.size})" else "Saved Question Papers (${questionSets.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(questionSets.size) { index ->
                    val set = questionSets[index]
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = set.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${set.questionCount} Qs • ${set.difficultyId.uppercase()} • ${dateFmt.format(Date(set.createdAt))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteSet(set.id) },
                                    modifier = Modifier.testTag("delete_set_${set.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Delete Paper",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onStartTest(set) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isHi) "टेस्ट दें" else "Take Test")
                                }
                                OutlinedButton(
                                    onClick = { onReviewSetAnswers(set) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isHi) "उत्तर कुंजी" else "Answer Key")
                                }
                            }
                        }
                    }
                }
            }

            if (attempts.isNotEmpty()) {
                item {
                    Text(
                        text = if (isHi) "पिछले प्रयास और स्कोर (${attempts.size})" else "Recent Test Attempts (${attempts.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(attempts.size) { index ->
                    val att = attempts[index]
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
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${att.percentage.toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = att.setTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Score: ${att.correctCount}/${att.totalAutoGradable} • ${dateFmt.format(Date(att.attemptedAt))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteAttempt(att.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete Attempt",
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

@Composable
fun HistoryScreen(
    prefs: UserProfilePrefs,
    notes: List<NoteEntity>,
    questionSets: List<QuestionSetEntity>,
    onUseNote: (NoteEntity) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onOpenQuestionSet: (QuestionSetEntity) -> Unit,
    onDeleteQuestionSet: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isHi = prefs.uiLanguage == AppUiLanguage.HINDI
    val dateFmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHi) "सहेजा गया इतिहास" else "Saved Notes & History",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = if (isHi) "अपने अपलोड किए गए नोट्स और जनरेटेड प्रश्नपत्रों को प्रबंधित या डिलीट करें"
                            else "View, reuse, or permanently delete your uploaded notes & generated papers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (notes.isNotEmpty() || questionSets.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onClearAllHistory,
                            modifier = Modifier.testTag("clear_all_history_button")
                        ) {
                            Text(
                                text = if (isHi) "सब हटाएं" else "Clear All",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (notes.isEmpty() && questionSets.isEmpty()) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HistoryEdu,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isHi) "आपका इतिहास खाली है" else "No Saved History Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isHi) "जब आप नोट्स अपलोड करेंगे या प्रश्न बनाएंगे, वे यहाँ सुरक्षित रहेंगे।"
                                else "Uploaded notes and generated question papers appear here for instant access.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (notes.isNotEmpty()) {
                item {
                    Text(
                        text = if (isHi) "अपलोड किए गए नोट्स (${notes.size})" else "Uploaded Study Notes (${notes.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(notes.size) { idx ->
                    val note = notes[idx]
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
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.fileName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${note.board} • Class ${note.studentClass} • ${note.subject}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dateFmt.format(Date(note.createdAt)),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(onClick = { onUseNote(note) }) {
                                Text(if (isHi) "प्रश्न बनाएं" else "Generate")
                            }
                            IconButton(onClick = { onDeleteNote(note.id) }) {
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

            if (questionSets.isNotEmpty()) {
                item {
                    Text(
                        text = if (isHi) "जनरेटेड प्रश्नपत्र (${questionSets.size})" else "Generated Question Sets (${questionSets.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(questionSets.size) { idx ->
                    val set = questionSets[idx]
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
                                    text = set.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${set.questionCount} Qs • ${set.outputLanguageId.uppercase()} • ${dateFmt.format(Date(set.createdAt))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(onClick = { onOpenQuestionSet(set) }) {
                                Text(if (isHi) "खोलें" else "Open")
                            }
                            IconButton(onClick = { onDeleteQuestionSet(set.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete Question Set",
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
