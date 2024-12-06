package com.vm.backgroundremove.objectremove.ui.main.your_projects

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityHistoryBinding
import com.vm.backgroundremove.objectremove.databinding.PopupOptionHistoryBinding
import com.vm.backgroundremove.objectremove.ui.common.setting.SettingActivity
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class YourProjectsActivity : BaseActivity<ActivityHistoryBinding, BaseViewModel>() {
    private var imageUrl = ""
    private var type = ""

    override fun createBinding() = ActivityHistoryBinding.inflate(layoutInflater)

    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        super.initView()
        try {
            // Gắn link ảnh cố định
            val fixedImageUrl =
                "https://aphoto.vn/wp-content/uploads/2020/04/anh-dep-jpg-fujifilm-2.jpg" // Thay bằng URL ảnh cứng
            Glide.with(this@YourProjectsActivity)
                .load(fixedImageUrl)
                .into(binding.imgResult)

            imageUrl = fixedImageUrl // Gắn giá trị cho biến imageUrl
        } catch (_: Exception) {
        }
    }


    override fun bindView() {
        super.bindView()
        binding.clSuccessfully.tap {
            binding.clSuccessfully.visibility = View.GONE
        }
        binding.icMoreOptions.setOnClickListener {
            showCustomMenu(it)
        }
        binding.ctlHome.tap {
            val intent = Intent(this,HomeActivity::class.java)
            startActivity(intent)
        }
        binding.ctlSetting.tap {
            val intent = Intent(this,SettingActivity::class.java)
            startActivity(intent)
        }
        binding.ctlYourProjects.tap {
            val intent = Intent(this,YourProjectsResultActivity::class.java)
            startActivity(intent)
        }


    }


    @SuppressLint("InflateParams")
    private fun showCustomMenu(view: View) {
        val bindingPopup = PopupOptionHistoryBinding.inflate(LayoutInflater.from(this))
        val inflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.popup_option_history, null)
        val popupWindow = PopupWindow(
            bindingPopup.root,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Thiết lập sự kiện cho từng item
        bindingPopup.clDownload.tap {

            downloadImageFromUrl(this@YourProjectsActivity, imageUrl)
            Toast.makeText(
                this@YourProjectsActivity,
                getString(R.string.home), Toast.LENGTH_SHORT
            )
                .show()



            popupWindow.dismiss()
        }

        bindingPopup.clShare.tap {
            shareImage(imageUrl)
            popupWindow.dismiss()
        }


        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = false

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth
        val popupHeight = popupView.measuredHeight

        popupWindow.width = popupWidth
        popupWindow.height = popupHeight

        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0] + view.width
        val y = location[1] - popupHeight

        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x, y)
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
                    showAndHideView(binding.clSuccessfully)
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

    private fun showAndHideView(view: View, duration: Long = 5000) {
        view.visibility = View.VISIBLE // Hiển thị view

        view.postDelayed({
            view.visibility = View.GONE
        }, duration)
    }

    private fun shareImage(url: String) {
        // Tải hình ảnh từ URL và lưu tạm vào cache
        Glide.with(this)
            .asFile()
            .load(url)
            .into(object : CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    // Chia sẻ file ảnh sau khi tải về thành công
                    shareFile(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Xử lý nếu tải hình thất bại
                }
            })
    }


    private fun shareFile(imageFile: File) {
        // Tạo URI từ File thông qua FileProvider
        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.provider", // Thay đổi theo package name
            imageFile
        )

        // Tạo Intent chia sẻ
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Cho phép quyền đọc file
        }

        // Hiển thị dialog chọn ứng dụng
        startActivity(Intent.createChooser(shareIntent, "Share image via"))
    }


}