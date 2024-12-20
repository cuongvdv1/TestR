package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.a8_app_utils.parcelable
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.database.HistoryRepository
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import com.vm.backgroundremove.objectremove.dialog.DiscardChangesDialog
import com.vm.backgroundremove.objectremove.dialog.LoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ResultRemoveBackGroundActivity :
    BaseActivity<ActivityRemoveBackgroundBinding, RemoveBackGroundViewModel>() {
    private var historyModel: HistoryModel? = null
    private var type = ""
    private var processId: Long = -1
    private var checkChangedBG: Boolean = false
    private var isClicked: Boolean = false
    private var intent_from: String? = null
    private val dbHistoryRepository: HistoryRepository by inject()

    private lateinit var dialog: LoadingDialog
    private lateinit var dialogDisCardChanges: DiscardChangesDialog
    private var bitmap: Bitmap? = null
    override fun createBinding(): ActivityRemoveBackgroundBinding {
        return ActivityRemoveBackgroundBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value

    override fun initView() {
        super.initView()

        intent_from = intent.getStringExtra(Constants.INTENT_EDIT_FROM)

        if (intent_from.equals(Constants.INTENT_EDIT_FROM_HISTORY)) {
            binding.tvTitle.text = getString(R.string.edit_photo_s_background)
        } else {
            binding.tvTitle.text = getString(R.string.background_removal)
        }

        binding.cvRmvBg.saveStack()
        dialogDisCardChanges = DiscardChangesDialog(this)
        dialog = LoadingDialog(this)
        type = intent.getStringExtra(Constants.TYPE_HISTORY).toString()
        viewModel.color.observe(this) { color ->
            checkChangedBG = true
            binding.cvRmvBg.setBackgroundWithColor(color)
        }
        viewModel.backGround.observe(this) { backGround ->
            checkChangedBG = true
            Log.d("TAG_BG", "TEST UNDO REDO")
            binding.cvRmvBg.setBackgroundBitmap(backGround)
        }
        viewModel.startColor.observe(this) { startColor ->
            val endColor = viewModel.endColor.value
            if (endColor != null && startColor != null) {
                checkChangedBG = true
                binding.cvRmvBg.setBackgroundWithGradient(startColor, endColor)
            }
        }

        viewModel.endColor.observe(this) { endColor ->
            val startColor = viewModel.startColor.value
            if (startColor != null && endColor != null) {
                checkChangedBG = true
                binding.cvRmvBg.setBackgroundWithGradient(startColor, endColor)
            }
        }

        try {
            historyModel = intent.parcelable<HistoryModel>(Constants.INTENT_RESULT)
            Log.d("TAG_MODEL", "${historyModel?.id}")
            processId = historyModel?.id!!
            historyModel?.let {
                if (!it.imageResult.isNullOrEmpty()) {
                    Glide.with(this).asBitmap()
                        .load(it.imageResult)
                        .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap, transition: Transition<in Bitmap>?
                            ) {
                                bitmap = resource
                                binding.cvRmvBg.setBitmap(bitmap!!)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                }
            }
            binding.ivBeforeAfter.tap {
                val uriImage = Uri.parse(historyModel?.imageCreate.toString())
                binding.cvRmvBg.toggleImage(bitmap!!, uriImage)
            }
        } catch (_: Exception) {
        }

        // xet cac su kien click
        binding.ivBack.tap {
            if (!isClicked) {
                isClicked = true
                if (checkChangedBG) {
                    checkChangedBG = true
                    dialogDisCardChanges.show()
                    dialogDisCardChanges.binding.tvYes.tap {
                        finish()
                    }
                    dialogDisCardChanges.binding.tvCancel.tap {
                        dialogDisCardChanges.dismiss()
                    }
                } else {
                    finish()
                }
            }
            Handler().postDelayed({ isClicked = false }, 500)
        }

        updateButtonStates()

        binding.ivExport.tap {
            if (!isClicked) {
                isClicked = true
                val imageUrl = historyModel?.imageResult?.takeIf { it.isNotEmpty() }
                Log.d("TAG_URL", "imageUrl: $imageUrl")
                if (imageUrl != null) {
                    dialog.setOnDismissListener {
                        if (binding.cvRmvBg.hasBackgroundBitmap()) {
                            val bitmapWithBackground = Bitmap.createBitmap(binding.cvRmvBg.getBitmapWithBackground()) // Lấy bitmap từ drawing cache
                            val path = saveBitmapToCache(bitmapWithBackground)
                            historyModel?.imageResult = path
                            lifecycleScope.launch(Dispatchers.IO) {
                                historyModel?.id = 0
                                val count = dbHistoryRepository.getRowRemoveBGCount()
                                val rowCount = if (count > 0) count + 1 else 1
                                historyModel?.type = "remove_background_edit"
                                historyModel?.time = System.currentTimeMillis()
                                historyModel?.name = getString(R.string.remove_BG) + " $rowCount"
                                historyModel?.imageResult = path
                                historyModel?.let { it1 -> dbHistoryRepository.insertProcess(it1) }
                            }
                            val intent = Intent(
                                this@ResultRemoveBackGroundActivity,
                                DownloadRemoveBackgroundActivity::class.java
                            )
                            intent.putExtra(Constants.INTENT_RESULT, historyModel)
                            Log.d("TAG_HISTORY_MODEL", "historyModel: ${historyModel?.imageResult}")
                            startActivity(intent)

                        } else {
                            val intent = Intent(
                                this@ResultRemoveBackGroundActivity,
                                DownloadRemoveBackgroundActivity::class.java
                            )
                            intent.putExtra(Constants.INTENT_RESULT, historyModel)
                            startActivity(intent)
                            Log.d("TAG_SAVE", "SAVED IMAGE WITHOUT BACKGROUND")
                        }

                    }
                    dialog.showWithTimeout(3000)
                } else {
                    Log.d("TAG_IMAGE", "Image URL is null or empty")
                }
            }
            Handler().postDelayed({ isClicked = false }, 500)
        }
        // Tạo fragment
        val fragment = ChooseBackGroundColorFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()
    }

    override fun bindView() {
        binding.ivDone.tap {
            if (!isClicked) {
                isClicked = true
                supportFragmentManager.fragments.forEach { fragment ->
                    if (fragment is ChooseBackGroundColorFragment && fragment.isVisible) {
                        if (fragment.check_gradient) {
                            binding.cvRmvBg.saveStack()
                        } else if (fragment.check_single_color) {
                            fragment.customColor?.let { color ->
                                viewModel.setColor(color)
                                binding.cvRmvBg.setBackgroundWithColor(color)
                            }
                        }
                        viewModel.setStartColor(null)
                        viewModel.setEndColor(null)
                        fragment.showColorList()
                        setBeforeImage()
                    }
                }
            }
            Handler().postDelayed({ isClicked = false }, 500)
        }

        binding.ivUndo.tap {
            binding.cvRmvBg.undo()
        }

        binding.ivRedo.tap {
            binding.cvRmvBg.redo()
        }

        binding.ivCancel.tap {
            if (!isClicked) {
                supportFragmentManager.fragments.forEach {
                    if (it is ChooseBackGroundColorFragment && it.isVisible) {
                        it.showColorList()
                        setBeforeImage()
                        if (it.check_gradient) {
                            binding.cvRmvBg.applyCurrentConfig()
                        }
                        viewModel.setStartColor(null)
                        viewModel.setEndColor(null)
                    }
                }
            }
            Handler().postDelayed({ isClicked = false }, 500)
        }
    }

    fun setNewImage() {
        binding.ivBeforeAfter.visibility = View.GONE
        binding.ivDone.visibility = View.VISIBLE
        binding.ivRedo.visibility = View.GONE
        binding.ivUndo.visibility = View.GONE
        binding.ivCancel.visibility = View.VISIBLE
        binding.tvEdit.text = getString(R.string.color_ppicker)
    }

    fun setBeforeImage() {
        binding.ivBeforeAfter.visibility = View.VISIBLE
        binding.ivDone.visibility = View.GONE
        binding.ivRedo.visibility = View.VISIBLE
        binding.ivCancel.visibility = View.GONE
        binding.ivUndo.visibility = View.VISIBLE
        binding.tvEdit.text = getString(R.string.edit)
    }

    fun updateButtonStates() {
        binding.ivUndo.alpha = if (binding.cvRmvBg.canUndo()) 1.0f else 0.3f
        binding.ivRedo.alpha = if (binding.cvRmvBg.canRedo()) 1.0f else 0.3f

        binding.ivUndo.isEnabled = binding.cvRmvBg.canUndo()
        binding.ivRedo.isEnabled = binding.cvRmvBg.canRedo()
    }

    private fun downloadImageFromUrl(context: Context, imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Tải bitmap từ URL
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .submit()
                    .get()

                val outputStream: OutputStream?

                // Tạo tên tệp ngẫu nhiên
                val randomFileName = "Image_${System.currentTimeMillis()}.jpg"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10 trở lên: Lưu vào MediaStore
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, randomFileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    if (uri == null) {
                        throw Exception("Failed to create URI for saving the image")
                    }
                    outputStream = context.contentResolver.openOutputStream(uri)
                } else {
                    // Android 9 trở xuống: Lưu vào thư mục Pictures
                    val downloadDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs()
                    }
                    val file = File(downloadDir, randomFileName)
                    outputStream = FileOutputStream(file)

                    // Thêm vào MediaStore để hiển thị trong thư viện
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, randomFileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.DATA, file.absolutePath)
                    }
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                }

                // Lưu bitmap vào file
                outputStream?.let {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    it.close()
                }

                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Image downloaded successfully", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {

                }
            }
        }
    }

    fun saveBitmapToCache(bitmap: Bitmap): String {
        val cachePath = File(cacheDir, "images")
        cachePath.mkdirs() // Tạo thư mục nếu nó không tồn tại

        // Tạo tên file với timestamp
        val timestamp = System.currentTimeMillis()
        val fileName = "image_$timestamp.png"
        val filePath = "${cachePath.absolutePath}/$fileName"

        val stream = FileOutputStream(filePath)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush() // Đảm bảo ghi lại tất cả dữ liệu vào file
        stream.close()

        return filePath
    }

