package com.example.data.remote

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.AiExtractionResult
import com.example.model.AiGenerationResponse
import com.example.model.DifficultyLevel
import com.example.model.EducationBoard
import com.example.model.GeneratedQuestion
import com.example.model.OutputLanguage
import com.example.model.QuestionFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiService {

    companion object {
        private const val TAG = "GeminiApiService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private val CANDIDATE_MODELS = listOf(
            "gemini-2.5-flash",
            "gemini-2.0-flash"
        )
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && key != "null"
    }

    private fun requireApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isBlank() || key == "MY_GEMINI_API_KEY" || key == "null") {
            throw IllegalStateException(
                "Gemini API Key is not configured yet. Please add GEMINI_API_KEY in the Secrets panel in AI Studio to enable AI note extraction and question generation."
            )
        }
        return key
    }

    suspend fun extractNotesFromFile(
        fileBytes: ByteArray,
        mimeType: String,
        fileName: String,
        onProgress: (Float, String) -> Unit
    ): AiExtractionResult = withContext(Dispatchers.IO) {
        val apiKey = requireApiKey()
        onProgress(0.25f, "Encoding $fileName for multimodal OCR...")

        val base64Data = Base64.encodeToString(fileBytes, Base64.NO_WRAP)

        val extractionPrompt = """
            You are StudyForge AI's accurate academic OCR & document reader for Indian school students (Class 6 to 12).
            Carefully read the attached study notes file ("$fileName").
            
            STRICT RULES:
            1. Extract ALL readable text, headings, formulas, equations, bullet points, tables, and page/section markers accurately in their original language (Hindi Devanagari, English, or mixed).
            2. Preserve page numbers or section headings as [Page X] or [Section: Heading] wherever visible so questions can cite them later.
            3. Do NOT invent or add outside facts that are not in the uploaded file.
            4. Check whether the notes appear cut off, blurry, partially illegible, or very short/incomplete. If so, set "isComplete" to false and explain clearly in "completenessWarning".
            5. If the file has no readable academic text at all, return empty "extractedText" and explain why in "completenessWarning".
            
            Respond strictly as a valid JSON object with these exact keys:
            {
              "extractedText": "Full accurate extracted text from the notes...",
              "detectedLanguage": "English | Hindi | Mixed",
              "isComplete": true,
              "completenessWarning": "",
              "pageOrTopicSummary": "1-2 sentence summary of topics covered in these notes"
            }
        """.trimIndent()

        val schema = JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
                put("extractedText", JSONObject().put("type", "STRING"))
                put("detectedLanguage", JSONObject().put("type", "STRING"))
                put("isComplete", JSONObject().put("type", "BOOLEAN"))
                put("completenessWarning", JSONObject().put("type", "STRING"))
                put("pageOrTopicSummary", JSONObject().put("type", "STRING"))
            })
            put("required", JSONArray().apply {
                put("extractedText")
                put("detectedLanguage")
                put("isComplete")
                put("completenessWarning")
                put("pageOrTopicSummary")
            })
        }

        val payload = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("inline_data", JSONObject().apply {
                                put("mime_type", mimeType)
                                put("data", base64Data)
                            })
                        })
                        put(JSONObject().put("text", extractionPrompt))
                    })
                }
            ))
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("responseSchema", schema)
                put("temperature", 0.1)
            })
        }

        onProgress(0.60f, "Extracting text & equations with Gemini Vision OCR...")
        val rawResponseText = executeWithModelFallback(apiKey, payload)
        onProgress(0.90f, "Validating extracted notes...")

        val json = parseCleanJsonObject(rawResponseText)
        val extracted = json.optString("extractedText", "").trim()
        if (extracted.isBlank()) {
            val reason = json.optString("completenessWarning", "").ifBlank {
                "No readable academic text could be extracted from '$fileName'. Please upload a clearer PDF, JPG, or PNG image."
            }
            throw IllegalArgumentException(reason)
        }

        val isComplete = json.optBoolean("isComplete", true)
        val warning = json.optString("completenessWarning", "").trim().takeIf { it.isNotBlank() && !isComplete }

        AiExtractionResult(
            extractedText = extracted,
            detectedLanguage = json.optString("detectedLanguage", "English"),
            isComplete = isComplete,
            completenessWarning = warning,
            pageOrTopicSummary = json.optString("pageOrTopicSummary", "Extracted study notes")
        )
    }

    suspend fun generateQuestionsFromNotes(
        notesText: String,
        board: EducationBoard,
        studentClass: Int,
        subject: String,
        chapter: String,
        format: QuestionFormat,
        questionCount: Int,
        difficulty: DifficultyLevel,
        outputLanguage: OutputLanguage
    ): AiGenerationResponse = withContext(Dispatchers.IO) {
        val apiKey = requireApiKey()
        if (notesText.isBlank() || notesText.trim().length < 25) {
            throw IllegalArgumentException("Uploaded notes are too short (less than 25 characters). Please upload or paste fuller study notes before generating questions.")
        }

        val formatRules = when (format) {
            QuestionFormat.MCQ -> """
                Generate ONLY Multiple Choice Questions (type = "MCQ", marks = 1).
                EVERY question MUST have EXACTLY 4 distinct options in the "options" array (without leading A/B/C/D prefixes, just the option text) and "correctAnswer" MUST match one of the 4 options verbatim.
            """.trimIndent()
            QuestionFormat.VERY_SHORT -> """
                Generate ONLY Very Short Answer Questions (type = "VERY_SHORT", marks = 1).
                Set "options" to an empty array []. "correctAnswer" must be a crisp 1-2 sentence model answer strictly from the notes.
            """.trimIndent()
            QuestionFormat.SHORT -> """
                Generate ONLY Short Answer Questions (type = "SHORT", marks = 3).
                Set "options" to an empty array []. "correctAnswer" must be a clear 30-60 word structured model answer with key points from the notes.
            """.trimIndent()
            QuestionFormat.LONG -> """
                Generate ONLY Long Answer Questions (type = "LONG", marks = 5).
                Set "options" to an empty array []. "correctAnswer" must be a comprehensive, structured point-by-point answer strictly grounded in the notes.
            """.trimIndent()
            QuestionFormat.FILL_BLANKS -> """
                Generate ONLY Fill in the Blanks questions (type = "FILL_BLANK", marks = 1).
                Each "questionText" MUST contain '________' representing a single key term or short phrase from the notes.
                Provide 4 plausible word choices in "options" (including the exact missing term) OR leave "options" with 4 choices so students can either type or select, and set "correctAnswer" to the exact missing term.
            """.trimIndent()
            QuestionFormat.TRUE_FALSE -> """
                Generate ONLY True or False statements (type = "TRUE_FALSE", marks = 1).
                Set "options" to ["True", "False"] (or translated/bilingual equivalents matching outputLanguage, e.g., ["सही (True)", "गलत (False)"] if Hindi/Bilingual).
                "correctAnswer" MUST match one of the two options verbatim.
            """.trimIndent()
            QuestionFormat.REVISION -> """
                Generate ONLY High-Yield Important Revision Questions (type = "REVISION", marks = 2) focusing on core definitions, laws, formulas, dates, and key takeaways present in the notes.
                Set "options" to an empty array [].
            """.trimIndent()
            QuestionFormat.QA_STUDY_SET -> """
                Generate Questions with Detailed Model Answers (type = "SHORT", marks = 2) designed for quick chapter revision.
                Set "options" to an empty array [].
            """.trimIndent()
            QuestionFormat.MOCK_TEST -> """
                Generate a balanced Indian Board-Style Mock Test mixing:
                - ~50% Multiple Choice Questions (type = "MCQ", marks = 1, MUST have EXACTLY 4 options, correctAnswer matches one option verbatim)
                - ~20% True/False or Fill in the Blanks (type = "TRUE_FALSE" with 2 options OR "FILL_BLANK" with 4 options, marks = 1)
                - ~30% Short / Long Descriptive Questions (type = "SHORT" with marks = 3 or "LONG" with marks = 5, options = [])
            """.trimIndent()
        }

        val systemInstruction = """
            You are StudyForge AI, a rigorous, trustworthy academic assessment generator for Indian school students.
            Context Metadata provided by student:
            - Target Board Context: ${board.fullNameEn}
            - Class: Class $studentClass
            - Subject: $subject
            - Chapter / Topic: $chapter
            - Requested Difficulty: ${difficulty.labelEn}
            - Requested Question Count: $questionCount
            
            STRICT ACADEMIC INTEGRITY & GROUNDING RULES (CRITICAL):
            1. Generate questions STRICTLY AND EXCLUSIVELY from the provided STUDENT NOTES below.
            2. DO NOT invent facts, dates, formulas, definitions, or unsupported answers outside the notes.
            3. Avoid duplicate or repetitive questions. Every question must test a distinct concept or detail from the notes.
            4. If the uploaded notes do not contain enough distinct facts to responsibly create all $questionCount unique questions without inventing information, generate as many high-quality non-duplicate questions as the notes genuinely support AND populate "incompleteNotesWarning" explaining clearly that the notes had limited coverage or were incomplete for $questionCount questions.
            5. NEVER claim that generated questions are "guaranteed board exam questions" or "official previous-year questions (PYQs)".
            6. Do NOT claim verified official ${board.code} syllabus alignment unless explicitly stated inside the uploaded notes themselves; format questions in a helpful ${board.code} Class $studentClass exam style for practice only.
            7. For EVERY question, provide a clear, student-friendly "explanation" grounded in the notes, and populate "sourceReference" with the section heading, topic, or [Page X] marker from the notes (e.g. "Section: Types of Chemical Reactions" or "Page 2").
            8. LANGUAGE REQUIREMENT: ${outputLanguage.promptInstruction}
            
            QUESTION FORMAT SPECIFICATION:
            $formatRules
        """.trimIndent()

        val userPrompt = """
            === STUDENT UPLOADED NOTES START ===
            $notesText
            === STUDENT UPLOADED NOTES END ===
            
            Generate up to $questionCount ${difficulty.labelEn} difficulty questions following all grounding, non-duplication, and language instructions.
        """.trimIndent()

        val questionItemSchema = JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
                put("type", JSONObject().put("type", "STRING"))
                put("questionText", JSONObject().put("type", "STRING"))
                put("options", JSONObject().apply {
                    put("type", "ARRAY")
                    put("items", JSONObject().put("type", "STRING"))
                })
                put("correctAnswer", JSONObject().put("type", "STRING"))
                put("explanation", JSONObject().put("type", "STRING"))
                put("sourceReference", JSONObject().put("type", "STRING"))
                put("marks", JSONObject().put("type", "INTEGER"))
            })
            put("required", JSONArray().apply {
                put("type")
                put("questionText")
                put("options")
                put("correctAnswer")
                put("explanation")
                put("sourceReference")
                put("marks")
            })
        }

        val responseSchema = JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
                put("incompleteNotesWarning", JSONObject().put("type", "STRING"))
                put("sourceCoverageSummary", JSONObject().put("type", "STRING"))
                put("questions", JSONObject().apply {
                    put("type", "ARRAY")
                    put("items", questionItemSchema)
                })
            })
            put("required", JSONArray().apply {
                put("incompleteNotesWarning")
                put("sourceCoverageSummary")
                put("questions")
            })
        }

        val payload = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            })
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                }
            ))
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("responseSchema", responseSchema)
                put("temperature", 0.25)
            })
        }

        val rawResponse = executeWithModelFallback(apiKey, payload)
        val root = parseCleanJsonObject(rawResponse)
        val questionsArray = root.optJSONArray("questions") ?: JSONArray()

        val parsedQuestions = mutableListOf<GeneratedQuestion>()
        val seenNormalized = mutableSetOf<String>()

        for (i in 0 until questionsArray.length()) {
            val qObj = questionsArray.optJSONObject(i) ?: continue
            val qText = qObj.optString("questionText", "").trim()
            if (qText.isBlank()) continue

            val normKey = qText.lowercase().replace(Regex("\\s+"), " ")
            if (!seenNormalized.add(normKey)) continue // Deduplicate strictly

            val optsJson = qObj.optJSONArray("options")
            val optionsList = mutableListOf<String>()
            if (optsJson != null) {
                for (j in 0 until optsJson.length()) {
                    val opt = optsJson.optString(j, "").trim()
                    if (opt.isNotBlank()) optionsList.add(opt)
                }
            }

            parsedQuestions.add(
                GeneratedQuestion(
                    id = parsedQuestions.size + 1,
                    type = qObj.optString("type", "MCQ").uppercase(),
                    questionText = qText,
                    options = optionsList,
                    correctAnswer = qObj.optString("correctAnswer", "").trim(),
                    explanation = qObj.optString("explanation", "").trim(),
                    sourceReference = qObj.optString("sourceReference", "Uploaded Notes").trim(),
                    marks = qObj.optInt("marks", 1).coerceAtLeast(1)
                )
            )
        }

        if (parsedQuestions.isEmpty()) {
            throw IllegalStateException("Could not generate valid questions from these notes. Please check that your uploaded notes contain sufficient readable academic material.")
        }

        val warningText = root.optString("incompleteNotesWarning", "").trim().ifBlank { null }
        val coverage = root.optString("sourceCoverageSummary", "").trim()

        AiGenerationResponse(
            questions = parsedQuestions,
            incompleteNotesWarning = warningText,
            sourceCoverageSummary = coverage
        )
    }

    private fun executeWithModelFallback(apiKey: String, payload: JSONObject): String {
        var lastException: Exception? = null
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val bodyString = payload.toString()

        for (modelName in CANDIDATE_MODELS) {
            try {
                val url = "$BASE_URL/$modelName:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(bodyString.toRequestBody(mediaType))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val respBody = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        val respJson = JSONObject(respBody)
                        val candidates = respJson.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val parts = candidates.getJSONObject(0)
                                .optJSONObject("content")
                                ?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val text = parts.getJSONObject(0).optString("text", "")
                                if (text.isNotBlank()) {
                                    return text
                                }
                            }
                        }
                        throw IllegalStateException("AI returned an empty candidate response. Your notes may have triggered a content filter or lacked readable text.")
                    } else {
                        val errMsg = try {
                            JSONObject(respBody).optJSONObject("error")?.optString("message")
                                ?: "HTTP ${response.code}"
                        } catch (_: Exception) {
                            "HTTP ${response.code}: ${response.message}"
                        }
                        Log.w(TAG, "Model $modelName returned HTTP ${response.code}: $errMsg")
                        // Retry on 404 (model name unavailable) or 503; fail fast on 400/403 invalid API key
                        if (response.code == 400 && errMsg.contains("API key", ignoreCase = true)) {
                            throw IllegalStateException("Invalid Gemini API Key. Please check your GEMINI_API_KEY in the AI Studio Secrets panel.")
                        }
                        if (response.code == 404 || response.code == 503) {
                            lastException = IllegalStateException(errMsg)
                            continue
                        }
                        throw IllegalStateException("Gemini API Error: $errMsg")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calling model $modelName", e)
                lastException = e
                if (e is IllegalStateException && e.message?.contains("API Key", ignoreCase = true) == true) {
                    throw e
                }
            }
        }
        throw lastException ?: IllegalStateException("Failed to reach Gemini API. Please check your internet connection.")
    }

    private fun parseCleanJsonObject(raw: String): JSONObject {
        val trimmed = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        return JSONObject(trimmed)
    }
}
