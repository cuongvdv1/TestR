package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.vm.backgroundremove.objectremove.MainActivity
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveObjectBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.ChooseBackGroundColorFragment
import com.vm.backgroundremove.objectremove.util.getBitmapFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class RemoveObjectActivity : BaseActivity<ActivityRemoveObjectBinding, BaseViewModel>(){
    override fun createBinding() = ActivityRemoveObjectBinding.inflate(layoutInflater)
    override fun setViewModel() = BaseViewModel()


    override fun initView() {
        super.initView()

        val imgPathGallery = intent.getStringExtra(Constants.IMG_GALLERY_PATH)
        val imagePathCamera = intent.getStringExtra(Constants.IMG_CAMERA_PATH)

        val filePath = intent.getStringExtra(Constants.IMG_CATEGORY_PATH)
        Log.d("TAG123", "filePath: $filePath")
        if (!imagePathCamera.isNullOrEmpty()) {
//            Glide.with(this)
//                .load(File(imagePathCamera))
//                .skipMemoryCache(true)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into(binding.ivRmvBg)
            getBitmapFrom(this, imagePathCamera) {
                binding.ivRmvBg.setBitmap(it)
            }

        } else if (!imgPathGallery.isNullOrEmpty()) {
//            Glide.with(this)
//                .load(imgPathGallery)
//                .into(binding.ivRmvBg)
            getBitmapFrom(this, imgPathGallery) {
                binding.ivRmvBg.setBitmap(it)
            }
        }

        val fragment = RemoveObjectFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()


        binding.ivExport.tap {
            val imageUrl = when {
                !imagePathCamera.isNullOrEmpty() -> File(imagePathCamera).absolutePath
                !imgPathGallery.isNullOrEmpty() -> imgPathGallery
                else -> null
            }

            if (imageUrl != null) {
                downloadImageFromUrl(this, imageUrl)
            } else {
                Toast.makeText(this, "Không có hình ảnh để tải xuống", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivBack.tap {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

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

                // Hiển thị thông báo
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        context,
                        "Lưu hình ảnh thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        context,
                        "Không thể lưu hình ảnh: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}