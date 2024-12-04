package com.vm.backgroundremove.objectremove.ui.main.cropview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.min

class CustomCropView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var imageBitmap: Bitmap? = null

    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
        isDither = true
    }
    private val imageMatrix = Matrix()

    private var scaleFactor = 1.0f
    private var posX = 0f
    private var posY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private val bound = RectF()

    private val scaleDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = scaleFactor.coerceIn(0.5f, 3.0f)
                invalidate()
                return true
            }
        })

    fun setImageBitmap(bitmap: Bitmap) {
        this.imageBitmap = bitmap
        if (width > 0 && height > 0) {
            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()
            val bitmapWidth = bitmap.width.toFloat()
            val bitmapHeight = bitmap.height.toFloat()
            imageMatrix.reset()
            invalidate()
        } else {
            post { setImageBitmap(bitmap) }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        imageBitmap?.let {
            canvas.save()
            canvas.concat(imageMatrix)
            canvas.drawBitmap(it, imageMatrix, imagePaint)
            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                activePointerId = event.getPointerId(0)
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                val x = event.getX(pointerIndex)
                val y = event.getY(pointerIndex)
                if (event.pointerCount == 1) {
                    posX += x - lastTouchX
                    posY += y - lastTouchY
                }
                lastTouchX = x
                lastTouchY = y
                invalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    lastTouchX = event.getX(newPointerIndex)
                    lastTouchY = event.getY(newPointerIndex)
                    activePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }
}
