package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppUiLanguage
import com.example.ui.components.StudyForgeTopBar
import com.example.ui.screens.ExamResultScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MyTestsScreen
import com.example.ui.screens.QuestionGeneratorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UploadNotesScreen
import com.example.ui.theme.StudyForgeTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.StudyForgeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyForgeApp()
        }
    }
}

private data class NavDestination(
    val screen: AppScreen,
    val labelEn: String,
    val labelHi: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val tag: String
)

@Composable
fun StudyForgeApp(viewModel: StudyForgeViewModel = viewModel()) {
    val prefs by viewModel.userPrefs.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val notes by viewModel.savedNotes.collectAsStateWithLifecycle()
    val questionSets by viewModel.savedQuestionSets.collectAsStateWithLifecycle()
    val attempts by viewModel.savedAttempts.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()
    val activeNoteText by viewModel.activeNoteText.collectAsStateWithLifecycle()
    val activeNoteTitle by viewModel.activeNoteTitle.collectAsStateWithLifecycle()
    val activeNoteWarning by viewModel.activeNoteWarning.collectAsStateWithLifecycle()
    val selectedFormat by viewModel.selectedFormat.collectAsStateWithLifecycle()
    val selectedCount by viewModel.selectedCount.collectAsStateWithLifecycle()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsStateWithLifecycle()
    val generationState by viewModel.generationState.collectAsStateWithLifecycle()
    val examSession by viewModel.activeExamSession.collectAsStateWithLifecycle()
    val toastBanner by viewModel.toastBanner.collectAsStateWithLifecycle()

    val isHindi = prefs.uiLanguage == AppUiLanguage.HINDI
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastBanner) {
        val msg = toastBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissToast()
        }
    }

    val navItems = remember {
        listOf(
            NavDestination(AppScreen.HOME, "Home", "होम", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            NavDestination(AppScreen.UPLOAD, "Upload", "अपलोड", Icons.Filled.CloudUpload, Icons.Outlined.CloudUpload, "nav_upload"),
            NavDestination(AppScreen.GENERATE, "Generate", "प्रश्न", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_generate"),
            NavDestination(AppScreen.MY_TESTS, "My Tests", "टेस्ट", Icons.Filled.Quiz, Icons.Outlined.Quiz, "nav_tests"),
            NavDestination(AppScreen.SETTINGS, "Settings", "सेटिंग्स", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
        )
    }

    StudyForgeTheme(
        darkTheme = prefs.darkThemeEnabled,
        isHindiLanguage = isHindi
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 720.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    StudyForgeTopBar(
                        prefs = prefs,
                        onToggleLanguage = { viewModel.toggleUiLanguage() },
                        onProfileClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar {
                            navItems.forEach { dest ->
                                val selected = currentScreen == dest.screen ||
                                    (dest.screen == AppScreen.MY_TESTS && currentScreen == AppScreen.EXAM_RESULT) ||
                                    (dest.screen == AppScreen.HOME && currentScreen == AppScreen.HISTORY)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.navigateTo(dest.screen) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.filledIcon else dest.outlinedIcon,
                                            contentDescription = dest.labelEn
                                        )
                                    },
                                    label = { Text(if (isHindi) dest.labelHi else dest.labelEn) },
                                    modifier = Modifier.testTag(dest.tag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        NavigationRail {
                            navItems.forEach { dest ->
                                val selected = currentScreen == dest.screen ||
                                    (dest.screen == AppScreen.MY_TESTS && currentScreen == AppScreen.EXAM_RESULT)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.navigateTo(dest.screen) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.filledIcon else dest.outlinedIcon,
                                            contentDescription = dest.labelEn
                                        )
                                    },
                                    label = { Text(if (isHindi) dest.labelHi else dest.labelEn) },
                                    modifier = Modifier.testTag("${dest.tag}_rail")
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (currentScreen) {
                            AppScreen.HOME -> HomeDashboardScreen(
                                prefs = prefs,
                                notesCount = notes.size,
                                questionSets = questionSets,
                                attempts = attempts,
                                activeNoteTitle = activeNoteTitle,
                                isApiKeyReady = viewModel.isApiKeyReady(),
                                onUpdateAcademic = { b, c, s, ch -> viewModel.updateAcademicMetadata(b, c, s, ch) },
                                onUpdateOutputLanguage = { viewModel.setOutputLanguage(it) },
                                onNavigate = { viewModel.navigateTo(it) },
                                onOpenSavedSet = { viewModel.openSavedQuestionSet(it, startRetake = true) },
                                onLoadSampleNotes = { viewModel.loadSampleIndianCurriculumNotes() }
                            )

                            AppScreen.UPLOAD -> UploadNotesScreen(
                                prefs = prefs,
                                uploadState = uploadState,
                                activeNoteText = activeNoteText,
                                activeNoteTitle = activeNoteTitle,
                                activeNoteWarning = activeNoteWarning,
                                savedNotes = notes,
                                onFileSelected = { uri -> viewModel.handleFileUpload(context, uri) },
                                onManualTextChanged = { viewModel.updateActiveNoteTextManually(it) },
                                onSelectSavedNote = { viewModel.selectExistingNoteForGeneration(it) },
                                onDeleteNote = { viewModel.deleteNote(it) },
                                onLoadSampleNotes = { viewModel.loadSampleIndianCurriculumNotes() },
                                onClearError = { viewModel.clearUploadError() },
                                onProceedToGenerator = { viewModel.navigateTo(AppScreen.GENERATE) },
                                onBack = { viewModel.navigateTo(AppScreen.HOME) }
                            )

                            AppScreen.GENERATE -> QuestionGeneratorScreen(
                                prefs = prefs,
                                activeNoteTitle = activeNoteTitle,
                                activeNoteText = activeNoteText,
                                selectedFormat = selectedFormat,
                                selectedCount = selectedCount,
                                selectedDifficulty = selectedDifficulty,
                                generationState = generationState,
                                onSelectFormat = { viewModel.setQuestionFormat(it) },
                                onSelectCount = { viewModel.setQuestionCount(it) },
                                onSelectDifficulty = { viewModel.setDifficulty(it) },
                                onUpdateAcademic = { b, c, s, ch -> viewModel.updateAcademicMetadata(b, c, s, ch) },
                                onUpdateOutputLanguage = { viewModel.setOutputLanguage(it) },
                                onGoToUpload = { viewModel.navigateTo(AppScreen.UPLOAD) },
                                onLoadSampleNotes = { viewModel.loadSampleIndianCurriculumNotes() },
                                onGenerateClick = { viewModel.generateQuestionsNow() },
                                onDismissError = { viewModel.dismissGenerationError() },
                                onBack = { viewModel.navigateTo(AppScreen.HOME) }
                            )

                            AppScreen.EXAM_RESULT -> ExamResultScreen(
                                prefs = prefs,
                                session = examSession,
                                isAnswerCorrectFn = { q, ans -> viewModel.isAnswerCorrect(q, ans) },
                                isObjectiveFn = { q -> viewModel.isObjectiveQuestion(q) },
                                onSelectAnswer = { qId, ans -> viewModel.selectAnswer(qId, ans) },
                                onToggleDescriptiveSelfMark = { qId -> viewModel.toggleSelfEvaluatedDescriptive(qId) },
                                onSubmitExam = { viewModel.submitExam() },
                                onRetakeTest = { viewModel.retakeCurrentTest() },
                                onGenerateMore = { viewModel.generateMoreFromCurrentNotes() },
                                onExportPdf = { withAnswers -> viewModel.exportCurrentSetToPdf(context, withAnswers) },
                                onBack = { viewModel.navigateTo(AppScreen.MY_TESTS) }
                            )

                            AppScreen.MY_TESTS -> MyTestsScreen(
                                prefs = prefs,
                                questionSets = questionSets,
                                attempts = attempts,
                                onStartTest = { viewModel.openSavedQuestionSet(it, startRetake = true) },
                                onReviewSetAnswers = { viewModel.openSavedQuestionSet(it, startRetake = false) },
                                onDeleteSet = { viewModel.deleteQuestionSet(it) },
                                onDeleteAttempt = { viewModel.deleteAttempt(it) },
                                onGoToGenerator = { viewModel.navigateTo(AppScreen.GENERATE) },
                                onBack = { viewModel.navigateTo(AppScreen.HOME) }
                            )

                            AppScreen.HISTORY -> HistoryScreen(
                                prefs = prefs,
                                notes = notes,
                                questionSets = questionSets,
                                onUseNote = { viewModel.selectExistingNoteForGeneration(it) },
                                onDeleteNote = { viewModel.deleteNote(it) },
                                onOpenQuestionSet = { viewModel.openSavedQuestionSet(it, startRetake = true) },
                                onDeleteQuestionSet = { viewModel.deleteQuestionSet(it) },
                                onClearAllHistory = { viewModel.clearAllData() },
                                onBack = { viewModel.navigateTo(AppScreen.HOME) }
                            )

                            AppScreen.SETTINGS -> SettingsScreen(
                                prefs = prefs,
                                onSetUiLanguage = { viewModel.setUiLanguage(it) },
                                onSetOutputLanguage = { viewModel.setOutputLanguage(it) },
                                onSaveProfile = { viewModel.saveStudentProfile(it) },
                                onSwitchToGuest = { viewModel.switchToGuestMode() },
                                onToggleDarkTheme = { viewModel.toggleDarkTheme(it) },
                                onClearAllData = { viewModel.clearAllData() },
                                onBack = { viewModel.navigateTo(AppScreen.HOME) }
                            )
                        }
                    }
                }
            }
        }
    }
}
