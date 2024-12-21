package com.vm.backgroundremove.objectremove.ui.main.color_picker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
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
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class HSView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val cornerRadius = 8f.toDp()
    private val strokeWidth = 2f.toDp()
    private val colorArray = floatArrayOf(330f, 0.4f, 1f)
    private var radius = 0f

    private var hsControllerPoint = PointF(strokeWidth, strokeWidth)
    private val hsControllerRadius = 5f.toDp()
    private var hsBound = RectF()
    private var onColorChanged: (Int) -> Unit = {}
    private var vView: OnHSChange? = null

    private val shaderHSPaint = Paint()

    private val colorPaint = Paint().apply {
        color = Color.WHITE
    }
    private val backgroundPaint = Paint().apply {
        color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawHSView(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        radius = min(w, h) / 2f - hsControllerRadius
        hsBound = RectF(
            0f,
            0f,
            w.toFloat(),
            h.toFloat()
        )
        calculateControllerPoint(w.toFloat(), h.toFloat())
        vView?.onHSChange(colorArray[0], colorArray[1])
        vView?.setValue(colorArray[2])
        setUpHSShader()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x: Float
                val y: Float
                val distance = calculateDistance(event.x, event.y)
                if (distance < radius) {
                    x = event.x
                    y = event.y
                } else {
                    val dY = event.y - measuredHeight / 2f
                    val angle = acos(abs(dY) / distance)
                    x = getXPosition(angle, event.x)
                    y = getYPosition(angle, event.y)
                }
                val dY = event.y - measuredHeight / 2f
                val angle = acos(dY / distance)
                Log.v("tag111", "angle: ${Math.toDegrees(angle.toDouble())}")
                colorArray[0] = getHue(angle, event)
                colorArray[1] = getSaturation(distance)
                hsControllerPoint = PointF(x, y)
                vView?.onHSChange(colorArray[0], colorArray[1])
                updateNewColor()
                true
            }

            else -> super.onTouchEvent(event)
        }
    }

    private fun calculateControllerPoint(width: Float, height: Float) {
        val centerX = width / 2f
        val centerY = height / 2f
        val angle = colorArray[0]
        val distanceFromCenter = colorArray[1] * radius
        var x = measuredWidth / 2f
        var y = measuredHeight / 2f
        when (angle) {
            in 0f .. 90f -> {
                val angleInRadian = Math.toRadians(90.0 + angle)
                x = centerX + distanceFromCenter * sin(angleInRadian).toFloat()
                y = centerY - abs(distanceFromCenter * cos(angleInRadian).toFloat())
            }
            in 90f..180f -> {
                val angleInRadian = Math.toRadians(270.0 - angle)
                x = centerX - distanceFromCenter * sin(angleInRadian).toFloat().absoluteValue
                y = centerY - distanceFromCenter * cos(angleInRadian).toFloat().absoluteValue
            }
            in 180f..270f -> {
                val angleInRadian = Math.toRadians(270.0 - angle)
                x = centerX - distanceFromCenter * sin(angleInRadian).toFloat().absoluteValue
                y = centerY + distanceFromCenter * cos(angleInRadian).toFloat()
            }
            in 270f..360f -> {
                val angleInRadian = Math.toRadians(90.0 - (360 - angle))
                x = centerX + distanceFromCenter * sin(angleInRadian).toFloat()
                y = centerY + distanceFromCenter * cos(angleInRadian).toFloat()
            }
        }
        hsControllerPoint.x = x
        hsControllerPoint.y = y
    }

    private fun getXPosition(angle: Float, eventX: Float): Float {
        val distanceX = radius * sin(angle)
        return if (eventX >= measuredWidth / 2f) {
            measuredWidth / 2f + distanceX
        } else {
            measuredWidth / 2f - distanceX
        }
    }

    private fun getYPosition(angle: Float, eventY: Float): Float {
        val distanceY = radius * cos(angle)
        return if (eventY >= measuredHeight / 2f) {
            measuredHeight / 2f + distanceY
        } else {
            measuredHeight / 2f - distanceY
        }
    }

    private fun calculateDistance(x: Float, y: Float): Float {
        return sqrt((x - measuredWidth / 2f).pow(2) + (y - measuredHeight / 2f).pow(2))
    }

    fun setOnColorChange(onColorChanged: (Int) -> Unit = {}) {
        this.onColorChanged = onColorChanged
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
        onColorChanged(color)
        invalidate()
    }

    private fun getHue(angle: Float, event: MotionEvent): Float {
        val angleInDegrees = Math.toDegrees(angle.toDouble()).toFloat()
        val centerX = measuredWidth / 2f
        val centerY = measuredHeight / 2f
        var hue = if (event.x >= centerX && event.y >= centerY) {
            270f + angleInDegrees
        } else if (event.x <= centerX) {
            270f - angleInDegrees
        } else if (event.x >= centerX && event.y <= centerY) {
            90f - (180f - angleInDegrees)
        } else {
            90f - (180f - angleInDegrees)
        }
        if (hue == 360f) hue = 0f

        return hue
    }

    private fun getSaturation(distanceFromCenter: Float): Float {
        return min(distanceFromCenter / radius, 1f)
    }

    private fun setUpHSShader() {
        SweepGradient(
            measuredWidth / 2f, measuredHeight / 2f, intArrayOf(
                context.getColor(R.color.color_FF0000),
                context.getColor(R.color.color_FF8A00),
                context.getColor(R.color.color_FFE600),
                context.getColor(R.color.color_14FF00),
                context.getColor(R.color.color_00A3FF),
                context.getColor(R.color.color_0500FF),
                context.getColor(R.color.color_AD00FF),
                context.getColor(R.color.color_FF00C7),
            ), null
        )
        val colorGradient = SweepGradient(
            measuredWidth / 2f, measuredHeight / 2f, intArrayOf(
                context.getColor(R.color.color_FF0000),
                context.getColor(R.color.color_FF00C7),
                context.getColor(R.color.color_AD00FF),
                context.getColor(R.color.color_0500FF),
                context.getColor(R.color.color_00A3FF),
                context.getColor(R.color.color_14FF00),
                context.getColor(R.color.color_FFE600),
                context.getColor(R.color.color_FF8A00),
                context.getColor(R.color.color_FF0000),
            ), null
        )

        val lightnessGradient = RadialGradient(
            measuredWidth / 2f,
            measuredHeight / 2f,
            radius,
            intArrayOf(Color.TRANSPARENT, Color.WHITE),
            null,
            TileMode.CLAMP
        )
        shaderHSPaint.shader =
            ComposeShader(colorGradient, lightnessGradient, PorterDuff.Mode.MULTIPLY)
    }

    private fun drawHSView(canvas: Canvas) {
        canvas.drawCircle(
            measuredWidth / 2f,
            measuredHeight / 2f,
            radius,
            backgroundPaint
        )
        canvas.drawCircle(
            measuredWidth / 2f,
            measuredHeight / 2f,
            radius,
            shaderHSPaint
        )
        drawHSController(canvas)
    }

    private fun drawHSController(canvas: Canvas) {
        canvas.drawCircle(
            hsControllerPoint.x,
            hsControllerPoint.y,
            hsControllerRadius,
            colorPaint
        )
    }

    interface OnHSChange {
        fun onHSChange(hue: Float, saturation: Float)
        fun setValue(value: Float)
    }
}