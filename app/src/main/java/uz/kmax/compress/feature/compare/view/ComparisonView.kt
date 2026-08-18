package uz.kmax.compress.feature.compare.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.min

class ComparisonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var originalBitmap: Bitmap? = null
    private var compressedBitmap: Bitmap? = null

    private var sliderPosition = 0.5f
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 6f
        style = Paint.Style.FILL_AND_STROKE
        setShadowLayer(12f, 0f, 0f, Color.parseColor("#40000000"))
    }

    private val imagePaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)

    private var scaleFactor = 1.0f
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private var isDraggingSlider = false

    // Pre-allocated objects for optimization
    private val originalMatrix = Matrix()
    private val compressedMatrix = Matrix()
    private var matricesCalculated = false

    fun setBitmaps(original: Bitmap, compressed: Bitmap) {
        this.originalBitmap = original
        this.compressedBitmap = compressed
        matricesCalculated = false
        postInvalidateOnAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        matricesCalculated = false
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        val x = event.x

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val sliderX = sliderPosition * width
                if (Math.abs(x - sliderX) < 80f) { // Increased hit area for better UX
                    isDraggingSlider = true
                    parent.requestDisallowInterceptTouchEvent(true)
                    performClick()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDraggingSlider) {
                    sliderPosition = (x / width).coerceIn(0f, 1f)
                    postInvalidateOnAnimation()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDraggingSlider = false
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun calculateMatrices() {
        val orig = originalBitmap ?: return
        val comp = compressedBitmap ?: return
        
        calculateFitMatrix(orig, originalMatrix)
        calculateFitMatrix(comp, compressedMatrix)
        
        matricesCalculated = true
    }

    private fun calculateFitMatrix(bitmap: Bitmap, matrix: Matrix) {
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        if (viewWidth == 0f || viewHeight == 0f) return

        val bitmapWidth = bitmap.width.toFloat()
        val bitmapHeight = bitmap.height.toFloat()

        val scale = min(viewWidth / bitmapWidth, viewHeight / bitmapHeight) * scaleFactor
        val dx = (viewWidth - bitmapWidth * scale) / 2
        val dy = (viewHeight - bitmapHeight * scale) / 2

        matrix.reset()
        matrix.postScale(scale, scale)
        matrix.postTranslate(dx, dy)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val orig = originalBitmap ?: return
        val comp = compressedBitmap ?: return

        if (!matricesCalculated) {
            calculateMatrices()
        }

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val sliderX = sliderPosition * viewWidth

        // Draw Original (Left)
        canvas.save()
        canvas.clipRect(0f, 0f, sliderX, viewHeight)
        canvas.drawBitmap(orig, originalMatrix, imagePaint)
        canvas.restore()

        // Draw Compressed (Right)
        canvas.save()
        canvas.clipRect(sliderX, 0f, viewWidth, viewHeight)
        canvas.drawBitmap(comp, compressedMatrix, imagePaint)
        canvas.restore()

        // Draw Divider
        canvas.drawLine(sliderX, 0f, sliderX, viewHeight, dividerPaint)
        
        // Draw Handle (Thumb)
        val handleRadius = 48f
        dividerPaint.style = Paint.Style.FILL
        canvas.drawCircle(sliderX, viewHeight / 2, handleRadius, dividerPaint)
        
        // Draw icon/arrows on handle
        dividerPaint.color = Color.BLACK
        dividerPaint.strokeWidth = 4f
        dividerPaint.setShadowLayer(0f, 0f, 0f, 0) // Remove shadow for arrows
        val arrowSize = 15f
        // Left arrow
        canvas.drawLine(sliderX - 20, viewHeight / 2, sliderX - 20 + arrowSize, viewHeight / 2 - arrowSize, dividerPaint)
        canvas.drawLine(sliderX - 20, viewHeight / 2, sliderX - 20 + arrowSize, viewHeight / 2 + arrowSize, dividerPaint)
        // Right arrow
        canvas.drawLine(sliderX + 20, viewHeight / 2, sliderX + 20 - arrowSize, viewHeight / 2 - arrowSize, dividerPaint)
        canvas.drawLine(sliderX + 20, viewHeight / 2, sliderX + 20 - arrowSize, viewHeight / 2 + arrowSize, dividerPaint)
        
        // Reset for next draw
        dividerPaint.color = Color.WHITE
        dividerPaint.strokeWidth = 6f
        dividerPaint.setShadowLayer(12f, 0f, 0f, Color.parseColor("#40000000"))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        originalBitmap = null
        compressedBitmap = null
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(1.0f, 5.0f)
            matricesCalculated = false
            postInvalidateOnAnimation()
            return true
        }
    }
}
