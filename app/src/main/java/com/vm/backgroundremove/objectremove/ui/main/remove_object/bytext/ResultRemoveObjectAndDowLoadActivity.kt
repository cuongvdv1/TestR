package com.vm.backgroundremove.objectremove.ui.main.remove_object.bytext

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.a8_app_utils.parcelable
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityResultRemoveObjectAndDowLoadBinding
import com.vm.backgroundremove.objectremove.dialog.LoadingDialog
import com.vm.backgroundremove.objectremove.ui.main.remove_object.ResultRemoveObjectActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ResultRemoveObjectAndDowLoadActivity :
    BaseActivity<ActivityResultRemoveObjectAndDowLoadBinding, BaseViewModel>() {
    override fun createBinding(): ActivityResultRemoveObjectAndDowLoadBinding {
        return ActivityResultRemoveObjectAndDowLoadBinding.inflate(layoutInflater)
    }

    private var historyModel: HistoryModel? = null
    private var bitmap: Bitmap? = null
    private var type = ""
    private lateinit var dialog: LoadingDialog
    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()
        dialog = LoadingDialog(this@ResultRemoveObjectAndDowLoadActivity)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_rm_object_by_list_dowload, RemoveByTextResultFragment()).commit()

        binding.ivBack.tap {
            finish()
        }

        type = intent.getStringExtra(Constants.TYPE_HISTORY).toString()
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

                                 bitmap = resource
                                binding.ivRmvObject.setImageFromBitmap(bitmap!!)

                            }

                            override fun onLoadCleared(placeholder: Drawable?) {

                            }
                        })
                }
                binding.ivBeforeAfter.tap {
                    Log.d("YEUTRINHLAMLUON",  historyModel?.imageCreate.toString())
                    val uriImage = Uri.parse( historyModel?.imageCreate.toString())
                    binding.ivRmvObject.toggleImage(bitmap!!, uriImage)
                }
            }
        } catch (_: Exception) {
        }

        binding.ivExport.tap {
            val imageUrl = historyModel?.imageResult?.takeIf { it.isNotEmpty() }

            if (imageUrl != null) {
                downloadImageFromUrl(this, imageUrl)
                val intent = Intent(this, ResultRemoveObjectActivity::class.java)
                intent.putExtra(Constants.INTENT_RESULT,historyModel)
                dialog.show()
                dialog.showWithTimeout(3000)
                dialog.dismiss()
                startActivity(intent)
                finish()
            }
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

                CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Image downloaded successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {

                }
            }
        }
    }
}