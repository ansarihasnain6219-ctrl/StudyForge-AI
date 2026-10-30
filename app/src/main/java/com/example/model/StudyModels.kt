package com.example.model

enum class AppUiLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिंदी")
}

enum class OutputLanguage(val id: String, val labelEn: String, val labelHi: String, val promptInstruction: String) {
    ENGLISH(
        "english",
        "English",
        "अंग्रेज़ी (English)",
        "Generate ALL questions, options, correct answers, and explanations strictly in clear academic English."
    ),
    HINDI(
        "hindi",
        "Hindi (हिंदी)",
        "हिंदी (Hindi)",
        "Generate ALL questions, options, correct answers, and explanations strictly in clear, standard academic Hindi (Devanagari script)."
    ),
    BILINGUAL(
        "bilingual",
        "Bilingual (English + हिंदी)",
        "द्विभाषी (English + हिंदी)",
        "Generate EVERY question, option, correct answer, and explanation BILINGUALLY: write the English text first followed immediately by ' / ' and the Hindi (Devanagari) translation."
    )
}

enum class EducationBoard(val code: String, val fullNameEn: String, val fullNameHi: String) {
    CBSE("CBSE", "Central Board of Secondary Education (CBSE)", "केंद्रीय माध्यमिक शिक्षा बोर्ड (CBSE)"),
    JAC("JAC", "Jharkhand Academic Council (JAC)", "झारखंड अधिविद्य परिषद (JAC)"),
    BIHAR("BSEB", "Bihar School Examination Board (Bihar Board)", "बिहार विद्यालय परीक्षा समिति (Bihar Board)"),
    UP("UPMSP", "Uttar Pradesh Madhyamik Shiksha Parishad (UP Board)", "उत्तर प्रदेश माध्यमिक शिक्षा परिषद (UP Board)"),
    ICSE("ICSE", "Indian Certificate of Secondary Education (ICSE / ISC)", "भारतीय माध्यमिक शिक्षा प्रमाणपत्र (ICSE)"),
    MAHARASHTRA("MSBSHSE", "Maharashtra State Board", "महाराष्ट्र राज्य बोर्ड"),
    RAJASTHAN("RBSE", "Board of Secondary Education Rajasthan (RBSE)", "राजस्थान माध्यमिक शिक्षा बोर्ड (RBSE)"),
    MP("MPBSE", "Madhya Pradesh Board (MPBSE)", "मध्य प्रदेश माध्यमिक शिक्षा मंडल"),
    WB("WBBSE", "West Bengal Board (WBBSE / WBCHSE)", "पश्चिम बंगाल बोर्ड"),
    OTHER("STATE", "Other Indian State Board", "अन्य राज्य बोर्ड")
}

enum class QuestionFormat(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val isInteractiveObjective: Boolean
) {
    MCQ(
        "mcq",
        "Multiple Choice Questions (MCQs)",
        "बहुविकल्पीय प्रश्न (MCQs)",
        "4 options per question with instant auto-grading",
        "4 विकल्पों वाले प्रश्न और तुरंत मूल्यांकन",
        true
    ),
    VERY_SHORT(
        "very_short",
        "Very Short Answer Questions",
        "अति लघु उत्तरीय प्रश्न",
        "1-2 sentence crisp conceptual questions (1 Mark style)",
        "1-2 वाक्यों के सटीक प्रश्न (1 अंक शैली)",
        false
    ),
    SHORT(
        "short",
        "Short Answer Questions",
        "लघु उत्तरीय प्रश्न",
        "30-50 words structured questions (2-3 Marks style)",
        "30-50 शब्दों के संक्षिप्त प्रश्न (2-3 अंक शैली)",
        false
    ),
    LONG(
        "long",
        "Long Answer Questions",
        "दीर्घ उत्तरीय प्रश्न",
        "Detailed analytical & descriptive questions (5 Marks style)",
        "विस्तृत विश्लेषणात्मक प्रश्न (5 अंक शैली)",
        false
    ),
    FILL_BLANKS(
        "fill_blanks",
        "Fill in the Blanks",
        "रिक्त स्थानों की पूर्ति करें",
        "Key terminology blanks with exact word checking",
        "मुख्य शब्दों वाले रिक्त स्थान प्रश्न",
        true
    ),
    TRUE_FALSE(
        "true_false",
        "True or False",
        "सही या गलत (True / False)",
        "Fact-verification statements strictly from notes",
        "नोट्स पर आधारित सही या गलत कथन",
        true
    ),
    REVISION(
        "revision",
        "Important Revision Questions",
        "महत्वपूर्ण रिवीजन प्रश्न",
        "High-priority definitions, formulas & core takeaways",
        "मुख्य परिभाषाएं, सूत्र और महत्वपूर्ण बिंदु",
        false
    ),
    QA_STUDY_SET(
        "qa_study_set",
        "Questions with Answers",
        "प्रश्न और उत्तर (Study Mode)",
        "Direct study flash-sheet with model answers unlocked",
        "मॉडल उत्तरों और व्याख्या के साथ अध्ययन सेट",
        false
    ),
    MOCK_TEST(
        "mock_test",
        "Full Board-Style Mock Test",
        "मॉक टेस्ट (मिश्रित प्रश्नपत्र)",
        "Balanced mix of MCQs, True/False, Blanks & Descriptive Qs",
        "बहुविकल्पीय, रिक्त स्थान और वर्णनात्मक प्रश्नों का मिश्रण",
        true
    )
}

enum class DifficultyLevel(val id: String, val labelEn: String, val labelHi: String) {
    EASY("easy", "Easy", "आसान (Easy)"),
    MEDIUM("medium", "Medium", "मध्यम (Medium)"),
    HARD("hard", "Hard", "कठिन (Hard)")
}

