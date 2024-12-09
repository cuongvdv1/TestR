package com.vm.backgroundremove.objectremove.ui.main.your_projects

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityYourProjectsResultBinding
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class YourProjectsResultActivity :
    BaseActivity<ActivityYourProjectsResultBinding, BaseViewModel>() {
    private var imageUrl = ""

    override fun createBinding() = ActivityYourProjectsResultBinding.inflate(layoutInflater)
    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        super.initView()
        try {
            // Gắn link ảnh cố định
            val fixedImageUrl =
                "https://aphoto.vn/wp-content/uploads/2020/04/anh-dep-jpg-fujifilm-2.jpg" // Thay bằng URL ảnh cứng
            Glide.with(this@YourProjectsResultActivity)
                .load(fixedImageUrl)
                .into(binding.ivHistoryResult)

            imageUrl = fixedImageUrl // Gắn giá trị cho biến imageUrl
        } catch (_: Exception) {
        }
    }


    override fun bindView() {
        super.bindView()

        binding.llBtnExport.setOnClickListener {
            downloadImageFromUrl(this@YourProjectsResultActivity, imageUrl)

        }

        binding.llBtnShare.setOnClickListener {
        }

        binding.ivBack.tap {
            finish()
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