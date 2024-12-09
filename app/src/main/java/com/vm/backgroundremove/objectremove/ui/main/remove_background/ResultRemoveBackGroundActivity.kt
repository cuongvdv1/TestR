package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
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
    override fun createBinding(): ActivityRemoveBackgroundBinding {
        return ActivityRemoveBackgroundBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value

    override fun initView() {
        super.initView()
        type = intent.getStringExtra(Constants.TYPE_HISTORY).toString()
        val imagePathCamera = intent.getStringExtra(Constants.IMG_CAMERA_PATH)
        viewModel.color.observe(this) { color ->
            binding.ivRmvBg.setBackgroundWithColor(color)
        }
        viewModel.backGround.observe(this) { backGround ->
            binding.ivRmvBg.setBackgroundBitmap(backGround)
        }
        viewModel.startColor.observe(this) { startColor ->
            viewModel.endColor.observe(this) { endColor ->
                binding.ivRmvBg.setBackgroundWithGradient(startColor, endColor)
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
                                val bitmap: Bitmap = resource
                                binding.ivRmvBg.setBitmap(bitmap)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // Xử lý khi cần thiết, không có gì đặc biệt trong trường hợp này
                            }
                        })
                }
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
                saveImageWithBackground()
//                val intent = Intent(this@ResultRemoveBackGroundActivity, DownloadRemoveBackgroundActivity::class.java)
////                intent.putExtra(Constants.IMG_CAMERA_PATH, imageUrl) // Truyền đường dẫn ảnh
//                startActivity(intent)
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
        binding.ivBeforeAfter.tap {
            supportFragmentManager.fragments.forEach {
                if (it is ChooseBackGroundColorFragment && it.isVisible) {
                    setBackgroundColor(it)
                    it.showColorList()
                }
            }
        }
    }

    private fun setBackgroundColor(fragment: ChooseBackGroundColorFragment) {
        fragment.customColor?.let { viewModel.setColor(it) }
    }

    fun setNewImage() {
        binding.ivBeforeAfter.setImageResource(R.drawable.ic_selected_color)
        binding.ivRedo.visibility = View.GONE
        binding.ivUndo.setImageResource(R.drawable.ic_cancel)
        binding.tvEdit.text = getString(R.string.color_ppicker)
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
        binding.ivRmvBg.isDrawingCacheEnabled = true
        val bitmapWithBackground = Bitmap.createBitmap(binding.ivRmvBg.drawingCache)
        binding.ivRmvBg.isDrawingCacheEnabled = false // Tắt chế độ vẽ để giải phóng bộ nhớ

        // Lưu bitmap này vào thư viện như đã làm trước đó
        downloadImage(bitmapWithBackground)
    }


}