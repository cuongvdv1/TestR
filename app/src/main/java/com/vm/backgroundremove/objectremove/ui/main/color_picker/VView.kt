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
import androidx.appcompat.content.res.AppCompatResources
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a8_app_utils.toDp

class VView(context: Context, attrs: AttributeSet?) : View(context, attrs), HSView.OnHSChange {
    private val strokeWidth = 1f.toDp()
    private val cornerRadius = 8f.toDp()
    private var controllerSize = 0f
    private var verticalSpacing = 0f
    private var controllerPosition = 20f.toDp()
    private var polygonTop = AppCompatResources.getDrawable(context, R.drawable.ic_circle_color8)
    private var polygonBottom =
        AppCompatResources.getDrawable(context, R.drawable.ic_circle_color8)

    private var bound = RectF()
    private val backgroundPaint = Paint().apply {
        color = Color.WHITE
    }
    private val controllerPaint = Paint().apply {
        color = Color.WHITE
    }

    private val shaderPaint = Paint()

    private val currentColorArray = floatArrayOf(200f, 1f, 1f)
    private val colorArray = floatArrayOf(200f, 1f, 1f)

    private var onValueChange: (Float) -> Unit = {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRoundRect(
            bound.left - strokeWidth,
            bound.top - strokeWidth,
            bound.right + strokeWidth,
            bound.bottom + strokeWidth,
            cornerRadius,
            cornerRadius,
            backgroundPaint
        )
        canvas.drawRoundRect(bound, cornerRadius, cornerRadius, shaderPaint)
        canvas.drawCircle(controllerPosition, measuredHeight / 2f, bound.height() / 2f, controllerPaint)
//        polygonTop?.draw(canvas)
//        polygonBottom?.draw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        controllerSize = h * 0.3125f
        verticalSpacing = controllerSize * 0.4f
        bound = RectF(
            controllerSize / 2,
            strokeWidth + verticalSpacing,
            w - controllerSize / 2,
            h - strokeWidth - verticalSpacing
        )
        controllerPosition = (currentColorArray[2]) * bound.width() + bound.left
        setBoundPolygon()
        setupShader(bound)
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
        invalidate()
    }

    override fun setValue(value: Float) {
        currentColorArray[2] = value
        controllerPosition = (currentColorArray[2]) * bound.width() + bound.left
        setBoundPolygon()
    }

    fun setOnValueChange(onValueChange: (Float) -> Unit = {}) {
        this.onValueChange = onValueChange
    }

    private fun setupShader(bound: RectF) {
        val color = Color.HSVToColor(colorArray)
        shaderPaint.shader = LinearGradient(
            bound.left, bound.top, bound.right, bound.top,Color.BLACK,color,Shader.TileMode.CLAMP
        )
    }

    private fun changePosition(newPosition: Float) {
        controllerPosition = newPosition
        setBoundPolygon()
        currentColorArray[2] = (newPosition / bound.width())
        onValueChange(currentColorArray[2])
    }

    private fun setBoundPolygon() {
        polygonTop?.setBounds(
            (controllerPosition - controllerSize / 2).toInt(),
            0,
            (controllerPosition + controllerSize / 2).toInt(),
            controllerSize.toInt()
        )
        polygonBottom?.setBounds(
            (controllerPosition - controllerSize / 2).toInt(),
            (measuredHeight - controllerSize).toInt(),
            (controllerPosition + controllerSize / 2).toInt(),
            measuredHeight
        )
    }
}