package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.a8_app_utils.parcelable
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import com.vm.backgroundremove.objectremove.dialog.LoadingDialog
import com.vm.backgroundremove.objectremove.ui.main.cropview.CropView
import com.vm.backgroundremove.objectremove.ui.main.your_projects.YourProjectsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ResultRemoveBackGroundActivity :
    BaseActivity<ActivityRemoveBackgroundBinding, RemoveBackGroundViewModel>() {
    private var historyModel: HistoryModel? = null
    private var type = ""

    private lateinit var dialog: LoadingDialog
    private var bitmap: Bitmap? = null
    override fun createBinding(): ActivityRemoveBackgroundBinding {
        return ActivityRemoveBackgroundBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value

    override fun initView() {
        super.initView()
        dialog= LoadingDialog(this)
        type = intent.getStringExtra(Constants.TYPE_HISTORY).toString()
        viewModel.color.observe(this) { color ->
            binding.cvRmvBg.setBackgroundWithColor(color)
        }
        viewModel.backGround.observe(this) { backGround ->
            binding.cvRmvBg.setBackgroundBitmap(backGround)
        }
        viewModel.startColor.observe(this) { startColor ->
            viewModel.endColor.observe(this) { endColor ->
                binding.cvRmvBg.setBackgroundWithGradient(startColor, endColor)
            }
        }

        try {
            historyModel = intent.parcelable<HistoryModel>(Constants.INTENT_RESULT)
            Log.d("TAG_MODEL", "$historyModel")
            historyModel?.let {
                if (!it.imageResult.isNullOrEmpty()) {
                    Glide.with(this).asBitmap() // Chỉ định rằng bạn muốn tải Bitmap
                        .load(it.imageResult) // Tải hình ảnh từ file
                        .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap, transition: Transition<in Bitmap>?
                            ) {
                                // Đây là nơi bạn nhận được Bitmap
                                bitmap = resource
                                binding.cvRmvBg.setBitmap(bitmap!!)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // Xử lý khi cần thiết, không có gì đặc biệt trong trường hợp này
                            }
                        })
                }
            }
            binding.ivBeforeAfter.tap {
                Log.d("YEUTRINHLAMLUON", historyModel?.imageCreate.toString())
                val uriImage = Uri.parse(historyModel?.imageCreate.toString())
                binding.cvRmvBg.toggleImage(bitmap!!, uriImage)
            }
        } catch (_: Exception) {
        }
        // xet cac su kien click
        // xet su kien back man
        binding.ivBack.tap {
            finish()
        }

        binding.ivExport.tap {
            val imageUrl = historyModel?.imageResult?.takeIf { it.isNotEmpty() }
            if (imageUrl != null) {
//                dialog.show()
                dialog.setOnDismissListener {
                    saveImageWithBackground()
                }
                dialog.showWithTimeout(3000)
            } else {
                Log.d("TAG_IMAGE", "Image URL is null or empty")
            }
        }
        // Tạo fragment
        val fragment = ChooseBackGroundColorFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()
    }

    override fun bindView() {
        binding.ivDone.tap {
            supportFragmentManager.fragments.forEach { fragment ->
                if (fragment is ChooseBackGroundColorFragment && fragment.isVisible) {
                    if (fragment.check_gradient) {
                        val startColor = fragment.color_start ?: Color.TRANSPARENT
                        val endColor = fragment.color_end ?: Color.TRANSPARENT
                        binding.cvRmvBg.setBackgroundWithGradient(startColor, endColor)
                    } else if (fragment.check_single_color) {
                        fragment.customColor?.let { color ->
                            viewModel.setColor(color)
                            binding.cvRmvBg.setBackgroundWithColor(color)
                        }
                    }
                    fragment.showColorList()
                    setBeforeImage() // Cập nhật giao diện từng bước
                }
            }
        }

        binding.ivUndo.tap {

            binding.cvRmvBg.undo()
        }
        binding.ivRedo.tap {

            binding.cvRmvBg.redo()
        }
        binding.ivCancel.tap {
            supportFragmentManager.fragments.forEach {
                if (it is ChooseBackGroundColorFragment && it.isVisible) {
                    it.showColorList()
                    setBeforeImage()
                }
            }
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

    private fun downloadImage(bitmap: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val outputStream: OutputStream?
                val randomFileName = "Image_${System.currentTimeMillis()}.jpg"
                var savedImagePath: String? = null // Biến để lưu đường dẫn lưu ảnh

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10 trở lên: Lưu vào MediaStore
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, randomFileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val uri = contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    if (uri == null) {
                        throw Exception("Unable to create URI for saving the image")
                    }
                    outputStream = contentResolver.openOutputStream(uri)
                    savedImagePath = uri.toString() // Lưu đường dẫn ảnh
                } else {
                    // Android 9 trở xuống: Lưu vào thư mục Pictures
                    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs()
                    }
                    val file = File(downloadDir, randomFileName)
                    outputStream = FileOutputStream(file)
                    savedImagePath = file.absolutePath // Lưu đường dẫn ảnh
                }

                // Save Bitmap to file
                outputStream?.let {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    it.close()
                }

                // Notify user and start DownloadRemoveBackgroundActivity
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@ResultRemoveBackGroundActivity, "Image saved successfully", Toast.LENGTH_SHORT).show()
                    savedImagePath?.let { path ->
                        val intent = Intent(this@ResultRemoveBackGroundActivity, DownloadRemoveBackgroundActivity::class.java)
                        intent.putExtra(Constants.IMG_CAMERA_PATH, path) // Truyền đường dẫn ảnh
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@ResultRemoveBackGroundActivity, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveImageWithBackground() {
        binding.cvRmvBg.isDrawingCacheEnabled = true
        val bitmapWithBackground = Bitmap.createBitmap(binding.cvRmvBg.drawingCache)
        binding.cvRmvBg.isDrawingCacheEnabled = false
        downloadImage(bitmapWithBackground)
    }
    fun clearBackground() {
        binding.cvRmvBg.clearBackGround() // Gọi hàm clearBackground trong CropView
    }

}