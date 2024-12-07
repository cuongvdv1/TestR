package com.vm.backgroundremove.objectremove.ui.main.progress

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.vm.backgroundremove.objectremove.R


class SeekbarProgress @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintTrack = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintIndicator = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    var progress = 0f
        set(value) {
            field = value.coerceIn(0f, 100f) // Giới hạn giá trị từ 0 đến 100
            invalidate()
        }

    var trackColor: Int = ContextCompat.getColor(context, R.color.trackColor)
        set(value) {
            field = value
            paintTrack.color = value
            invalidate()
        }

    var indicatorColor: Int = ContextCompat.getColor(context, R.color.color_FFA637)
        set(value) {
            field = value
            invalidateShader()
            invalidate()
        }

    var endColor: Int = ContextCompat.getColor(context, R.color.color_FFA637)
        set(value) {
            field = value
            invalidateShader()
            invalidate()
        }

    var cornerRadius: Float = 30f // Bo góc mặc định
        set(value) {
            field = value
            invalidate()
        }

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomLinearProgressView,
            0, 0
        )

        try {
            progress = a.getFloat(R.styleable.CustomLinearProgressView_progressProgress, 0f)
            trackColor = a.getColor(R.styleable.CustomLinearProgressView_trackColorProgress, trackColor)
            indicatorColor =
                a.getColor(R.styleable.CustomLinearProgressView_indicatorColorProgress, indicatorColor)
            endColor = a.getColor(R.styleable.CustomLinearProgressView_endColorProgress, endColor)
            cornerRadius = a.getDimension(R.styleable.CustomLinearProgressView_indicatorRadius, 20f)
        } finally {
            a.recycle()
        }

        paintTrack.color = trackColor
        invalidateShader()
    }

    private fun invalidateShader() {
        val gradient = LinearGradient(
            0f, 0f, width.toFloat(), 0f, // Gradient theo chiều ngang
            intArrayOf(indicatorColor, endColor),
            null,
            Shader.TileMode.CLAMP
        )
        paintIndicator.shader = gradient
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidateShader()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val trackHeight = height.toFloat()

        // Vẽ track (nền)
        val trackRect = RectF(0f, 0f, width.toFloat(), trackHeight)
        canvas.drawRoundRect(trackRect, cornerRadius, cornerRadius, paintTrack)


        // Vẽ thanh tiến trình
        val progressWidth = width * (progress / 100) // Tính độ dài thanh dựa trên progress
        val progressRect = RectF(0f, 0f, progressWidth, trackHeight)
        canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, paintIndicator)
    }
}

