package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.model.GeneratedQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExamExporter {

    private const val PAGE_WIDTH = 595 // A4 standard points (72 dpi)
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_HORIZONTAL = 42f
    private const val MARGIN_TOP = 46f
    private const val MARGIN_BOTTOM = 52f
    private const val CONTENT_WIDTH = (PAGE_WIDTH - 2 * MARGIN_HORIZONTAL).toInt()

    suspend fun generatePrintablePdf(
        context: Context,
        title: String,
        board: String,
        studentClass: Int,
        subject: String,
        chapter: String,
        difficulty: String,
        questions: List<GeneratedQuestion>,
        includeAnswerKey: Boolean
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()

        // Load bundled Noto Sans Devanagari so both Hindi (Devanagari) and English Latin glyphs render cleanly!
        val baseTypeface = try {
            ResourcesCompat.getFont(context, R.font.noto_sans_devanagari) ?: Typeface.SANS_SERIF
        } catch (_: Exception) {
            Typeface.SANS_SERIF
        }
        val boldTypeface = Typeface.create(baseTypeface, Typeface.BOLD)

        val headerTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = 16f
            color = Color.rgb(30, 64, 175) // Royal Blue
        }
        val subHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = baseTypeface
            textSize = 10.5f
            color = Color.rgb(51, 65, 85)
        }
        val sectionTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = 13f
            color = Color.rgb(15, 23, 42)
        }
        val questionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = 11f
            color = Color.rgb(15, 23, 42)
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = baseTypeface
            textSize = 10.5f
            color = Color.rgb(30, 41, 59)
        }
        val metaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = baseTypeface
            textSize = 9f
            color = Color.rgb(100, 116, 139)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 1f
        }
        val bannerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(239, 246, 255)
        }

        var pageNumber = 1
        var currentPage = pdfDocument.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        )
        var canvas = currentPage.canvas
        var cursorY = MARGIN_TOP

        fun drawFooter(c: Canvas, pNum: Int) {
            val footerText = "StudyForge AI • Practice Paper from Student Notes (Not Official Board PYQ) • Page $pNum"
            c.drawLine(MARGIN_HORIZONTAL, PAGE_HEIGHT - 36f, PAGE_WIDTH - MARGIN_HORIZONTAL, PAGE_HEIGHT - 36f, linePaint)
            c.drawText(footerText, MARGIN_HORIZONTAL, PAGE_HEIGHT - 20f, metaPaint)
        }

        fun ensureSpace(requiredHeight: Float) {
            if (cursorY + requiredHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                drawFooter(canvas, pageNumber)
                pdfDocument.finishPage(currentPage)
                pageNumber++
                currentPage = pdfDocument.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                )
                canvas = currentPage.canvas
                cursorY = MARGIN_TOP
            }
        }

        fun drawWrappedText(text: String, paint: TextPaint, indent: Float = 0f, spacingAfter: Float = 6f) {
            if (text.isBlank()) return
            val availableWidth = (CONTENT_WIDTH - indent).toInt().coerceAtLeast(100)
            val layout = StaticLayout.Builder
                .obtain(text, 0, text.length, paint, availableWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2f, 1.12f)
                .setIncludePad(false)
                .build()

            ensureSpace(layout.height + spacingAfter)
            canvas.save()
            canvas.translate(MARGIN_HORIZONTAL + indent, cursorY)
            layout.draw(canvas)
            canvas.restore()
            cursorY += layout.height + spacingAfter
        }

        // Exam Paper Header Box
        canvas.drawRoundRect(
            MARGIN_HORIZONTAL,
            cursorY,
            PAGE_WIDTH - MARGIN_HORIZONTAL,
            cursorY + 86f,
            8f,
            8f,
            bannerBgPaint
        )
        cursorY += 20f
        canvas.drawText("STUDYFORGE AI — PRACTICE QUESTION PAPER", MARGIN_HORIZONTAL + 12f, cursorY, headerTitlePaint)
        cursorY += 18f
        val totalMarks = questions.sumOf { it.marks }
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        canvas.drawText(
            "Board: $board   |   Class: $studentClass   |   Subject: $subject",
            MARGIN_HORIZONTAL + 12f,
            cursorY,
            subHeaderPaint
        )
        cursorY += 16f
        canvas.drawText(
            "Chapter: $chapter   |   Difficulty: $difficulty   |   Max Marks: $totalMarks   |   Date: $dateStr",
            MARGIN_HORIZONTAL + 12f,
            cursorY,
            subHeaderPaint
        )
        cursorY += 16f
        canvas.drawText(
            "Note: AI-generated strictly from uploaded study notes for practice. Not an official board paper.",
            MARGIN_HORIZONTAL + 12f,
            cursorY,
            metaPaint
        )
        cursorY += 26f

        drawWrappedText(title, sectionTitlePaint, spacingAfter = 10f)
        canvas.drawLine(MARGIN_HORIZONTAL, cursorY, PAGE_WIDTH - MARGIN_HORIZONTAL, cursorY, linePaint)
        cursorY += 14f

        // Render Questions
        questions.forEachIndexed { idx, q ->
            val qPrefix = "Q${idx + 1}. ${q.questionText}  [${q.marks} ${if (q.marks == 1) "Mark" else "Marks"}]"
            drawWrappedText(qPrefix, questionPaint, indent = 0f, spacingAfter = 4f)

            if (q.options.isNotEmpty()) {
                val labels = listOf("A", "B", "C", "D", "E", "F")
                q.options.forEachIndexed { optIdx, optText ->
                    val label = labels.getOrElse(optIdx) { "${optIdx + 1}" }
                    drawWrappedText("($label) $optText", bodyPaint, indent = 14f, spacingAfter = 3f)
                }
            }

            if (q.sourceReference.isNotBlank()) {
                drawWrappedText("Source: ${q.sourceReference}", metaPaint, indent = 14f, spacingAfter = 10f)
            } else {
                cursorY += 8f
            }
        }

        // Answer Key & Explanations Section
        if (includeAnswerKey) {
            ensureSpace(120f)
            cursorY += 12f
            canvas.drawLine(MARGIN_HORIZONTAL, cursorY, PAGE_WIDTH - MARGIN_HORIZONTAL, cursorY, linePaint)
            cursorY += 18f
            drawWrappedText("ANSWER KEY & EXPLANATIONS (उत्तर कुंजी और व्याख्या)", headerTitlePaint, spacingAfter = 12f)

            questions.forEachIndexed { idx, q ->
                drawWrappedText("Q${idx + 1} Answer: ${q.correctAnswer}", questionPaint, indent = 0f, spacingAfter = 3f)
                if (q.explanation.isNotBlank()) {
                    drawWrappedText("Explanation: ${q.explanation}", bodyPaint, indent = 12f, spacingAfter = 10f)
                }
            }
        }

        drawFooter(canvas, pageNumber)
        pdfDocument.finishPage(currentPage)

        val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val safeSubject = subject.replace(Regex("[^A-Za-z0-9]"), "_").take(18)
        val suffix = if (includeAnswerKey) "With_Answers" else "Question_Paper"
        val fileName = "StudyForge_${board}_Class${studentClass}_${safeSubject}_${suffix}_${System.currentTimeMillis() % 10000}.pdf"
        val outFile = File(outputDir, fileName)

        FileOutputStream(outFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()

        outFile
    }

    fun shareOrOpenPdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, pdfFile.nameWithoutExtension)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Download / Print PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
