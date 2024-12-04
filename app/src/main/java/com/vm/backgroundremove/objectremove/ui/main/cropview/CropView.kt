package com.vm.backgroundremove.objectremove.ui.main.cropview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.util.toDp
import kotlin.math.sqrt

class CropView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private var bitmap: Bitmap? = null
    private var backgroundBitmap: Bitmap? = null
    private val bound = RectF()
    private val boundBg = RectF()
    private val boundPoint = FloatArray(4)
    private val mappedBoundPoint = FloatArray(4)
    private val matrixValues = FloatArray(9)
    private val matrix = Matrix()
    private val padding = 20f.toDp()
    private val minScaleFactor = 0.25f
    private val maxScaleFactor = 4f
    private val bg_none = AppCompatResources.getDrawable(context, R.drawable.bg_none)

    private var actionType = ActionType.NONE
    private val oldTouchPoint = PointF()
    private var oldDistance = 0f

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        setWillNotDraw(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateBitmapBound(w.toFloat(), h.toFloat())
        bg_none?.setBounds(0,0, w,h)
        boundBg.set(0f,0f,w.toFloat(),h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        bg_none?.draw(canvas)
        backgroundBitmap?.let {
            canvas.drawBitmap(it, null, boundBg, null)
        }
        drawBitmap(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> handleDown(event)
            MotionEvent.ACTION_POINTER_DOWN -> handlePointerDown(event)
            MotionEvent.ACTION_MOVE -> handleMove(event)
            MotionEvent.ACTION_UP -> handleUp()
            else -> false
        }
    }

    fun setBackgroundBitmap(bitmap: Bitmap) {
        backgroundBitmap = bitmap
        invalidate() // Vẽ lại view khi có sự thay đổi
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        calculateBitmapBound(measuredWidth.toFloat(), measuredHeight.toFloat())
        invalidate()
    }

    private fun drawBitmap(canvas: Canvas) {
        canvas.save()
        canvas.concat(matrix)
        bitmap?.let {
            canvas.drawBitmap(it, null, bound, null)
        }
        canvas.restore()
    }

    private fun calculateBitmapBound(parentWidth: Float, parentHeight: Float) {
        bitmap?.let {
            val maxWidth = parentWidth - padding * 2f
            val maxHeight = parentHeight - padding * 2f
            var width = maxWidth
            var height = (maxWidth * it.height) / it.width
            if (height > maxHeight) {
                height = maxHeight
                width = (height * it.width) / it.height
            }
            val left = (parentWidth - width) / 2f
            val top = (parentHeight - height) / 2f
            bound.set(left, top, left + width, top + height)

            boundPoint[0] = bound.left
            boundPoint[1] = bound.top
            boundPoint[2] = bound.right
            boundPoint[3] = bound.bottom
        }
    }

    private fun getMappedBoundPoint() = matrix.mapPoints(mappedBoundPoint, boundPoint)

    private fun handleUp(): Boolean {
        if (actionType != ActionType.NONE) {
            actionType = ActionType.NONE
            return true
        }
        return false
    }

    private fun handleDown(event: MotionEvent): Boolean {
        getMappedBoundPoint()
        if (insideImage(event.x, event.y)) {
            actionType = ActionType.DRAG
            oldTouchPoint.set(event.x, event.y)
            return true
        }
        return false
    }

    private fun handlePointerDown(event: MotionEvent): Boolean {
        if (event.pointerCount < 2) return false
        getMappedBoundPoint()
        if (insideImage(event.getX(1), event.getY(1))) {
            oldDistance =
                calculateDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
            actionType = ActionType.ZOOM_AND_ROTATION
            return true
        }
        return false
    }

    private fun handleMove(event: MotionEvent): Boolean {
        return when (actionType) {
            ActionType.DRAG -> move(event)
            ActionType.ZOOM_AND_ROTATION -> zoom(event)
            else -> false
        }
    }

    private fun zoom(event: MotionEvent): Boolean {
        if (event.pointerCount < 2) return false
        val newDistance =
            calculateDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
        val currentScaleFactor = newDistance / oldDistance
        val scaleFactor = currentScaleFactor * getScaleFactor()
        if (scaleFactor in minScaleFactor..maxScaleFactor) {
            val midX = (mappedBoundPoint[0] + mappedBoundPoint[2]) / 2f
            val midY = (mappedBoundPoint[1] + mappedBoundPoint[3]) / 2f
            oldDistance = newDistance
            matrix.postScale(currentScaleFactor, currentScaleFactor, midX, midY)
            invalidate()
            return true
        }
        return false
    }

    private fun move(event: MotionEvent): Boolean {
        moveX(event.x - oldTouchPoint.x)
        moveY(event.y - oldTouchPoint.y)
        oldTouchPoint.set(event.x, event.y)
        invalidate()
        return true
    }

    private fun moveX(distance: Float) {
        matrix.postTranslate(distance, 0f)
    }

    private fun moveY(distance: Float) {
        matrix.postTranslate(0f, distance)
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = (x1 - x2).toDouble()
        val dy = (y1 - y2).toDouble()
        return sqrt(dx * dx + dy * dy).toFloat()
    }

    private fun insideImage(x: Float, y: Float): Boolean {
        val left = mappedBoundPoint[0]
        val top = mappedBoundPoint[1]
        val right = mappedBoundPoint[2]
        val bottom = mappedBoundPoint[3]
        return left < right && top < bottom
                && x >= left && x < right && y >= top && y < bottom
    }

    private fun getMatrixValue(matrix: Matrix, index: Int): Float {
        matrix.getValues(matrixValues)
        return matrixValues[index]
    }

    private fun getScaleFactor(): Float {
        val scaleX = getMatrixValue(matrix, Matrix.MSCALE_X)
        val skewY = getMatrixValue(matrix, Matrix.MSKEW_Y)
        return sqrt(scaleX * scaleX + skewY * skewY)
    }
}