//    private fun downloadImage(bitmap: Bitmap) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val outputStream: OutputStream?
//                val randomFileName = "Image_${System.currentTimeMillis()}.jpg"
//                var savedImagePath: String? = null // Biến để lưu đường dẫn lưu ảnh
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    // Android 10 trở lên: Lưu vào MediaStore
//                    val contentValues = ContentValues().apply {
//                        put(MediaStore.Images.Media.DISPLAY_NAME, randomFileName)
//                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
//                    }
//                    val uri = contentResolver.insert(
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                        contentValues
//                    )
//                    if (uri == null) {
//                        throw Exception("Unable to create URI for saving the image")
//                    }
//                    outputStream = contentResolver.openOutputStream(uri)
//                    savedImagePath = uri.toString() // Lưu đường dẫn ảnh
//                } else {
//                    val downloadDir =
//                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                    if (!downloadDir.exists()) {
//                        downloadDir.mkdirs()
//                    }
//                    val file = File(downloadDir, randomFileName)
//                    outputStream = FileOutputStream(file)
//                    savedImagePath = file.absolutePath // Lưu đường dẫn ảnh
//                }
//                // Save Bitmap to file
//                outputStream?.let {
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
//                    it.close()
//                }
//                // Notify user and start DownloadRemoveBackgroundActivity
//                CoroutineScope(Dispatchers.Main).launch {
//                    Toast.makeText(
//                        this@ResultRemoveBackGroundActivity,
//                        "Image saved successfully",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    savedImagePath?.let { path ->
//                        val intent = Intent(
//                            this@ResultRemoveBackGroundActivity,
//                            DownloadRemoveBackgroundActivity::class.java
//                        )
//                        intent.putExtra(Constants.INTENT_IMG_RESULT_PATH, path) // Truyền đường dẫn ảnh
//
//                        startActivity(intent)
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                CoroutineScope(Dispatchers.Main).launch {
//                    Toast.makeText(
//                        this@ResultRemoveBackGroundActivity,
//                        "Failed to save image: ${e.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }
//    }
//
//
//    private fun saveImageWithBackground() {
//        binding.cvRmvBg.isDrawingCacheEnabled = true
//        val bitmapWithBackground = Bitmap.createBitmap(binding.cvRmvBg.drawingCache)
//        binding.cvRmvBg.isDrawingCacheEnabled = false
//        downloadImage(bitmapWithBackground)
//    }


    fun clearBackground() {
        binding.cvRmvBg.clearBackGround() // Gọi hàm clearBackground trong CropView
    }

}