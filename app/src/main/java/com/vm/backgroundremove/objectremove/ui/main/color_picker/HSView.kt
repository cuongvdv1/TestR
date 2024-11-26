package com.vm.backgroundremove.objectremove.ui.main.color_picker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader.TileMode
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a8_app_utils.toDp

class HSView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val radius = 8f.toDp()
    private val strokeWidth = 2f.toDp()
    private val colorArray = floatArrayOf(180f, 0.5f, 1f)

    private var hsControllerPoint = PointF(strokeWidth, strokeWidth)
    private var hsControllerRadius = 0f
    private var hsBound = RectF()
    private var onColorChanged: (Int) -> Unit = {}
    private var vView: OnHSChange? = null

    private val shaderHSPaint = Paint()

    private val colorPaint = Paint().apply {
        color = context.getColor(R.color.white)
    }
    private val backgroundPaint = Paint().apply {
        color = Color.WHITE
    }
    private val strokePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = this@HSView.strokeWidth
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawHSView(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        hsBound = RectF(
            0f,
            0f,
            w.toFloat(),
            h.toFloat()
        )
        hsControllerRadius = (hsBound.height() * 0.307f) / 2f
        val x = colorArray[0] / (360f / w)
        val y = (1f - colorArray[1]) * h
        hsControllerPoint = PointF(x, y)
        vView?.onHSChange(colorArray[0], colorArray[1])
        vView?.setValue(colorArray[2])
        setUpHSShader()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                var x = hsControllerPoint.x
                var y = hsControllerPoint.y
                if (event.x >= hsBound.left && event.x <= hsBound.right) {
                    x = event.x
                    colorArray[0] = getHue(event)
                }
                if (event.y >= hsBound.top && event.y <= hsBound.bottom) {
                    y = event.y
                    colorArray[1] = getSaturation(event)
                }
                hsControllerPoint = PointF(x, y)
                vView?.onHSChange(colorArray[0], colorArray[1])
                updateNewColor()
                true
            }

            else -> super.onTouchEvent(event)
        }
    }

    fun setOnColorChange(onColorChanged: (Int) -> Unit = {}) {
        this.onColorChanged = onColorChanged
    }

    fun setColor(color: Int) {
        Color.colorToHSV(color, colorArray)
        hsControllerPoint.x = colorArray[0] / (360f / measuredWidth)
        hsControllerPoint.y = (1f - colorArray[1]) * measuredHeight
        colorPaint.color = color
        invalidate()
    }

    fun setValue(value: Float) {
        colorArray[2] = value
        updateNewColor()
    }

    fun setupWith(vView: OnHSChange) {
        this.vView = vView
    }

    private fun updateNewColor() {
        val color = Color.HSVToColor(colorArray)
        colorPaint.color = color
        onColorChanged(color)
        invalidate()
    }

    private fun getHue(event: MotionEvent): Float {
        var x = event.x
        if (x < 0f) x = 0f
        if (x > measuredWidth) x = measuredWidth.toFloat()
        var hue = (360f / measuredWidth) * x
        if (hue == 360f) hue = 0f

        return hue
    }

    private fun getSaturation(event: MotionEvent): Float {
        return 1f - (event.y / measuredHeight)
    }

    private fun setUpHSShader() {
        val colorGradient = LinearGradient(
            hsBound.left,
            hsBound.top,
            hsBound.right,
            hsBound.top,
            intArrayOf(
                context.getColor(R.color.color_FF0000),
                context.getColor(R.color.color_FF8A00),
                context.getColor(R.color.color_FFE600),
                context.getColor(R.color.color_14FF00),
                context.getColor(R.color.color_00A3FF),
                context.getColor(R.color.color_0500FF),
                context.getColor(R.color.color_AD00FF),
                context.getColor(R.color.color_FF00C7),
                context.getColor(R.color.color_FF0000),
            ), null, TileMode.CLAMP
        )

        val lightnessGradient = LinearGradient(
            hsBound.left,
            hsBound.top,
            hsBound.left,
            hsBound.bottom,
            Color.WHITE,
            Color.TRANSPARENT,
            TileMode.CLAMP
        )
        shaderHSPaint.shader =
            ComposeShader(colorGradient, lightnessGradient, PorterDuff.Mode.MULTIPLY)
    }
    private fun drawHSView(canvas: Canvas) {
        canvas.drawRoundRect(
            0f,
            0f,
            measuredWidth.toFloat(),
            measuredHeight.toFloat(),
            radius,
            radius,
            backgroundPaint
        )
        canvas.drawRoundRect(
            hsBound.left,
            hsBound.top,
            hsBound.right,
            hsBound.bottom,
            radius,
            radius,
            shaderHSPaint
        )
        drawHSController(canvas)
        canvas.drawRoundRect(
            0f,
            0f,
            measuredWidth.toFloat(),
            measuredHeight.toFloat(),
            radius,
            radius,
            strokePaint
        )
    }

    private fun drawHSController(canvas: Canvas) {
        canvas.drawCircle(
            hsControllerPoint.x,
            hsControllerPoint.y,
            hsControllerRadius - 12f.toDp(),
            backgroundPaint
        )
        canvas.drawCircle(
            hsControllerPoint.x,
            hsControllerPoint.y,
            hsControllerRadius - 12f.toDp(),
            colorPaint
        )
    }

    interface OnHSChange {
        fun onHSChange(hue: Float, saturation: Float)
        fun setValue(value: Float)
    }
}
