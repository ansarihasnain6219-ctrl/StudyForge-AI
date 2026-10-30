package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.StudyForgeRepository
import com.example.data.local.NoteEntity
import com.example.data.local.QuestionSetEntity
import com.example.data.local.StudyForgeDatabase
import com.example.data.local.TestAttemptEntity
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.UserProfilePrefs
import com.example.model.AppUiLanguage
import com.example.model.DifficultyLevel
import com.example.model.EducationBoard
import com.example.model.GeneratedQuestion
import com.example.model.IndianCurriculumCatalog
import com.example.model.OutputLanguage
import com.example.model.QuestionFormat
import com.example.util.FileUploadValidator
import com.example.util.PdfExamExporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class AppScreen(val route: String) {
    HOME("dashboard"),
    UPLOAD("upload_notes"),
    GENERATE("generate_questions"),
    EXAM_RESULT("exam_result"),
    MY_TESTS("my_tests"),
    HISTORY("history"),
    SETTINGS("settings")
}

sealed interface UploadProcessingState {
    data object Idle : UploadProcessingState
    data class Processing(val fileName: String, val progress: Float, val statusMessage: String) : UploadProcessingState
    data class Success(val note: NoteEntity) : UploadProcessingState
    data class Error(val message: String) : UploadProcessingState
}

sealed interface GenerationUiState {
    data object Idle : GenerationUiState
    data class Generating(val statusMessage: String) : GenerationUiState
    data class Error(val message: String) : GenerationUiState
}

data class ActiveExamSession(
    val questionSet: QuestionSetEntity,
    val questions: List<GeneratedQuestion>,
    val studentAnswers: Map<Int, String> = emptyMap(),
    val selfEvaluatedCorrectIds: Set<Int> = emptySet(),
    val isSubmitted: Boolean = false,
    val scoreCorrect: Int = 0,
    val totalAutoGradable: Int = 0,
    val percentage: Float = 0f
)

class StudyForgeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "StudyForgeViewModel"
    }

    private val database = StudyForgeDatabase.getInstance(application)
    private val repository = StudyForgeRepository(database.studyForgeDao())
    private val prefsRepo = UserPreferencesRepository(application)

    val userPrefs: StateFlow<UserProfilePrefs> = prefsRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfilePrefs()
    )

    val savedNotes: StateFlow<List<NoteEntity>> = repository.allNotesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savedQuestionSets: StateFlow<List<QuestionSetEntity>> = repository.allQuestionSetsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savedAttempts: StateFlow<List<TestAttemptEntity>> = repository.allAttemptsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadProcessingState>(UploadProcessingState.Idle)
    val uploadState: StateFlow<UploadProcessingState> = _uploadState.asStateFlow()

    private val _activeNoteText = MutableStateFlow("")
    val activeNoteText: StateFlow<String> = _activeNoteText.asStateFlow()

    private val _activeNoteId = MutableStateFlow<Long?>(null)
    val activeNoteId: StateFlow<Long?> = _activeNoteId.asStateFlow()

    private val _activeNoteTitle = MutableStateFlow("")
    val activeNoteTitle: StateFlow<String> = _activeNoteTitle.asStateFlow()

    private val _activeNoteWarning = MutableStateFlow<String?>(null)
    val activeNoteWarning: StateFlow<String?> = _activeNoteWarning.asStateFlow()

    // Generator Configuration State
    private val _selectedFormat = MutableStateFlow(QuestionFormat.MCQ)
    val selectedFormat: StateFlow<QuestionFormat> = _selectedFormat.asStateFlow()

    private val _selectedCount = MutableStateFlow(5)
    val selectedCount: StateFlow<Int> = _selectedCount.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow(DifficultyLevel.MEDIUM)
    val selectedDifficulty: StateFlow<DifficultyLevel> = _selectedDifficulty.asStateFlow()

    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val generationState: StateFlow<GenerationUiState> = _generationState.asStateFlow()

    private val _activeExamSession = MutableStateFlow<ActiveExamSession?>(null)
    val activeExamSession: StateFlow<ActiveExamSession?> = _activeExamSession.asStateFlow()

    private val _toastBanner = MutableStateFlow<String?>(null)
    val toastBanner: StateFlow<String?> = _toastBanner.asStateFlow()

    fun isApiKeyReady(): Boolean = repository.isApiKeyConfigured()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun dismissToast() {
        _toastBanner.value = null
    }

    fun toggleUiLanguage() {
        viewModelScope.launch {
            val next = if (userPrefs.value.uiLanguage == AppUiLanguage.ENGLISH) {
                AppUiLanguage.HINDI
            } else {
                AppUiLanguage.ENGLISH
            }
            prefsRepo.updateUiLanguage(next)
        }
    }

    fun setUiLanguage(lang: AppUiLanguage) {
        viewModelScope.launch {
            prefsRepo.updateUiLanguage(lang)
        }
    }

    fun setOutputLanguage(lang: OutputLanguage) {
        viewModelScope.launch {
            prefsRepo.updateOutputLanguage(lang)
        }
    }

    fun updateAcademicMetadata(
        board: EducationBoard? = null,
        studentClass: Int? = null,
        subject: String? = null,
        chapter: String? = null
    ) {
        viewModelScope.launch {
            val current = userPrefs.value
            val newBoard = board ?: current.selectedBoard
            val newClass = studentClass ?: current.selectedClass
            val subjectsForClass = IndianCurriculumCatalog.getSubjectsForClass(newClass)

            val resolvedSubject = when {
                subject != null -> subject
                subjectsForClass.any { it.nameEn == current.selectedSubject } -> current.selectedSubject
                else -> subjectsForClass.first().nameEn
            }

            val matchingCatalog = subjectsForClass.find { it.nameEn == resolvedSubject }
            val resolvedChapter = when {
                chapter != null -> chapter
                subject != null && matchingCatalog != null -> matchingCatalog.suggestedChapters.first()
                studentClass != null && matchingCatalog != null -> matchingCatalog.suggestedChapters.first()
                else -> current.selectedChapter
            }

            prefsRepo.updateAcademicSelection(
                board = newBoard,
                studentClass = newClass,
                subject = resolvedSubject,
                chapter = resolvedChapter
            )
        }
    }

    fun setQuestionFormat(format: QuestionFormat) {
        _selectedFormat.value = format
    }

    fun setQuestionCount(count: Int) {
        _selectedCount.value = count
    }

    fun setDifficulty(difficulty: DifficultyLevel) {
        _selectedDifficulty.value = difficulty
    }

    fun updateActiveNoteTextManually(text: String, title: String = "Typed / Edited Study Notes") {
        _activeNoteText.value = text
        if (_activeNoteTitle.value.isBlank()) {
            _activeNoteTitle.value = title
        }
    }

    fun selectExistingNoteForGeneration(note: NoteEntity) {
        _activeNoteId.value = note.id
        _activeNoteTitle.value = note.fileName
        _activeNoteText.value = note.extractedText
        _activeNoteWarning.value = note.completenessWarning
        _uploadState.value = UploadProcessingState.Success(note)
        _currentScreen.value = AppScreen.GENERATE
    }

    fun handleFileUpload(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uploadState.value = UploadProcessingState.Processing(
                    fileName = "Validating file...",
                    progress = 0.10f,
                    statusMessage = "Validating file format & size..."
                )
                val validated = FileUploadValidator.validateAndReadUri(context, uri)

                _uploadState.value = UploadProcessingState.Processing(
                    fileName = validated.fileName,
                    progress = 0.20f,
                    statusMessage = "Reading ${validated.fileName} (${FileUploadValidator.formatFileSize(validated.sizeBytes)})..."
                )

                val extraction = repository.extractNotesFromUpload(
                    context = context,
                    fileBytes = validated.bytes,
                    mimeType = validated.mimeType,
                    fileName = validated.fileName,
                    onProgress = { prog, msg ->
                        _uploadState.value = UploadProcessingState.Processing(
                            fileName = validated.fileName,
                            progress = prog,
                            statusMessage = msg
                        )
                    }
                )

                val prefs = userPrefs.value
                val noteEntity = NoteEntity(
                    fileName = validated.fileName,
                    mimeType = validated.mimeType,
                    fileSizeBytes = validated.sizeBytes,
                    board = prefs.selectedBoard.code,
                    studentClass = prefs.selectedClass,
                    subject = prefs.selectedSubject,
                    chapter = prefs.selectedChapter,
                    extractedText = extraction.extractedText,
                    completenessWarning = extraction.completenessWarning,
                    summary = extraction.pageOrTopicSummary
                )
                val id = repository.saveNote(noteEntity)
                val saved = noteEntity.copy(id = id)

                _activeNoteId.value = saved.id
                _activeNoteTitle.value = saved.fileName
                _activeNoteText.value = saved.extractedText
                _activeNoteWarning.value = saved.completenessWarning
                _uploadState.value = UploadProcessingState.Success(saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "File upload & OCR failed", e)
                _uploadState.value = UploadProcessingState.Error(
                    e.localizedMessage ?: "Failed to extract text from the uploaded file."
                )
            }
        }
    }

    fun loadSampleIndianCurriculumNotes() {
        val sampleText = """
            [Page 1] [Section: Chemical Reactions and Equations - Class 10 Science]
            1. Chemical Reaction: Whenever a chemical change occurs, a chemical reaction has taken place. Indicators include change in state, change in colour, evolution of a gas, or change in temperature.
            Example: Magnesium ribbon burns with a dazzling white flame in air (oxygen) and changes into a white powder called magnesium oxide:
            2Mg(s) + O2(g) -> 2MgO(s)
            
            2. Law of Conservation of Mass: Mass can neither be created nor destroyed in a chemical reaction. The total mass of the elements present in the products of a chemical reaction has to be equal to the total mass of the elements present in the reactants (balanced chemical equation).
            
            [Page 2] [Section: Types of Chemical Reactions]
            A. Combination Reaction: A reaction in which a single product is formed from two or more reactants.
            Example: Quick lime (Calcium oxide, CaO) reacts vigorously with water to produce slaked lime (Calcium hydroxide) releasing a large amount of heat (Exothermic reaction):
            CaO(s) + H2O(l) -> Ca(OH)2(aq) + Heat.
            A solution of slaked lime is used for whitewashing walls. Calcium hydroxide reacts slowly with carbon dioxide in air to form a thin shiny layer of calcium carbonate (CaCO3) in 2 to 3 days.
            
            B. Decomposition Reaction: A single reactant breaks down to give simpler products.
            - Thermal Decomposition: Ferrous sulphate crystals (FeSO4.7H2O, green colour) lose water when heated and decompose to ferric oxide (Fe2O3), sulphur dioxide (SO2) and sulphur trioxide (SO3).
            - Electrolytic Decomposition: Electrolysis of water produces hydrogen and oxygen in a 2:1 volume ratio.
            - Photolytic Decomposition: Silver chloride (AgCl, white) turns grey in sunlight to form silver metal and chlorine gas: 2AgCl(s) -> 2Ag(s) + Cl2(g). Silver bromide (AgBr) behaves similarly and is used in black and white photography.
            
            [Page 3] [Section: Displacement, Double Displacement & Redox]
            C. Displacement Reaction: A more reactive element displaces a less reactive element from its salt solution.
            Example: Iron nail dipped in blue copper sulphate solution turns brownish and the blue colour fades:
            Fe(s) + CuSO4(aq) -> FeSO4(aq) + Cu(s)
            
            D. Double Displacement Reaction: Exchange of ions between reactants.
            Example: Sodium sulphate + Barium chloride -> Barium sulphate (white insoluble precipitate) + Sodium chloride:
            Na2SO4(aq) + BaCl2(aq) -> BaSO4(s) + 2NaCl(aq)
            
            E. Oxidation and Reduction (Redox):
            - Oxidation is the gain of oxygen or loss of hydrogen.
            - Reduction is the loss of oxygen or gain of hydrogen.
            - Corrosion: Rusting of iron, black coating on silver, and green coating on copper when attacked by moisture and acids.
            - Rancidity: When fats and oils are oxidised, their smell and taste change. Nitrogen gas is flushed into potato chip bags to prevent rancidity.
        """.trimIndent()

        viewModelScope.launch {
            val prefs = userPrefs.value
            val note = NoteEntity(
                fileName = "Class10_Chemical_Reactions_Notes.txt",
                mimeType = "text/plain",
                fileSizeBytes = sampleText.toByteArray().size.toLong(),
                board = prefs.selectedBoard.code,
                studentClass = 10,
                subject = "Science (विज्ञान)",
                chapter = "Chemical Reactions and Equations",
                extractedText = sampleText,
                completenessWarning = null,
                summary = "Covers balanced equations, combination, decomposition, displacement, redox reactions, corrosion, and rancidity."
            )
            val id = repository.saveNote(note)
            val saved = note.copy(id = id)
            _activeNoteId.value = saved.id
            _activeNoteTitle.value = saved.fileName
            _activeNoteText.value = saved.extractedText
            _activeNoteWarning.value = null
            _uploadState.value = UploadProcessingState.Success(saved)
            updateAcademicMetadata(
                studentClass = 10,
                subject = "Science (विज्ञान)",
                chapter = "Chemical Reactions and Equations"
            )
        }
    }

    fun clearUploadError() {
        _uploadState.value = UploadProcessingState.Idle
    }

    fun generateQuestionsNow() {
        val textToUse = _activeNoteText.value.trim()
        if (textToUse.length < 25) {
            _generationState.value = GenerationUiState.Error(
                "Please upload a PDF/JPG/PNG notes file or paste at least 25 characters of study notes first."
            )
            return
        }

        viewModelScope.launch {
            val prefs = userPrefs.value
            val format = _selectedFormat.value
            val count = _selectedCount.value
            val difficulty = _selectedDifficulty.value

            _generationState.value = GenerationUiState.Generating(
                "Generating $count ${difficulty.labelEn} questions strictly from your notes..."
            )
            try {
                val (savedSet, questions) = repository.generateAndSaveQuestionSet(
                    noteId = _activeNoteId.value,
                    notesText = textToUse,
                    board = prefs.selectedBoard,
                    studentClass = prefs.selectedClass,
                    subject = prefs.selectedSubject,
                    chapter = prefs.selectedChapter,
                    format = format,
                    questionCount = count,
                    difficulty = difficulty,
                    outputLanguage = prefs.outputLanguage
                )
                _generationState.value = GenerationUiState.Idle

                // If "Questions with Answers" study sheet was chosen, unlock answers immediately for reading; otherwise open interactive exam
                val autoSubmitted = format == QuestionFormat.QA_STUDY_SET || format == QuestionFormat.REVISION
                _activeExamSession.value = ActiveExamSession(
                    questionSet = savedSet,
                    questions = questions,
                    isSubmitted = autoSubmitted,
                    totalAutoGradable = questions.count { isObjectiveQuestion(it) }
                )
                _currentScreen.value = AppScreen.EXAM_RESULT
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Question generation failed", e)
                _generationState.value = GenerationUiState.Error(
                    e.localizedMessage ?: "Failed to generate questions. Please check your internet connection and API key."
                )
            }
        }
    }

    fun dismissGenerationError() {
        _generationState.value = GenerationUiState.Idle
    }

    fun openSavedQuestionSet(setEntity: QuestionSetEntity, startRetake: Boolean = true) {
        val questions = StudyForgeRepository.deserializeQuestions(setEntity.questionsJson)
        val isStudySheet = setEntity.questionFormatId == QuestionFormat.QA_STUDY_SET.id
        _activeExamSession.value = ActiveExamSession(
            questionSet = setEntity,
            questions = questions,
            isSubmitted = !startRetake || isStudySheet,
            totalAutoGradable = questions.count { isObjectiveQuestion(it) }
        )
        _currentScreen.value = AppScreen.EXAM_RESULT
    }

    fun selectAnswer(questionId: Int, answer: String) {
        _activeExamSession.update { session ->
            if (session == null || session.isSubmitted) session
            else session.copy(studentAnswers = session.studentAnswers + (questionId to answer))
        }
    }

    fun toggleSelfEvaluatedDescriptive(questionId: Int) {
        _activeExamSession.update { session ->
            if (session == null) null
            else {
                val updated = if (questionId in session.selfEvaluatedCorrectIds) {
                    session.selfEvaluatedCorrectIds - questionId
                } else {
                    session.selfEvaluatedCorrectIds + questionId
                }
                recalculateSessionScore(session.copy(selfEvaluatedCorrectIds = updated))
            }
        }
    }

    fun submitExam() {
        val current = _activeExamSession.value ?: return
        if (current.isSubmitted) return

        val evaluated = recalculateSessionScore(current.copy(isSubmitted = true))
        _activeExamSession.value = evaluated

        viewModelScope.launch {
            val answersJson = JSONObject().apply {
                evaluated.studentAnswers.forEach { (k, v) -> put(k.toString(), v) }
            }.toString()

            val attempt = TestAttemptEntity(
                questionSetId = evaluated.questionSet.id,
                setTitle = evaluated.questionSet.title,
                board = evaluated.questionSet.board,
                studentClass = evaluated.questionSet.studentClass,
                subject = evaluated.questionSet.subject,
                chapter = evaluated.questionSet.chapter,
                totalAutoGradable = evaluated.totalAutoGradable,
                correctCount = evaluated.scoreCorrect,
                totalQuestions = evaluated.questions.size,
                percentage = evaluated.percentage,
                answersJson = answersJson
            )
            repository.saveTestAttempt(attempt)
        }
    }

    private fun recalculateSessionScore(session: ActiveExamSession): ActiveExamSession {
        var correctCount = 0
        var gradableCount = 0

        session.questions.forEach { q ->
            if (isObjectiveQuestion(q)) {
                gradableCount++
                val studentAns = session.studentAnswers[q.id]?.trim().orEmpty()
                if (isAnswerCorrect(q, studentAns)) {
                    correctCount++
                }
            } else {
                // Descriptive question: include in score if student wrote an answer or self-checked
                gradableCount++
                if (q.id in session.selfEvaluatedCorrectIds) {
                    correctCount++
                }
            }
        }

        val pct = if (gradableCount > 0) (correctCount.toFloat() / gradableCount.toFloat()) * 100f else 0f
        return session.copy(
            scoreCorrect = correctCount,
            totalAutoGradable = gradableCount,
            percentage = pct
        )
    }

    fun isObjectiveQuestion(q: GeneratedQuestion): Boolean {
        return q.options.isNotEmpty() || q.type in setOf("MCQ", "TRUE_FALSE", "FILL_BLANK")
    }

    fun isAnswerCorrect(q: GeneratedQuestion, studentAns: String): Boolean {
        if (studentAns.isBlank()) return false
        val normStudent = studentAns.trim().lowercase()
        val normCorrect = q.correctAnswer.trim().lowercase()
        if (normStudent == normCorrect) return true
        // Support matching bilingual option prefix before " / "
        val beforeSlashStudent = normStudent.substringBefore(" / ").trim()
        val beforeSlashCorrect = normCorrect.substringBefore(" / ").trim()
        return beforeSlashStudent.isNotBlank() && beforeSlashStudent == beforeSlashCorrect
    }

    fun retakeCurrentTest() {
        _activeExamSession.update { session ->
            session?.copy(
                studentAnswers = emptyMap(),
                selfEvaluatedCorrectIds = emptySet(),
                isSubmitted = false,
                scoreCorrect = 0,
                percentage = 0f
            )
        }
    }

    fun generateMoreFromCurrentNotes() {
        _currentScreen.value = AppScreen.GENERATE
    }

    fun exportCurrentSetToPdf(context: Context, includeAnswerKey: Boolean) {
        val session = _activeExamSession.value ?: return
        viewModelScope.launch {
            try {
                _toastBanner.value = "Preparing printable A4 PDF..."
                val set = session.questionSet
                val file = PdfExamExporter.generatePrintablePdf(
                    context = context,
                    title = set.title,
                    board = set.board,
                    studentClass = set.studentClass,
                    subject = set.subject,
                    chapter = set.chapter,
                    difficulty = set.difficultyId.replaceFirstChar { it.uppercase() },
                    questions = session.questions,
                    includeAnswerKey = includeAnswerKey
                )
                _toastBanner.value = null
                PdfExamExporter.shareOrOpenPdf(context, file)
            } catch (e: Exception) {
                Log.e(TAG, "PDF export failed", e)
                _toastBanner.value = "PDF export error: ${e.localizedMessage}"
            }
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            if (_activeNoteId.value == noteId) {
                _activeNoteId.value = null
                _activeNoteText.value = ""
                _activeNoteTitle.value = ""
                _activeNoteWarning.value = null
                _uploadState.value = UploadProcessingState.Idle
            }
            _toastBanner.value = "Uploaded note deleted."
        }
    }

    fun deleteQuestionSet(setId: Long) {
        viewModelScope.launch {
            repository.deleteQuestionSet(setId)
            if (_activeExamSession.value?.questionSet?.id == setId) {
                _activeExamSession.value = null
            }
            _toastBanner.value = "Generated question set deleted."
        }
    }

    fun deleteAttempt(attemptId: Long) {
        viewModelScope.launch {
            repository.deleteTestAttempt(attemptId)
            _toastBanner.value = "Test score record deleted."
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllUserHistory()
            _activeNoteId.value = null
            _activeNoteText.value = ""
            _activeNoteTitle.value = ""
            _activeNoteWarning.value = null
            _activeExamSession.value = null
            _uploadState.value = UploadProcessingState.Idle
            _toastBanner.value = "All uploaded notes & generated history deleted."
        }
    }

    fun saveStudentProfile(name: String) {
        viewModelScope.launch {
            prefsRepo.saveStudentAccount(name)
            _toastBanner.value = if (name.isBlank()) "Switched to Guest Mode" else "Profile saved for ${name.trim()}"
        }
    }

    fun switchToGuestMode() {
        viewModelScope.launch {
            prefsRepo.switchToGuestMode()
            _toastBanner.value = "Guest Mode activated"
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setDarkTheme(enabled)
        }
    }
}
