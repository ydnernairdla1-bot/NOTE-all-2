package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.ui.utils.formatFullTimestamp
import com.example.ui.utils.formatRupiah
import com.example.ui.utils.formatTransactionDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min

data class PdfExportResult(
    val noteCount: Int,
    val transactionCount: Int,
    val activityCount: Int,
    val imageCount: Int,
    val pageCount: Int,
    val timestamp: Long
)

class PdfExportManager(
    private val context: Context,
    private val imageStorageManager: ImageStorageManager
) {
    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN_H = 38f
        const val MARGIN_TOP = 42f
        const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_H * 2f)
        const val CONTENT_BOTTOM = 790f
        const val FOOTER_Y = 816f

        val COLOR_PRIMARY = Color.parseColor("#15504A")
        val COLOR_PRIMARY_SOFT = Color.parseColor("#E6F2F0")
        val COLOR_ACCENT = Color.parseColor("#D97706")
        val COLOR_INK = Color.parseColor("#1C1F1E")
        val COLOR_MUTED = Color.parseColor("#5A6360")
        val COLOR_BORDER = Color.parseColor("#D7CFC2")
        val COLOR_SURFACE_TINT = Color.parseColor("#F7F4EE")
        val COLOR_EXPENSE = Color.parseColor("#BA1A1A")
        val COLOR_EXPENSE_SOFT = Color.parseColor("#FDECEC")
    }

    suspend fun exportAllToPdf(
        destUri: Uri,
        notes: List<NoteEntity>,
        transactions: List<TransactionEntity>,
        activities: List<ActivityEntity>
    ): PdfExportResult = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val exportDateLabel = formatFullTimestamp(timestamp)
        val writer = PdfPageWriter(exportDateLabel)
        var exportedImages = 0

        try {
            writer.drawCoverHeader(
                title = "Note All",
                subtitle = "Ekspor Lengkap Catatan, Keuangan & Aktivitas Harian",
                dateLabel = "Diekspor: $exportDateLabel"
            )

            // Summary metrics box
            val totalIncome = transactions
                .filter { it.transactionType == TransactionType.INCOME }
                .sumOf { it.amount }
            val totalExpense = transactions
                .filter { it.transactionType == TransactionType.EXPENSE }
                .sumOf { it.amount }
            val netBalance = totalIncome - totalExpense

            writer.drawSummaryBox(
                listOf(
                    "Catatan" to "${notes.size} catatan",
                    "Transaksi" to "${transactions.size} catatan",
                    "Aktivitas" to "${activities.size} rutinitas",
                    "Saldo Bersih" to formatRupiah(netBalance)
                )
            )

            // Section 1: Catatan
            writer.drawSectionHeader("1. Catatan (${notes.size})")
            if (notes.isEmpty()) {
                writer.drawMutedLine("Belum ada catatan tersimpan.")
            } else {
                notes.forEachIndexed { index, note ->
                    if (index > 0) writer.drawItemDivider()
                    writer.ensureSpace(48f)

                    val noteTitle = note.title.ifBlank { "Catatan Tanpa Judul" }
                    val pinTag = if (note.isPinned) " • Disematkan" else ""
                    writer.drawItemTitle(noteTitle)
                    writer.drawMetaLine("${note.category} • ${formatFullTimestamp(note.updatedAt)}$pinTag")

                    if (note.content.isNotBlank()) {
                        writer.drawParagraph(note.content, writer.bodyPaint)
                        writer.advance(4f)
                    }

                    val noteImages = note.imageFileNames()
                    if (noteImages.isNotEmpty()) {
                        exportedImages += writer.drawImagesGrid(noteImages)
                    }
                }
            }

            // Section 2: Keuangan
            writer.advance(10f)
            writer.drawSectionHeader("2. Keuangan (${transactions.size})")
            if (transactions.isEmpty()) {
                writer.drawMutedLine("Belum ada transaksi keuangan.")
            } else {
                writer.drawSummaryBox(
                    listOf(
                        "Pemasukan" to formatRupiah(totalIncome),
                        "Pengeluaran" to formatRupiah(totalExpense),
                        "Saldo" to formatRupiah(netBalance)
                    )
                )
                transactions.forEachIndexed { index, tx ->
                    if (index > 0) writer.drawItemDivider()
                    writer.ensureSpace(44f)
                    val isIncome = tx.transactionType == TransactionType.INCOME
                    val amountText = formatRupiah(tx.amount, showSign = true, isIncome = isIncome)
                    writer.drawTransactionRow(
                        title = tx.title.ifBlank { tx.category },
                        meta = "${tx.category} • ${formatTransactionDate(tx.transactionDate)}",
                        amountText = amountText,
                        isIncome = isIncome
                    )
                    if (tx.note.isNotBlank()) {
                        writer.drawParagraph(tx.note, writer.captionPaint)
                    }
                    tx.receiptImageName?.let { receipt ->
                        exportedImages += writer.drawImagesGrid(listOf(receipt), maxImageWidth = 180f, maxImageHeight = 140f)
                    }
                }
            }

            // Section 3: Aktivitas Harian
            writer.advance(10f)
            writer.drawSectionHeader("3. Aktivitas Harian (${activities.size})")
            if (activities.isEmpty()) {
                writer.drawMutedLine("Belum ada aktivitas harian.")
            } else {
                val todayKey = ActivityEntity.todayDateKey()
                activities.forEachIndexed { index, act ->
                    if (index > 0) writer.drawItemDivider()
                    writer.ensureSpace(44f)
                    val doneToday = act.isCompletedOn(todayKey)
                    val durationText = if (act.targetMinutes > 0) "${act.targetMinutes} mnt" else "Fleksibel"
                    val completedDays = act.completedDateKeys().size
                    val todayStatus = if (doneToday) "Selesai hari ini" else "Belum selesai hari ini"

                    writer.drawItemTitle(act.title)
                    writer.drawMetaLine("${act.category} • Durasi: $durationText • Total selesai: $completedDays hari ($todayStatus)")
                    if (act.note.isNotBlank()) {
                        writer.drawParagraph(act.note, writer.captionPaint)
                    }
                    act.photoImageName?.let { photo ->
                        exportedImages += writer.drawImagesGrid(listOf(photo), maxImageWidth = 180f, maxImageHeight = 140f)
                    }
                }
            }

            writer.finishAndWriteTo(destUri)

            PdfExportResult(
                noteCount = notes.size,
                transactionCount = transactions.size,
                activityCount = activities.size,
                imageCount = exportedImages,
                pageCount = writer.totalPages,
                timestamp = timestamp
            )
        } finally {
            writer.closeSafely()
        }
    }

    suspend fun exportSingleNoteToPdf(
        destUri: Uri,
        note: NoteEntity
    ): PdfExportResult = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val exportDateLabel = formatFullTimestamp(timestamp)
        val writer = PdfPageWriter(exportDateLabel)
        var exportedImages = 0

        try {
            val noteTitle = note.title.ifBlank { "Catatan Tanpa Judul" }
            val pinLabel = if (note.isPinned) " • Disematkan" else ""
            writer.drawCoverHeader(
                title = "Note All",
                subtitle = noteTitle,
                dateLabel = "${note.category} • Diperbarui ${formatFullTimestamp(note.updatedAt)}$pinLabel"
            )

            if (note.content.isNotBlank()) {
                writer.drawParagraph(note.content, writer.bodyPaint)
                writer.advance(8f)
            } else if (note.imageFileNames().isEmpty()) {
                writer.drawMutedLine("(Catatan kosong)")
            }

            val noteImages = note.imageFileNames()
            if (noteImages.isNotEmpty()) {
                writer.drawSectionHeader("Lampiran Foto (${noteImages.size})")
                exportedImages += writer.drawImagesGrid(noteImages, maxImageWidth = 248f, maxImageHeight = 230f)
            }

            writer.finishAndWriteTo(destUri)

            PdfExportResult(
                noteCount = 1,
                transactionCount = 0,
                activityCount = 0,
                imageCount = exportedImages,
                pageCount = writer.totalPages,
                timestamp = timestamp
            )
        } finally {
            writer.closeSafely()
        }
    }

    private inner class PdfPageWriter(private val exportDateLabel: String) {
        private val document = PdfDocument()
        private var currentPage: PdfDocument.Page? = null
        private var canvas: Canvas? = null
        private var pageNumber = 0
        private var cursorY = MARGIN_TOP
        private var isClosed = false

        val totalPages: Int get() = pageNumber

        val headerTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6F2F0")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val sectionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_PRIMARY
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val itemTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_INK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val metaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_MUTED
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_INK
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val captionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_MUTED
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        val incomeAmountPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_PRIMARY
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val expenseAmountPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_EXPENSE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_MUTED
            textSize = 8.5f
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = COLOR_BORDER
        }

        init {
            startNewPage()
        }

        private fun startNewPage() {
            currentPage?.let { page ->
                document.finishPage(page)
            }
            pageNumber++
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val page = document.startPage(pageInfo)
            currentPage = page
            canvas = page.canvas
            cursorY = MARGIN_TOP

            // Subtle running footer on every page
            val c = page.canvas
            val linePaint = Paint().apply {
                color = COLOR_BORDER
                strokeWidth = 0.6f
            }
            c.drawLine(MARGIN_H, FOOTER_Y - 12f, PAGE_WIDTH - MARGIN_H, FOOTER_Y - 12f, linePaint)
            c.drawText("Note All • $exportDateLabel", MARGIN_H, FOOTER_Y, footerPaint)
            val rightFooter = footerPaint.apply { textAlign = Paint.Align.RIGHT }
            c.drawText("Halaman $pageNumber", PAGE_WIDTH - MARGIN_H, FOOTER_Y, rightFooter)
            footerPaint.textAlign = Paint.Align.LEFT
        }

        fun ensureSpace(heightNeeded: Float) {
            if (cursorY + heightNeeded > CONTENT_BOTTOM) {
                startNewPage()
            }
        }

        fun advance(delta: Float) {
            cursorY += delta
        }

        fun drawCoverHeader(title: String, subtitle: String, dateLabel: String) {
            val subtitleLayout = StaticLayout.Builder
                .obtain(subtitle, 0, subtitle.length, headerSubPaint, (CONTENT_WIDTH - 28f).toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .build()
            val boxHeight = 52f + subtitleLayout.height + 16f
            ensureSpace(boxHeight + 12f)

            val c = canvas ?: return
            val rect = RectF(MARGIN_H, cursorY, PAGE_WIDTH - MARGIN_H, cursorY + boxHeight)
            fillPaint.color = COLOR_PRIMARY
            c.drawRoundRect(rect, 10f, 10f, fillPaint)

            // Warm amber accent strip on left
            fillPaint.color = COLOR_ACCENT
            val accentRect = RectF(MARGIN_H, cursorY, MARGIN_H + 5f, cursorY + boxHeight)
            c.drawRoundRect(accentRect, 4f, 4f, fillPaint)

            c.drawText(title, MARGIN_H + 16f, cursorY + 26f, headerTitlePaint)

            c.save()
            c.translate(MARGIN_H + 16f, cursorY + 34f)
            subtitleLayout.draw(c)
            c.restore()

            c.drawText(dateLabel, MARGIN_H + 16f, cursorY + boxHeight - 12f, headerSubPaint)
            cursorY += boxHeight + 14f
        }

        fun drawSummaryBox(items: List<Pair<String, String>>) {
            if (items.isEmpty()) return
            val boxHeight = 44f
            ensureSpace(boxHeight + 10f)
            val c = canvas ?: return

            val rect = RectF(MARGIN_H, cursorY, PAGE_WIDTH - MARGIN_H, cursorY + boxHeight)
            fillPaint.color = COLOR_SURFACE_TINT
            c.drawRoundRect(rect, 8f, 8f, fillPaint)
            c.drawRoundRect(rect, 8f, 8f, strokePaint)

            val colWidth = CONTENT_WIDTH / items.size
            items.forEachIndexed { idx, (label, value) ->
                val x = MARGIN_H + (idx * colWidth) + 10f
                c.drawText(label, x, cursorY + 16f, metaPaint)
                c.drawText(value, x, cursorY + 32f, itemTitlePaint)
            }
            cursorY += boxHeight + 12f
        }

        fun drawSectionHeader(title: String) {
            ensureSpace(32f)
            val c = canvas ?: return
            fillPaint.color = COLOR_PRIMARY_SOFT
            val rect = RectF(MARGIN_H, cursorY, PAGE_WIDTH - MARGIN_H, cursorY + 24f)
            c.drawRoundRect(rect, 6f, 6f, fillPaint)
            c.drawText(title, MARGIN_H + 10f, cursorY + 16f, sectionPaint)
            cursorY += 32f
        }

        fun drawItemTitle(title: String) {
            drawParagraph(title, itemTitlePaint)
            cursorY += 2f
        }

        fun drawMetaLine(meta: String) {
            drawParagraph(meta, metaPaint)
            cursorY += 3f
        }

        fun drawMutedLine(text: String) {
            drawParagraph(text, captionPaint)
            cursorY += 4f
        }

        fun drawTransactionRow(title: String, meta: String, amountText: String, isIncome: Boolean) {
            ensureSpace(30f)
            val c = canvas ?: return
            val amountPaint = if (isIncome) incomeAmountPaint else expenseAmountPaint
            c.drawText(amountText, PAGE_WIDTH - MARGIN_H, cursorY + 12f, amountPaint)

            val titleWidth = (CONTENT_WIDTH - 135f).coerceAtLeast(160f)
            drawParagraph(title, itemTitlePaint, maxWidth = titleWidth)
            drawMetaLine(meta)
        }

        fun drawItemDivider() {
            ensureSpace(12f)
            val c = canvas ?: return
            cursorY += 4f
            c.drawLine(MARGIN_H, cursorY, PAGE_WIDTH - MARGIN_H, cursorY, strokePaint)
            cursorY += 8f
        }

        fun drawParagraph(
            text: String,
            paint: TextPaint,
            maxWidth: Float = CONTENT_WIDTH
        ) {
            val clean = text.trimEnd()
            if (clean.isEmpty()) return
            val layout = StaticLayout.Builder
                .obtain(clean, 0, clean.length, paint, maxWidth.toInt().coerceAtLeast(1))
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2f, 1.05f)
                .setIncludePad(false)
                .build()

            for (lineIndex in 0 until layout.lineCount) {
                val lineTop = layout.getLineTop(lineIndex)
                val lineBottom = layout.getLineBottom(lineIndex)
                val lineHeight = (lineBottom - lineTop).toFloat().coerceAtLeast(12f)
                ensureSpace(lineHeight + 2f)

                val c = canvas ?: return
                val start = layout.getLineStart(lineIndex)
                val end = layout.getLineEnd(lineIndex)
                val baselineOffset = -layout.getLineAscent(lineIndex).toFloat()
                c.drawText(clean, start, end, MARGIN_H, cursorY + baselineOffset, paint)
                cursorY += lineHeight
            }
        }

        fun drawImagesGrid(
            fileNames: List<String>,
            maxImageWidth: Float = 248f,
            maxImageHeight: Float = 200f
        ): Int {
            var count = 0
            val gap = 14f
            val validFiles = fileNames.mapNotNull { name ->
                val f = imageStorageManager.getImageFile(name)
                if (f.exists()) f else null
            }
            if (validFiles.isEmpty()) return 0

            val pairs = validFiles.chunked(2)
            for (rowFiles in pairs) {
                val decoded = rowFiles.mapNotNull { file ->
                    decodeBitmapForPdf(file, maxDim = 900)
                }
                if (decoded.isEmpty()) continue

                try {
                    val drawSizes = decoded.map { bmp ->
                        val scale = min(maxImageWidth / bmp.width.toFloat(), maxImageHeight / bmp.height.toFloat())
                            .coerceAtMost(1f)
                        Pair(
                            (bmp.width * scale).coerceAtLeast(24f),
                            (bmp.height * scale).coerceAtLeast(24f)
                        )
                    }
                    val rowHeight = drawSizes.maxOf { it.second }
                    ensureSpace(rowHeight + 10f)

                    val c = canvas ?: return count
                    var x = MARGIN_H
                    decoded.forEachIndexed { idx, bmp ->
                        val (w, h) = drawSizes[idx]
                        val destRect = RectF(x, cursorY, x + w, cursorY + h)
                        c.drawBitmap(bmp, null, destRect, null)
                        c.drawRect(destRect, strokePaint)
                        x += w + gap
                        count++
                    }
                    cursorY += rowHeight + 8f
                } finally {
                    decoded.forEach { if (!it.isRecycled) it.recycle() }
                }
            }
            return count
        }

        fun finishAndWriteTo(destUri: Uri) {
            currentPage?.let { page ->
                document.finishPage(page)
                currentPage = null
                canvas = null
            }
            val outputStream = context.contentResolver.openOutputStream(destUri)
                ?: throw IllegalStateException("Tidak dapat membuka file tujuan PDF.")
            outputStream.use { out ->
                document.writeTo(out)
            }
        }

        fun closeSafely() {
            if (!isClosed) {
                currentPage?.let { page ->
                    runCatching { document.finishPage(page) }
                    currentPage = null
                }
                runCatching { document.close() }
                isClosed = true
            }
        }
    }

    private fun decodeBitmapForPdf(file: File, maxDim: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        val w = bounds.outWidth
        val h = bounds.outHeight
        if (w <= 0 || h <= 0) return null

        var sample = 1
        while (max(w / sample, h / sample) > maxDim) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(file.absolutePath, opts)
    }
}
