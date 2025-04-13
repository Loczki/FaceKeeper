package com.google.mediapipe.examples.facedetection

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: FaceDetectorResult? = null
    private var boxPaint = Paint()
    private var cyberpunkOuterPaint = Paint()
    private var cyberpunkInnerPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var namePaint = Paint()
    private var statusPaint = Paint()

    private var scanLineY = 0f
    private var scanLineAnimator: ValueAnimator? = null
    private var avatarBitmap: Bitmap? = null
    private var avatarBitmap_jacus: Bitmap? = null
    private var avatarBitmap_wojtus: Bitmap? = null
    private var runeBitmap1: Bitmap? = null
    private var runeBitmap2: Bitmap? = null
    private var scanLinePaint = Paint()

    private var scaleFactor: Float = 1f
    private var bounds = Rect()
    private val cornerRadius = 8f
    private val animCornerLength = 20f
    private val cornerStrokeWidth = 8f
    private val scanLineHeight = 5f

    // Hardcoded information
    private val personName = "V. SILVERHAND"
    private val personName_jacus = "JACEK URBANOWICZ"
    private val personName_wojtus = "WOJCIECH ŁOBODA"
    private val personStatus = "STATUS: IDENTIFIED"
    private val idNumber = "ID: NC-" + (1000000..9999999).random()
    private val currentDateTime = "2025-04-13 01:55:53"
    private val currentUser = "Piotreqsl"

    // Dodaj nowe pole dla efektu blur
    private val blurPaint = Paint().apply {
        maskFilter = BlurMaskFilter(50f, BlurMaskFilter.Blur.NORMAL)
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    init {
        initPaints()
        loadResources()
        startAnimations()
    }

    private fun loadResources() {
        // Load the avatar bitmaps
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.cyberpunk_avatar)
        val drawable_jacus = ContextCompat.getDrawable(context!!, R.drawable.jacus)
        val drawable_wojtus = ContextCompat.getDrawable(context!!, R.drawable.wojtus)
        val runeDrawable1 = ContextCompat.getDrawable(context!!, R.drawable.runa1)
        val runeDrawable2 = ContextCompat.getDrawable(context!!, R.drawable.runa2)

        if (drawable != null) {
            val width = 150
            val height = 150
            avatarBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(avatarBitmap!!)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }

        if (drawable_jacus != null) {
            val width = 150
            val height = 150
            avatarBitmap_jacus = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(avatarBitmap_jacus!!)
            drawable_jacus.setBounds(0, 0, canvas.width, canvas.height)
            drawable_jacus.draw(canvas)
        }

        if (drawable_wojtus != null) {
            val width = 150
            val height = 150
            avatarBitmap_wojtus = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(avatarBitmap_wojtus!!)
            drawable_wojtus.setBounds(0, 0, canvas.width, canvas.height)
            drawable_wojtus.draw(canvas)
        }

        if (runeDrawable1 != null) {
            val width = 150
            val height = 150
            runeBitmap1 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(runeBitmap1!!)
            runeDrawable1.setBounds(0, 0, canvas.width, canvas.height)
            runeDrawable1.draw(canvas)
        }

        if (runeDrawable2 != null) {
            val width = 150
            val height = 150
            runeBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(runeBitmap2!!)
            runeDrawable2.setBounds(0, 0, canvas.width, canvas.height)
            runeDrawable2.draw(canvas)
        }
    }

    private fun startAnimations() {
        scanLineAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                scanLineY = animatedValue
                invalidate()
            }
            start()
        }
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.parseColor("#80000000")
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.mp_primary)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE

        cyberpunkOuterPaint.color = Color.parseColor("#00FFFF")
        cyberpunkOuterPaint.strokeWidth = cornerStrokeWidth
        cyberpunkOuterPaint.style = Paint.Style.STROKE
        cyberpunkOuterPaint.pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        cyberpunkOuterPaint.setShadowLayer(15f, 0f, 0f, Color.parseColor("#00FFFF"))

        cyberpunkInnerPaint.color = Color.parseColor("#FF00FF")
        cyberpunkInnerPaint.strokeWidth = cornerStrokeWidth / 2
        cyberpunkInnerPaint.style = Paint.Style.STROKE
        cyberpunkInnerPaint.pathEffect = DashPathEffect(floatArrayOf(5f, 10f), 0f)

        scanLinePaint.color = Color.parseColor("#80FF00FF")
        scanLinePaint.strokeWidth = scanLineHeight
        scanLinePaint.style = Paint.Style.STROKE

        namePaint.color = Color.parseColor("#00FFFF")
        namePaint.textSize = 40f
        namePaint.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

        statusPaint.color = Color.parseColor("#FF00FF")
        statusPaint.textSize = 30f
        statusPaint.typeface = Typeface.create("monospace", Typeface.NORMAL)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        results?.let {
            var index = 0
            for (detection in it.detections()) {
                val boundingBox = detection.boundingBox()

                val top = boundingBox.top * scaleFactor
                val bottom = boundingBox.bottom * scaleFactor
                val left = boundingBox.left * scaleFactor
                val right = boundingBox.right * scaleFactor

                val boxHeight = bottom - top
                val boxWidth = right - left

                drawCyberpunkBox(canvas, left, top, right, bottom)

                val currentScanY = top + (boxHeight * scanLineY)
                canvas.drawLine(left, currentScanY, right, currentScanY, scanLinePaint)

                val indexText = String.format("ID: %s", index)

                val bitmap = when (index) {
                    0 -> avatarBitmap_jacus
                    1 -> avatarBitmap_wojtus
                    else -> avatarBitmap
                }

                val name = when (index) {
                    0 -> personName_jacus
                    1 -> personName_wojtus
                    else -> personName
                }

                val status = when (index) {
                    0 -> "STATUS: IDENTIFIED"
                    1 -> "STATUS: IDENTIFIED"
                    else -> "STATUS: UNKNOWN"
                }

                bitmap?.let { bitmap ->
                    val infoBackgroundRect = RectF(
                        left + 10f,
                        top - bitmap.height - 10f,
                        left + bitmap.width + 10f + textPaint.measureText(personName) + 170f, // Increased width for rune
                        top - 10f
                    )
                    canvas.drawRect(infoBackgroundRect, textBackgroundPaint)

                    val avatarLeft = left + 10f
                    val avatarTop = top - bitmap.height - 10f

                    if (index > 1) {
                        canvas.drawRect(
                            left,
                            top,
                            right,
                            bottom,
                            blurPaint
                        )
                    }

                    if (avatarTop > 0) {
                        // Draw avatar
                        canvas.drawBitmap(bitmap, avatarLeft, avatarTop, null)

                        // Draw rune for identified users
                        if (index <= 1) {
                            val runeBitmap = if (index == 0) runeBitmap1 else runeBitmap2
                            runeBitmap?.let { rune ->
                                val runeLeft = left + bitmap.width + textPaint.measureText(personName) + 20f
                                canvas.drawBitmap(rune, runeLeft, avatarTop, null)
                            }
                        }

                        canvas.drawText(
                            name, avatarLeft + bitmap.width + 10f,
                            avatarTop + 40f, namePaint
                        )
                        canvas.drawText(
                            status, avatarLeft + bitmap.width + 10f,
                            avatarTop + 80f, statusPaint
                        )
                        canvas.drawText(
                            indexText, avatarLeft + bitmap.width + 10f,
                            avatarTop + 120f, statusPaint
                        )
                    } else {
                        val altAvatarTop = bottom + 10f
                        canvas.drawBitmap(bitmap, avatarLeft, altAvatarTop, null)

                        // Draw rune for identified users
                        if (index <= 1) {
                            val runeBitmap = if (index == 0) runeBitmap1 else runeBitmap2
                            runeBitmap?.let { rune ->
                                val runeLeft = left + bitmap.width + textPaint.measureText(personName) + 20f
                                canvas.drawBitmap(rune, runeLeft, altAvatarTop, null)
                            }
                        }

                        canvas.drawText(
                            name, avatarLeft + bitmap.width + 10f,
                            altAvatarTop + 40f, namePaint
                        )
                        canvas.drawText(
                            status, avatarLeft + bitmap.width + 10f,
                            altAvatarTop + 80f, statusPaint
                        )
                        canvas.drawText(
                            indexText, avatarLeft + bitmap.width + 10f,
                            altAvatarTop + 120f, statusPaint
                        )
                    }
                }

                val scoreText = String.format("MATCH: %.1f%%", detection.categories()[0].score() * 100)
                val scoreWidth = statusPaint.measureText(scoreText)
                val scoreBackgroundRect = RectF(
                    right - scoreWidth - 20f,
                    top - 40f,
                    right,
                    top
                )
                canvas.drawRect(scoreBackgroundRect, textBackgroundPaint)
                canvas.drawText(
                    scoreText, scoreBackgroundRect.left + 10f,
                    scoreBackgroundRect.bottom - 10f, statusPaint
                )
                index++
            }
        }
    }

    private fun drawCyberpunkBox(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        val outerRect = RectF(left - 10f, top - 10f, right + 10f, bottom + 10f)
        canvas.drawRect(outerRect, cyberpunkOuterPaint)

        val innerRect = RectF(left + 5f, top + 5f, right - 5f, bottom - 5f)
        canvas.drawRect(innerRect, cyberpunkInnerPaint)

        canvas.drawLine(left - 10f, top - 10f, left - 10f + animCornerLength, top - 10f, cyberpunkOuterPaint)
        canvas.drawLine(left - 10f, top - 10f, left - 10f, top - 10f + animCornerLength, cyberpunkOuterPaint)

        canvas.drawLine(right + 10f - animCornerLength, top - 10f, right + 10f, top - 10f, cyberpunkOuterPaint)
        canvas.drawLine(right + 10f, top - 10f, right + 10f, top - 10f + animCornerLength, cyberpunkOuterPaint)

        canvas.drawLine(left - 10f, bottom + 10f, left - 10f + animCornerLength, bottom + 10f, cyberpunkOuterPaint)
        canvas.drawLine(left - 10f, bottom + 10f - animCornerLength, left - 10f, bottom + 10f, cyberpunkOuterPaint)

        canvas.drawLine(right + 10f - animCornerLength, bottom + 10f, right + 10f, bottom + 10f, cyberpunkOuterPaint)
        canvas.drawLine(right + 10f, bottom + 10f - animCornerLength, right + 10f, bottom + 10f, cyberpunkOuterPaint)
    }

    fun setResults(
        detectionResults: FaceDetectorResult,
        imageHeight: Int,
        imageWidth: Int,
    ) {
        results = detectionResults
        scaleFactor = min(width * 1f / imageWidth, height * 1f / imageHeight)
        invalidate()
    }

    fun clear() {
        results = null
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        cyberpunkOuterPaint.reset()
        cyberpunkInnerPaint.reset()
        namePaint.reset()
        statusPaint.reset()
        scanLinePaint.reset()
        invalidate()
        initPaints()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scanLineAnimator?.cancel()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}