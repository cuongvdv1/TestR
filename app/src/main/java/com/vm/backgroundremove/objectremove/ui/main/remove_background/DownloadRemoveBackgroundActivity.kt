package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
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
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityYourProjectsResultBinding
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class DownloadRemoveBackgroundActivity:BaseActivity<ActivityYourProjectsResultBinding, BaseViewModel>() {
    private var imageUrl = ""
    private var historyModel : HistoryModel? = null
    override fun createBinding(): ActivityYourProjectsResultBinding {
        return ActivityYourProjectsResultBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()
        imageUrl = intent.getStringExtra(Constants.IMG_CAMERA_PATH) ?: ""
        historyModel = intent.getParcelableExtra(Constants.INTENT_RESULT)

        if (imageUrl.isEmpty().not()) {
            binding.tvTitleName.visibility = View.GONE
            binding.tvSaved.visibility = View.VISIBLE
            binding.ivHome.visibility = View.VISIBLE
            binding.llBtnShare.visibility = View.GONE
            binding.llBtnExport.visibility = View.GONE
            Log.d("TAG_IMAGE", "imageUrl: $imageUrl")
            Glide.with(this)
                .load(imageUrl)
                .into(binding.ivHistoryResult) // Assume you have an ImageView in your layout
        }else{
            Log.d("TAG_IMAGE", "imageUrl1111: $imageUrl")
            Log.d("TAG_IMAGE", "historyModel: ${historyModel?.imageResult}")
            Glide.with(this)
                .load(historyModel?.imageResult)
                .into(binding.ivHistoryResult) // Assume you have an ImageView in your layout
        }

        binding.llBtnExport.tap {
            Glide.with(this)
                .asBitmap()
                .load(historyModel?.imageResult)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        // Chia sẻ file ảnh sau khi tải về thành công
                        saveFileToDownload(resource)
                        Toast.makeText(this@DownloadRemoveBackgroundActivity, "Image saved successfully", Toast.LENGTH_SHORT).show()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Log.d("TAG_IMAGE", "onLoadCleared: ")
                    }
                })


        }
        binding.llBtnShare.tap {
            shareImageFromCache()
            Log.d("TAG_IMAGE", "shareImage: shared")
        }
        binding.llShareWithFriend.tap {
            shareImageFromCache()
        }

        binding.ivBack.tap {
            finish()
        }
        binding.ivHome.tap {
            val intent = Intent(this@DownloadRemoveBackgroundActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }


    //    private fun shareImage(url: String) {
//        // Tải hình ảnh từ URL và lưu tạm vào cache
//        Glide.with(this)
//            .asFile()
//            .load(url)
//            .into(object : CustomTarget<File>() {
//                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
//                    // Chia sẻ file ảnh sau khi tải về thành công
//                    shareFile(resource)
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {
//                   Log.d("TAG_IMAGE", "onLoadCleared: ")
//                }
//            })
//    }
//
//    private fun shareFile(imageFile: File) {
//        // Tạo URI từ File thông qua FileProvider
//        val uri = FileProvider.getUriForFile(
//            this,
//            "$packageName.provider", // Thay đổi theo package name
//            imageFile
//        )
//
//        // Tạo Intent chia sẻ
//        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//            type = "image/*"
//            putExtra(Intent.EXTRA_STREAM, uri)
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Cho phép quyền đọc file
//        }
//
//        // Hiển thị dialog chọn ứng dụng
//        startActivity(Intent.createChooser(shareIntent, "Share image via"))
//    }
    private fun shareImageFromCache() {
        try {
            val imageUrl = historyModel?.imageResult
            imageUrl?.let { url ->
                Glide.with(this)
                    .asBitmap()
                    .load(url)
                    .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {
                            try {
                                val filePath = createDownloadFile(this@DownloadRemoveBackgroundActivity)
                                val file = File(filePath)

                                val fos = FileOutputStream(file)
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                                fos.close()

                                val imageUri = FileProvider.getUriForFile(
                                    this@DownloadRemoveBackgroundActivity,
                                    "$packageName.provider",
                                    file
                                )

                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, imageUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        getString(R.string.share)
                                    )
                                )
                            } catch (e: Exception) {
                                Log.d("share", e.message.toString())
                                e.printStackTrace()
                            }
                        }

                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {

                        }
                    })
            }
        }catch (_:Exception){}
    }

    fun createDownloadFile(context: Context): String {
        val cacheDir = File(context.cacheDir, "Stylist")

        val fileName = "Stylist${System.currentTimeMillis()}.png"

        val cacheFile = File(cacheDir, fileName)

        try {
            if (!cacheFile.exists()) {
                cacheDir.mkdirs()
                cacheFile.createNewFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return cacheFile.absolutePath
    }

    private fun saveFileToDownload(bitmap: Bitmap) {
        val resolver = contentResolver
        var name = System.currentTimeMillis().toString()
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/removebg")
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        Log.v("tag111", "gen success: $name")
    }


}