data class SubjectCatalogItem(
    val nameEn: String,
    val nameHi: String,
    val suggestedChapters: List<String>
)

object IndianCurriculumCatalog {
    val classes: List<Int> = (6..12).toList()

    val questionCounts: List<Int> = listOf(5, 10, 20, 30, 50)

    fun getSubjectsForClass(studentClass: Int): List<SubjectCatalogItem> {
        return if (studentClass <= 10) {
            listOf(
                SubjectCatalogItem(
                    "Science (विज्ञान)",
                    "विज्ञान (Science)",
                    listOf(
                        "Chemical Reactions and Equations",
                        "Acids, Bases and Salts",
                        "Life Processes",
                        "Light – Reflection and Refraction",
                        "Electricity & Magnetic Effects",
                        "Force and Laws of Motion",
                        "Cell – Structure and Functions",
                        "Custom Chapter from Notes"
                    )
                ),
                SubjectCatalogItem(
                    "Mathematics (गणित)",
                    "गणित (Mathematics)",
                    listOf(
                        "Real Numbers & Polynomials",
                        "Quadratic Equations",
                        "Triangles & Coordinate Geometry",
                        "Trigonometry",
                        "Statistics & Probability",
                        "Mensuration & Surface Areas",
                        "Custom Chapter from Notes"
                    )
                ),
                SubjectCatalogItem(
                    "Social Science (सामाजिक विज्ञान)",
                    "सामाजिक विज्ञान (Social Science)",
                    listOf(
                        "The Rise of Nationalism in Europe",
                        "Nationalism in India",
                        "Resources and Development",
                        "Power Sharing & Federalism",
                        "Sectors of the Indian Economy",
                        "French Revolution & Democracy",
                        "Custom Chapter from Notes"
                    )
                ),
                SubjectCatalogItem(
                    "English",
                    "अंग्रेज़ी (English)",
                    listOf(
                        "Prose & Literature Comprehension",
                        "Poetry Analysis",
                        "Grammar & Writing Skills",
                        "Custom Chapter from Notes"
                    )
                ),
                SubjectCatalogItem(
                    "Hindi (हिंदी)",
                    "हिंदी (Hindi)",
                    listOf(
                        "गद्य खंड (Prose)",
                        "काव्य खंड (Poetry)",
                        "व्याकरण (Grammar)",
                        "Custom Chapter from Notes"
                    )
                ),
                SubjectCatalogItem(
                    "Computer Applications",
                    "कंप्यूटर अनुप्रयोग",
                    listOf(
                        "Networking & Cyber Ethics",
                        "HTML & Web Fundamentals",
                        "Programming Basics",
                        "Custom Chapter from Notes"
                    )
                )
            )
        } else {
            listOf(
                SubjectCatalogItem(
                    "Physics (भौतिकी)",
                    "भौतिकी (Physics)",
                    listOf(
                        "Electrostatics & Current Electricity",
                        "Kinematics & Laws of Motion",
                        "Thermodynamics",
                        "Optics & Modern Physics",
                        "Electromagnetic Induction",
                        "Custom Chapter from Notes"
                    )
                ),
                SubjectCatalogItem(
                    "Chemistry (रसायन विज्ञान)",
                    "रसायन विज्ञान (Chemistry)",
                    listOf(
                        "Solutions & Electrochemistry",
                        "Chemical Bonding & Atomic Structure",
                        "Chemical Kinetics",
                        "Organic Chemistry – Haloalkanes & Alcohols",
                        "Coordination Compounds",
                        "Custom Chapter from Notes"
                    )
                ),
                SubjectCatalogItem(
                    "Biology (जीव विज्ञान)",
                    "जीव विज्ञान (Biology)",
                    listOf(
                        "Principles of Inheritance & Variation",
                        "Molecular Basis of Inheritance",
                        "Cell Cycle & Biomolecules",
                        "Human Physiology",
                        "Biotechnology & Ecology",
                        "Custom Chapter from Notes"
                    )
                ),
                SubjectCatalogItem(
                    "Mathematics (गणित)",
                    "गणित (Mathematics)",
                    listOf(
                        "Relations, Functions & Matrices",
                        "Continuity, Differentiability & Calculus",
                        "Vectors & 3D Geometry",
                        "Probability & Linear Programming",
                        "Custom Chapter from Notes"
                    )
                ),
                SubjectCatalogItem(
                    "History / Pol. Science (कला संकाय)",
                    "इतिहास / राजनीति विज्ञान",
                    listOf(
                        "Bricks, Beads and Bones",
                        "Constitution of India",
                        "Era of One-Party Dominance",
                        "Custom Chapter from Notes"
                    )
                ),
                SubjectCatalogItem(
                    "Accountancy / Economics (वाणिज्य)",
                    "लेखाशास्त्र / अर्थशास्त्र",
                    listOf(
                        "National Income & Macroeconomics",
                        "Partnership Accounts",
                        "Indian Economic Development",
                        "Custom Chapter from Notes"
                    )
                )
            )
        }
    }
}

data class GeneratedQuestion(
    val id: Int,
    val type: String, // "MCQ", "TRUE_FALSE", "FILL_BLANK", "VERY_SHORT", "SHORT", "LONG", "REVISION"
    val questionText: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String,
    val sourceReference: String = "",
    val marks: Int = 1
)

data class AiExtractionResult(
    val extractedText: String,
    val detectedLanguage: String,
    val isComplete: Boolean,
    val completenessWarning: String?,
    val pageOrTopicSummary: String
)

data class AiGenerationResponse(
    val questions: List<GeneratedQuestion>,
    val incompleteNotesWarning: String?,
    val sourceCoverageSummary: String
)
