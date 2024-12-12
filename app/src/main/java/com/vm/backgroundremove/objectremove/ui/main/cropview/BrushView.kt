package com.vm.backgroundremove.objectremove.ui.main.cropview
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withSave
import com.vm.backgroundremove.objectremove.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class BrushView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val drawPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 10f
    }

    private val paths = mutableListOf<Path>()
    private val undonePaths = mutableListOf<Path>()

    private val drawPath = Path()
    private var canvasBitmap: Bitmap? = null
    private var canvasImage: Bitmap? = null
    private var canvas: Canvas? = null

    private var isShowingBitmap1 = true


    private var isDrawingEnabled = true

    fun setDrawingEnabled(enabled: Boolean) {
        isDrawingEnabled = enabled
    }

    init {
        isDrawingCacheEnabled = true
    }

    fun setImageFromUrl(imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(imageUrl)
                val bitmap = BitmapFactory.decodeStream(url.openStream())
                canvasImage = bitmap
                postInvalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setImageFromBitmap(bitmap: Bitmap) {
        canvasImage = bitmap
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        drawPaint.strokeWidth = width
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            undonePaths.add(paths.removeAt(paths.size - 1))
            invalidate()
        }
    }

    fun redo() {
        if (undonePaths.isNotEmpty()) {
            paths.add(undonePaths.removeAt(undonePaths.size - 1))
            invalidate()
        }
    }

    fun clearAll() {
        paths.clear()
        undonePaths.clear()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#DFDFDF"))
        canvasImage?.let {
            // Tính toán tỷ lệ để đảm bảo ảnh vừa với View theo chiều dọc
            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()
            val imageWidth = it.width.toFloat()
            val imageHeight = it.height.toFloat()

            val scale = if (imageWidth / imageHeight > viewWidth / viewHeight) {
                // Ảnh rộng hơn, căn theo chiều ngang
                viewWidth / imageWidth
            } else {
                // Ảnh cao hơn, căn theo chiều dọc
                viewHeight / imageHeight
            }

            val scaledWidth = imageWidth * scale
            val scaledHeight = imageHeight * scale

            val left = (viewWidth - scaledWidth) / 2
            val top = (viewHeight - scaledHeight) / 2
            val right = left + scaledWidth
            val bottom = top + scaledHeight

            val srcRect = Rect(0, 0, it.width, it.height)
            val destRect = RectF(left, top, right, bottom)

            canvas.drawBitmap(it, srcRect, destRect, null)
        }


        canvasBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

        for (path in paths) {
            canvas.drawPath(path, drawPaint)
        }
        canvas.drawPath(drawPath, drawPaint)
    }
    fun setImageFromPath(path: String) {
        val bitmap = BitmapFactory.decodeFile(path)
        canvasImage = bitmap
        invalidate()
    }



    fun toggleImage(bitmap: Bitmap, uri: Uri) {
        if (isShowingBitmap1) {
            // Hiển thị ảnh từ URI
            setImageFromUri(uri)
        } else {
            // Hiển thị ảnh từ bitmap
            setImageFromBitmap(bitmap)
        }
        // Đảo trạng thái
        isShowingBitmap1 = !isShowingBitmap1
    }

    fun setImageFromUri(uri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                canvasImage = bitmap
                invalidate()
            } else {
                Log.e("setImageFromUri", "Bitmap is null for URI: $uri")
            }
        } catch (e: Exception) {
            Log.e("setImageFromUri", "Error loading image from URI: $uri", e)
        }
    }



}
