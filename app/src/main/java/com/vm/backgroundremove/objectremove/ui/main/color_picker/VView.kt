package com.vm.backgroundremove.objectremove.ui.main.color_picker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.vm.backgroundremove.objectremove.a8_app_utils.toDp

class VView(context: Context, attrs: AttributeSet?) : View(context, attrs), HSView.OnHSChange {
    private val strokeWidth = 1f.toDp()
    private val cornerRadius = 8f.toDp()
    private var controllerRadius = 10f.toDp() // Bán kính của chấm tròn
    private var controllerPosition = 20f.toDp()

    private var bound = RectF()
    private val backgroundPaint = Paint().apply {
        color = Color.WHITE
    }
    private val shaderPaint = Paint()
    private val circlePaint = Paint().apply {
        isAntiAlias = true
    }
    private val circleStrokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE // Vẽ chỉ đường viền
        strokeWidth = 3f.toDp() // Độ dày của đường viền
        color = Color.WHITE // Màu viền, bạn có thể thay đổi
    }

    private val currentColorArray = floatArrayOf(200f, 1f, 1f)
    private val colorArray = floatArrayOf(200f, 1f, 1f)

    private var onValueChange: (Float) -> Unit = {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Vẽ thanh màu
        canvas.drawRoundRect(
            bound.left,
            bound.top,
            bound.right,
            bound.bottom,
            cornerRadius,
            cornerRadius,
            shaderPaint
        )

        // Vẽ viền chấm tròn
        canvas.drawCircle(
            controllerPosition,
            bound.centerY(),
            controllerRadius - circleStrokePaint.strokeWidth / 2, // Tránh viền bị cắt
            circleStrokePaint
        )

        // Vẽ chấm tròn
        canvas.drawCircle(
            controllerPosition,
            bound.centerY(),
            controllerRadius - circleStrokePaint.strokeWidth, // Chừa không gian cho viền
            circlePaint
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Thiết lập chiều rộng đầy đủ cho thanh màu
        bound = RectF(
            strokeWidth,
            strokeWidth,
            w - strokeWidth,
            h - strokeWidth
        )

        // Điều chỉnh bán kính chấm tròn sao cho bằng một nửa chiều cao của thanh
        controllerRadius = bound.height() / 2f

        // Tính toán vị trí ban đầu của chấm tròn (giới hạn trong thanh)
        controllerPosition = (1f - currentColorArray[2]) * (bound.width() - 2 * controllerRadius) + bound.left + controllerRadius

        setupShader(bound)
        updateCircleColor()
    }



    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (event.x < bound.left) {
                    changePosition(bound.left)
                } else if (event.x > bound.right) {
                    changePosition(bound.right)
                } else if (event.x >= bound.left && event.x <= bound.right) {
                    changePosition(event.x)
                }
                invalidate()
                true
            }

            else -> super.onTouchEvent(event)
        }
    }

    override fun onHSChange(hue: Float, saturation: Float) {
        currentColorArray[0] = hue
        currentColorArray[1] = saturation
        colorArray[0] = hue
        colorArray[1] = saturation
        setupShader(bound)
        updateCircleColor()
        invalidate()
    }

    override fun setValue(value: Float) {
        currentColorArray[2] = value
        controllerPosition = (1f - currentColorArray[2]) * bound.width() + bound.left
        updateCircleColor()
        invalidate()
    }

    fun setOnValueChange(onValueChange: (Float) -> Unit = {}) {
        this.onValueChange = onValueChange
    }

    private fun setupShader(bound: RectF) {
        val color = Color.HSVToColor(colorArray)
        shaderPaint.shader = LinearGradient(
            bound.left, bound.top, bound.right, bound.top, color, Color.BLACK, Shader.TileMode.CLAMP
        )
    }

    private fun changePosition(newPosition: Float) {
        // Giới hạn vị trí tâm chấm tròn trong khoảng `bound.left + controllerRadius` và `bound.right - controllerRadius`
        controllerPosition = newPosition.coerceIn(bound.left + controllerRadius, bound.right - controllerRadius)

        // Cập nhật giá trị màu sắc (giá trị "value" trong HSV)
        currentColorArray[2] = 1f - ((controllerPosition - bound.left - controllerRadius) / (bound.width() - 2 * controllerRadius))
        updateCircleColor()
        onValueChange(currentColorArray[2])
    }



    private fun updateCircleColor() {
        val color = Color.HSVToColor(currentColorArray)
        circlePaint.color = color
    }
}
