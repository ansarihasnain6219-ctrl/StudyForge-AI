package com.example.data

import android.content.Context
import com.example.data.local.NoteEntity
import com.example.data.local.QuestionSetEntity
import com.example.data.local.StudyForgeDao
import com.example.data.local.TestAttemptEntity
import com.example.data.remote.GeminiApiService
import com.example.model.AiExtractionResult
import com.example.model.AiGenerationResponse
import com.example.model.DifficultyLevel
import com.example.model.EducationBoard
import com.example.model.GeneratedQuestion
import com.example.model.OutputLanguage
import com.example.model.QuestionFormat
import com.example.util.LocalPdfTextExtractor
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class StudyForgeRepository(
    private val dao: StudyForgeDao,
    private val geminiService: GeminiApiService = GeminiApiService()
) {
    val allNotesFlow: Flow<List<NoteEntity>> = dao.observeAllNotes()
    val allQuestionSetsFlow: Flow<List<QuestionSetEntity>> = dao.observeAllQuestionSets()
    val allAttemptsFlow: Flow<List<TestAttemptEntity>> = dao.observeAllAttempts()

    fun isApiKeyConfigured(): Boolean = geminiService.isApiKeyConfigured()

    suspend fun extractNotesFromUpload(
        context: Context,
        fileBytes: ByteArray,
        mimeType: String,
        fileName: String,
        onProgress: (Float, String) -> Unit
    ): AiExtractionResult {
        val isPdf = mimeType.equals("application/pdf", ignoreCase = true) ||
            fileName.endsWith(".pdf", ignoreCase = true)

        if (isPdf) {
            val localPdfResult = LocalPdfTextExtractor.extractTextFromPdfBytes(
                context = context,
                pdfBytes = fileBytes,
                fileName = fileName,
                onProgress = onProgress
            )

            // If digital text was extracted locally, return it directly so plain text is sent to Gemini API during generation!
            if (!localPdfResult.isScannedImageOnlyPdf) {
                return AiExtractionResult(
                    extractedText = localPdfResult.extractedText,
                    detectedLanguage = localPdfResult.detectedLanguage,
                    isComplete = localPdfResult.isComplete,
                    completenessWarning = localPdfResult.completenessWarning,
                    pageOrTopicSummary = localPdfResult.summary
                )
            }

            // Scanned PDF with no embedded text layer: fall back to Gemini Vision OCR if API key is ready
            if (!geminiService.isApiKeyConfigured()) {
                throw IllegalArgumentException(
                    "PDF '$fileName' (${localPdfResult.totalPages} pages) contains scanned images with no selectable text layer. Configure GEMINI_API_KEY in AI Studio Secrets to run Vision OCR on scanned PDFs, or upload a digital text PDF."
                )
            }

            onProgress(
                0.55f,
                "Scanned PDF detected (no text layer). Running Gemini Vision OCR on '$fileName'..."
            )
        }

        return geminiService.extractNotesFromFile(
            fileBytes = fileBytes,
            mimeType = mimeType,
            fileName = fileName,
            onProgress = onProgress
        )
    }

    suspend fun saveNote(note: NoteEntity): Long = dao.insertNote(note)

    suspend fun deleteNote(noteId: Long) = dao.deleteNoteById(noteId)

    suspend fun generateAndSaveQuestionSet(
        noteId: Long?,
        notesText: String,
        board: EducationBoard,
        studentClass: Int,
        subject: String,
        chapter: String,
        format: QuestionFormat,
        questionCount: Int,
        difficulty: DifficultyLevel,
        outputLanguage: OutputLanguage
    ): Pair<QuestionSetEntity, List<GeneratedQuestion>> {
        val aiResult: AiGenerationResponse = geminiService.generateQuestionsFromNotes(
            notesText = notesText,
            board = board,
            studentClass = studentClass,
            subject = subject,
            chapter = chapter,
            format = format,
            questionCount = questionCount,
            difficulty = difficulty,
            outputLanguage = outputLanguage
        )

        val serialized = serializeQuestions(aiResult.questions)
        val setTitle = "${board.code} Class $studentClass • $chapter (${format.titleEn.substringBefore("(").trim()})"
        val entity = QuestionSetEntity(
            noteId = noteId,
            title = setTitle,
            board = board.code,
            studentClass = studentClass,
            subject = subject,
            chapter = chapter,
            questionFormatId = format.id,
            difficultyId = difficulty.id,
            outputLanguageId = outputLanguage.id,
            questionCount = aiResult.questions.size,
            questionsJson = serialized,
            incompleteWarning = aiResult.incompleteNotesWarning
        )
        val newId = dao.insertQuestionSet(entity)
        return entity.copy(id = newId) to aiResult.questions
    }

    suspend fun deleteQuestionSet(setId: Long) = dao.deleteQuestionSetById(setId)

    suspend fun saveTestAttempt(attempt: TestAttemptEntity): Long = dao.insertAttempt(attempt)

    suspend fun deleteTestAttempt(attemptId: Long) = dao.deleteAttemptById(attemptId)

    suspend fun clearAllUserHistory() {
        dao.deleteAllAttempts()
        dao.deleteAllQuestionSets()
        dao.deleteAllNotes()
    }

    companion object {
        fun serializeQuestions(questions: List<GeneratedQuestion>): String {
            val arr = JSONArray()
            questions.forEach { q ->
                val obj = JSONObject().apply {
                    put("id", q.id)
                    put("type", q.type)
                    put("questionText", q.questionText)
                    val optArr = JSONArray()
                    q.options.forEach { optArr.put(it) }
                    put("options", optArr)
                    put("correctAnswer", q.correctAnswer)
                    put("explanation", q.explanation)
                    put("sourceReference", q.sourceReference)
                    put("marks", q.marks)
                }
                arr.put(obj)
            }
            return arr.toString()
        }

        fun deserializeQuestions(jsonStr: String): List<GeneratedQuestion> {
            return try {
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<GeneratedQuestion>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val opts = mutableListOf<String>()
                    val optArr = obj.optJSONArray("options")
                    if (optArr != null) {
                        for (j in 0 until optArr.length()) {
                            opts.add(optArr.optString(j, ""))
                        }
                    }
                    list.add(
                        GeneratedQuestion(
                            id = obj.optInt("id", i + 1),
                            type = obj.optString("type", "MCQ"),
                            questionText = obj.optString("questionText", ""),
                            options = opts,
                            correctAnswer = obj.optString("correctAnswer", ""),
                            explanation = obj.optString("explanation", ""),
                            sourceReference = obj.optString("sourceReference", ""),
                            marks = obj.optInt("marks", 1)
                        )
                    )
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
