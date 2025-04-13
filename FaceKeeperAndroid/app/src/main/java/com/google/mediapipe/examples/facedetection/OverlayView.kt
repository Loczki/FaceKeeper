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

    init {
        initPaints()
        loadResources()
        startAnimations()
    }

    private fun loadResources() {
        // Load the avatar bitmap
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.cyberpunk_avatar)
        val drawable_jacus = ContextCompat.getDrawable(context!!, R.drawable.jacus)
        val drawable_wojtus = ContextCompat.getDrawable(context!!, R.drawable.wojtus)
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
    }

    private fun startAnimations() {
        // Scan line animation
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

    private fun initPaints() {
        textBackgroundPaint.color = Color.parseColor("#80000000") // Semi-transparent black
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.mp_primary)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE

        // Cyberpunk-style outer box
        cyberpunkOuterPaint.color = Color.parseColor("#00FFFF") // Cyan
        cyberpunkOuterPaint.strokeWidth = cornerStrokeWidth
        cyberpunkOuterPaint.style = Paint.Style.STROKE
        cyberpunkOuterPaint.pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        cyberpunkOuterPaint.setShadowLayer(15f, 0f, 0f, Color.parseColor("#00FFFF"))

        // Cyberpunk-style inner box
        cyberpunkInnerPaint.color = Color.parseColor("#FF00FF") // Magenta
        cyberpunkInnerPaint.strokeWidth = cornerStrokeWidth / 2
        cyberpunkInnerPaint.style = Paint.Style.STROKE
        cyberpunkInnerPaint.pathEffect = DashPathEffect(floatArrayOf(5f, 10f), 0f)

        // Scan line paint
        scanLinePaint.color = Color.parseColor("#80FF00FF") // Semi-transparent magenta
        scanLinePaint.strokeWidth = scanLineHeight
        scanLinePaint.style = Paint.Style.STROKE

        // Name text paint
        namePaint.color = Color.parseColor("#00FFFF") // Cyan
        namePaint.textSize = 40f
        namePaint.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

        // Status text paint
        statusPaint.color = Color.parseColor("#FF00FF") // Magenta
        statusPaint.textSize = 30f
        statusPaint.typeface = Typeface.create("monospace", Typeface.NORMAL)
    }

    // Dodaj nowe pole dla efektu blur
    private val blurPaint = Paint().apply {
        maskFilter = BlurMaskFilter(50f, BlurMaskFilter.Blur.NORMAL)
        color = Color.BLACK
        style = Paint.Style.FILL
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

                // Draw cyberpunk-style outer box with corner accents
                drawCyberpunkBox(canvas, left, top, right, bottom)

                // Draw scan line
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

                // Draw avatar image
                bitmap?.let { bitmap ->
                    val infoBackgroundRect = RectF(
                        left + 10f,
                        top - bitmap.height - 10f,
                        left + bitmap.width + 10f + textPaint.measureText(personName),
                        top - 10f
                    )
                    canvas.drawRect(infoBackgroundRect, textBackgroundPaint)

                    val avatarLeft = left + 10f
                    val avatarTop = top - bitmap.height - 10f

                    // Jeśli to niezidentyfikowana twarz (index > 1), dodaj efekt rozmycia
                    if (index > 1) {
                        // Narysuj prostokąt z efektem rozmycia na obszarze twarzy
                        canvas.drawRect(
                            left,
                            top,
                            right,
                            bottom,
                            blurPaint
                        )
                    }

                    if (avatarTop > 0) {
                        canvas.drawBitmap(bitmap, avatarLeft, avatarTop, null)

                        // Draw name and status
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
                        // Draw below the face if not enough space above
                        val altAvatarTop = bottom + 10f
                        canvas.drawBitmap(bitmap, avatarLeft, altAvatarTop, null)

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

                // Draw confidence score in cyberpunk style
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
        // Draw main box with dashed effect
        val outerRect = RectF(left - 10f, top - 10f, right + 10f, bottom + 10f)
        canvas.drawRect(outerRect, cyberpunkOuterPaint)

        // Draw inner box
        val innerRect = RectF(left + 5f, top + 5f, right - 5f, bottom - 5f)
        canvas.drawRect(innerRect, cyberpunkInnerPaint)

        // Draw corner accents (top-left)
        canvas.drawLine(left - 10f, top - 10f, left - 10f + animCornerLength, top - 10f, cyberpunkOuterPaint)
        canvas.drawLine(left - 10f, top - 10f, left - 10f, top - 10f + animCornerLength, cyberpunkOuterPaint)

        // Draw corner accents (top-right)
        canvas.drawLine(right + 10f - animCornerLength, top - 10f, right + 10f, top - 10f, cyberpunkOuterPaint)
        canvas.drawLine(right + 10f, top - 10f, right + 10f, top - 10f + animCornerLength, cyberpunkOuterPaint)

        // Draw corner accents (bottom-left)
        canvas.drawLine(left - 10f, bottom + 10f, left - 10f + animCornerLength, bottom + 10f, cyberpunkOuterPaint)
        canvas.drawLine(left - 10f, bottom + 10f - animCornerLength, left - 10f, bottom + 10f, cyberpunkOuterPaint)

        // Draw corner accents (bottom-right)
        canvas.drawLine(right + 10f - animCornerLength, bottom + 10f, right + 10f, bottom + 10f, cyberpunkOuterPaint)
        canvas.drawLine(right + 10f, bottom + 10f - animCornerLength, right + 10f, bottom + 10f, cyberpunkOuterPaint)
    }

    fun setResults(
        detectionResults: FaceDetectorResult,
        imageHeight: Int,
        imageWidth: Int,
    ) {
        results = detectionResults

        // Images, videos and camera live streams are displayed in FIT_START mode. So we need to scale
        // up the bounding box to match with the size that the images/videos/live streams being
        // displayed.
        scaleFactor = min(width * 1f / imageWidth, height * 1f / imageHeight)

        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scanLineAnimator?.cancel()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }

}