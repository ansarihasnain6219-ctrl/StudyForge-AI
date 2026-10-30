package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.StudyForgeRepository
import com.example.model.GeneratedQuestion
import com.example.model.IndianCurriculumCatalog
import com.example.util.LocalPdfTextExtractor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name curriculum catalog and bilingual question serialization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("StudyForge AI", context.getString(R.string.app_name))

        val class10Subjects = IndianCurriculumCatalog.getSubjectsForClass(10)
        val class12Subjects = IndianCurriculumCatalog.getSubjectsForClass(12)
        assertTrue(class10Subjects.isNotEmpty())
        assertTrue(class12Subjects.any { it.nameEn.contains("Physics") })

        val sampleQuestions = listOf(
            GeneratedQuestion(
                id = 1,
                type = "MCQ",
                questionText = "Which gas is flushed in chip bags to prevent rancidity? / चिप्स की थैली में कौन सी गैस भरी जाती है?",
                options = listOf("Oxygen", "Nitrogen / नाइट्रोजन", "Hydrogen", "Chlorine"),
                correctAnswer = "Nitrogen / नाइट्रोजन",
                explanation = "Nitrogen is an unreactive antioxidant gas.",
                sourceReference = "Page 3 • Redox & Rancidity",
                marks = 1
            )
        )

        val json = StudyForgeRepository.serializeQuestions(sampleQuestions)
        val restored = StudyForgeRepository.deserializeQuestions(json)
        assertEquals(1, restored.size)
        assertEquals("Nitrogen / नाइट्रोजन", restored.first().correctAnswer)
        assertEquals(4, restored.first().options.size)
    }

    @Test
    fun `verify local pdf text extraction parses multi-page pdf with page markers`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        PDFBoxResourceLoader.init(context)

        val pdfBytes = ByteArrayOutputStream().use { out ->
            PDDocument().use { doc ->
                val page1 = PDPage()
                doc.addPage(page1)
                PDPageContentStream(doc, page1).use { stream ->
                    stream.beginText()
                    stream.setFont(PDType1Font.HELVETICA_BOLD, 14f)
                    stream.newLineAtOffset(50f, 700f)
                    stream.showText("Chapter 1: Chemical Reactions and Equations")
                    stream.endText()

                    stream.beginText()
                    stream.setFont(PDType1Font.HELVETICA, 12f)
                    stream.newLineAtOffset(50f, 675f)
                    stream.showText("Magnesium ribbon burns in oxygen to form magnesium oxide (2Mg + O2 -> 2MgO).")
                    stream.endText()
                }

                val page2 = PDPage()
                doc.addPage(page2)
                PDPageContentStream(doc, page2).use { stream ->
                    stream.beginText()
                    stream.setFont(PDType1Font.HELVETICA, 12f)
                    stream.newLineAtOffset(50f, 700f)
                    stream.showText("Oxidation is the gain of oxygen and reduction is the loss of oxygen.")
                    stream.endText()
                }

                doc.save(out)
            }
            out.toByteArray()
        }

        val extractionResult = LocalPdfTextExtractor.extractTextFromPdfBytes(
            context = context,
            pdfBytes = pdfBytes,
            fileName = "Class10_Chemistry.pdf"
        )

        assertFalse(extractionResult.isScannedImageOnlyPdf)
        assertEquals(2, extractionResult.totalPages)
        assertEquals(2, extractionResult.pagesWithText)
        assertTrue(extractionResult.extractedText.contains("[Page 1]"))
        assertTrue(extractionResult.extractedText.contains("Chemical Reactions and Equations"))
        assertTrue(extractionResult.extractedText.contains("[Page 2]"))
        assertTrue(extractionResult.extractedText.contains("Oxidation is the gain of oxygen"))
    }
}
