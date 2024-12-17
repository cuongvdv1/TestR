package com.vm.backgroundremove.objectremove.ui.main.cropview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.ui.main.remove_background.ResultRemoveBackGroundActivity
import com.vm.backgroundremove.objectremove.util.toDp
import java.io.IOException
import kotlin.math.sqrt

class CropView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val undoStack: MutableList<Bitmap?> = mutableListOf()
    private val redoStack: MutableList<Bitmap?> = mutableListOf()

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
    private var isShowingBitmap = true
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        setWillNotDraw(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateBitmapBound(w.toFloat(), h.toFloat())
        bg_none?.setBounds(0, 0, w, h)
        boundBg.set(0f, 0f, w.toFloat(), h.toFloat())
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

    fun createColorBitmap(color: Int, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color) // Vẽ toàn bộ bitmap với màu được chỉ định
        return bitmap
    }

    fun hasBackgroundBitmap(): Boolean {
        return backgroundBitmap != null
    }

    fun setBackgroundBitmap(bitmap: Bitmap, needSaveStack: Boolean = true) {

        backgroundBitmap = bitmap
        invalidate() // Vẽ lại view khi có sự thay đổi
        if (needSaveStack) {
            saveStack()
        }
        (context as? ResultRemoveBackGroundActivity)?.updateButtonStates()
    }

    fun saveStack() {
        undoStack.add(backgroundBitmap) // Lưu trạng thái hiện tại vào undo stack
        redoStack.clear() // Xóa redo stack vì đây là một thay đổi mới
    }

    fun applyCurrentConfig() {
        if (undoStack.isNotEmpty()) {
            backgroundBitmap = undoStack.last()
            invalidate()
        } else {
            clearBackGround()
        }
    }

    fun undo() {
        Log.d("TAG_UNDO", "UNDO: ${undoStack.size}")
        if (undoStack.isNotEmpty()) {
            redoStack.add(backgroundBitmap) // Lưu trạng thái hiện tại vào redo stack
            undoStack.removeLast()
            if (undoStack.isNotEmpty()) {

                backgroundBitmap = undoStack.last()  // Lấy trạng thái cuối cùng từ undo stack
             invalidate() // Vẽ lại view
            } else {
                clearBackGround()
            }
            (context as? ResultRemoveBackGroundActivity)?.updateButtonStates()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(backgroundBitmap) // Lưu trạng thái hiện tại vào undo stack
            backgroundBitmap = redoStack.removeLast() // Lấy trạng thái cuối cùng từ redo stack
            invalidate() // Vẽ lại view
            (context as? ResultRemoveBackGroundActivity)?.updateButtonStates()
        }
    }

    fun setBackgroundWithColor(color: Int) {
        val bitmap = createColorBitmap(color, measuredWidth, measuredHeight)
        setBackgroundBitmap(bitmap)
        (context as? ResultRemoveBackGroundActivity)?.updateButtonStates()
    }

    fun setBackgroundWithGradient(startColor: Int, endColor: Int) {
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(startColor, endColor)
        )
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        gradientDrawable.setBounds(0, 0, width, height)
        gradientDrawable.draw(canvas)
        setBackgroundBitmap(bitmap, false)
        (context as? ResultRemoveBackGroundActivity)?.updateButtonStates()
    }

    fun clearBackGround() {
        backgroundBitmap = null
        invalidate()
    }

    fun canUndo(): Boolean {
        return undoStack.isNotEmpty()
    }

    fun canRedo(): Boolean {
        return redoStack.isNotEmpty()
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

    fun toggleImage(bitmap: Bitmap, uri: Uri) {
        if (isShowingBitmap) {
            // Hiển thị ảnh từ URI
            setUri(uri)
        } else {
            // Hiển thị ảnh từ bitmap
            setBitmap(bitmap)
        }
        // Đảo trạng thái
        isShowingBitmap = !isShowingBitmap
    }

    fun setUri(uri: Uri) {
        try {
            // Chuyển đổi URI thành Bitmap
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            bitmap?.let {
                setBitmap(it) // Gọi hàm setBitmap để thiết lập bitmap cho CropView
            }
        } catch (e: IOException) {
            e.printStackTrace() // Xử lý lỗi nếu có
        }
    }

    private fun calculateBitmapBound(parentWidth: Float, parentHeight: Float) {
        bitmap?.let {
            val maxHeight = parentHeight - padding * 2f
            val maxWidth = parentWidth - padding * 2f
            var height = maxHeight
            var width = (height * it.width) / it.height

            // Nếu chiều rộng vượt quá chiều rộng tối đa, điều chỉnh lại dựa trên chiều rộng
            if (width > maxWidth) {
                width = maxWidth
                height = (width * it.height) / it.width
            }

            val left = (parentWidth - width) / 2f
            val top = (parentHeight - height)
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
