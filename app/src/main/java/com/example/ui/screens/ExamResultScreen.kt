package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfilePrefs
import com.example.model.AppUiLanguage
import com.example.model.GeneratedQuestion
import com.example.ui.components.AcademicIntegrityBanner
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ErrorRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.viewmodel.ActiveExamSession

@Composable
fun ExamResultScreen(
    prefs: UserProfilePrefs,
    session: ActiveExamSession?,
    isAnswerCorrectFn: (GeneratedQuestion, String) -> Boolean,
    isObjectiveFn: (GeneratedQuestion) -> Boolean,
    onSelectAnswer: (Int, String) -> Unit,
    onToggleDescriptiveSelfMark: (Int) -> Unit,
    onSubmitExam: () -> Unit,
    onRetakeTest: () -> Unit,
    onGenerateMore: () -> Unit,
    onExportPdf: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isHi = prefs.uiLanguage == AppUiLanguage.HINDI

    if (session == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (isHi) "कोई सक्रिय प्रश्नपत्र नहीं चुना गया है" else "No active question paper loaded",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onGenerateMore) {
                    Text(if (isHi) "प्रश्न बनाएं" else "Generate Questions")
                }
            }
        }
        return
    }

    val set = session.questionSet
    val answeredCount = session.studentAnswers.count { it.value.isNotBlank() }

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
            // Exam Paper Header Card
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
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${set.board} • Class ${set.studentClass}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "${session.questions.sumOf { it.marks }} Marks • ${session.questions.size} Qs",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${set.subject} — ${set.chapter}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isHi) "कठिनाई: ${set.difficultyId.uppercase()} | भाषा: ${set.outputLanguageId.uppercase()}"
                            else "Difficulty: ${set.difficultyId.uppercase()} | Output: ${set.outputLanguageId.uppercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        // Printable PDF Download Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onExportPdf(false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_question_pdf_button")
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isHi) "प्रश्नपत्र PDF" else "Paper PDF")
                            }
                            OutlinedButton(
                                onClick = { onExportPdf(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_answer_key_pdf_button")
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isHi) "उत्तर कुंजी PDF" else "PDF + Answers")
                            }
                        }
                    }
                }
            }

            // Incomplete Notes Warning if AI flagged limited notes coverage
            if (!set.incompleteWarning.isNullOrBlank()) {
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
                                text = set.incompleteWarning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Performance Summary Card (Shown after Submission)
            if (session.isSubmitted) {
                item {
                    val pct = session.percentage.toInt()
                    val perfMessage = when {
                        pct >= 85 -> if (isHi) "शानदार प्रदर्शन! आपकी अध्याय पर पकड़ बहुत मजबूत है।" else "Outstanding! Strong mastery of the concepts in your notes."
                        pct >= 60 -> if (isHi) "अच्छा प्रयास! गलत प्रश्नों की व्याख्या का एक बार रिवीजन करें।" else "Good progress! Review the explanations below to sharpen weak spots."
                        else -> if (isHi) "अभ्यास जारी रखें! नोट्स को दोबारा पढ़ें और रीटेक टेस्ट दें।" else "Keep practicing! Re-read the source sections and retake this test."
                    }

                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("performance_summary_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = if (isHi) "परीक्षा परिणाम और प्रदर्शन सारांश" else "Performance Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${session.scoreCorrect} / ${session.totalAutoGradable}",
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (isHi) "सही उत्तर" else "Correct Answers",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { (pct / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = perfMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onRetakeTest,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("retake_test_button")
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isHi) "रीटेक टेस्ट" else "Retake Test")
                                }
                                OutlinedButton(
                                    onClick = onGenerateMore,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("generate_more_questions_button")
                                ) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isHi) "और प्रश्न बनाएं" else "Generate More")
                                }
                            }
                        }
                    }
                }
            } else {
                // Progress tracker before submit
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHi) "उत्तर दिए गए: $answeredCount / ${session.questions.size}"
                            else "Answered: $answeredCount of ${session.questions.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearProgressIndicator(
                            progress = {
                                if (session.questions.isNotEmpty()) {
                                    answeredCount.toFloat() / session.questions.size.toFloat()
                                } else 0f
                            },
                            modifier = Modifier
                                .width(150.dp)
                                .height(8.dp)
                        )
                    }
                }
            }

            // Question Cards List
            items(session.questions.size) { index ->
                val q = session.questions[index]
                val studentAns = session.studentAnswers[q.id].orEmpty()
                val isObjective = isObjectiveFn(q)
                val isCorrect = if (isObjective) {
                    isAnswerCorrectFn(q, studentAns)
                } else {
                    q.id in session.selfEvaluatedCorrectIds
                }

                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (session.isSubmitted) {
                            if (isCorrect) SuccessGreen.copy(alpha = 0.6f) else ErrorRed.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_card_${q.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Q${index + 1} • ${q.type.replace("_", " ")}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "${q.marks} ${if (q.marks == 1) "Mark" else "Marks"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = q.questionText,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (q.sourceReference.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (isHi) "स्रोत" else "Source"}: ${q.sourceReference}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Options OR Text Response Input
                        if (q.options.isNotEmpty()) {
                            val optionLabels = listOf("A", "B", "C", "D", "E", "F")
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                q.options.forEachIndexed { optIdx, optionText ->
                                    val isSelectedOption = studentAns == optionText
                                    val isRightOption = isAnswerCorrectFn(q, optionText)

                                    val bgColor = when {
                                        session.isSubmitted && isRightOption -> SuccessGreenContainer
                                        session.isSubmitted && isSelectedOption && !isRightOption -> ErrorRedContainer
                                        isSelectedOption -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    }

                                    val borderColor = when {
                                        session.isSubmitted && isRightOption -> SuccessGreen
                                        session.isSubmitted && isSelectedOption && !isRightOption -> ErrorRed
                                        isSelectedOption -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = bgColor,
                                        border = BorderStroke(1.2.dp, borderColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable(enabled = !session.isSubmitted) {
                                                onSelectAnswer(q.id, optionText)
                                            }
                                            .testTag("q_${q.id}_option_$optIdx")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "(${optionLabels.getOrElse(optIdx) { "${optIdx + 1}" }})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = optionText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (session.isSubmitted && (isRightOption || isSelectedOption)) {
                                                    Color(0xFF0F172A)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (session.isSubmitted && isRightOption) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "Correct Option",
                                                    tint = SuccessGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else if (session.isSubmitted && isSelectedOption && !isRightOption) {
                                                Icon(
                                                    imageVector = Icons.Filled.Cancel,
                                                    contentDescription = "Wrong Option",
                                                    tint = ErrorRed,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Descriptive / Fill-in-Blank without options
                            OutlinedTextField(
                                value = studentAns,
                                onValueChange = { onSelectAnswer(q.id, it) },
                                enabled = !session.isSubmitted,
                                label = {
                                    Text(if (isHi) "अपना उत्तर यहाँ लिखें..." else "Write your practice answer...")
                                },
                                minLines = if (q.type == "LONG") 4 else 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("q_${q.id}_text_answer")
                            )
                        }

                        // Explanation & Answer Key Block after submission
                        if (session.isSubmitted) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            if (!isObjective) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleDescriptiveSelfMark(q.id) }
                                ) {
                                    Checkbox(
                                        checked = q.id in session.selfEvaluatedCorrectIds,
                                        onCheckedChange = { onToggleDescriptiveSelfMark(q.id) }
                                    )
                                    Text(
                                        text = if (isHi) "मेरा वर्णनात्मक उत्तर मॉडल उत्तर से मेल खाता है (सही चिह्नित करें)"
                                        else "My descriptive answer matches the model key points (Mark Correct)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "${if (isHi) "सही उत्तर" else "Correct Answer"}: ${q.correctAnswer}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    if (q.explanation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(
                                                imageVector = Icons.Outlined.Lightbulb,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = q.explanation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Submit Button at Bottom of Exam
            if (!session.isSubmitted) {
                item {
                    Button(
                        onClick = onSubmitExam,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("submit_exam_button")
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHi) "टेस्ट सबमिट करें और स्कोर देखें" else "Submit Test & Check Answers",
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
