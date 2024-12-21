package com.vm.backgroundremove.objectremove.ui.main.your_projects

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
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
import com.vm.backgroundremove.objectremove.databinding.ActivityHistoryResultBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.ResultRemoveBackGroundActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class HistoryResultActivity : BaseActivity<ActivityHistoryResultBinding, BaseViewModel>() {

    private var historyModel: HistoryModel? = null
    private var isClickable : Boolean = true

    override fun createBinding(): ActivityHistoryResultBinding {
        return ActivityHistoryResultBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()

        historyModel = intent.parcelable<HistoryModel>(Constants.INTENT_RESULT)

        binding.tvTitleName.text = historyModel?.name
        if(historyModel?.type == "remove_background_done" || historyModel?.type == "remove_background"){
            binding.ivHistory.visibility = View.VISIBLE
        }

        binding.ivHistory.tap {
            if(isClickable){
                isClickable = false
                Glide.with(this)
                    .asBitmap()
                    .load(historyModel?.imageResult)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            saveFileToDownload(resource)
                            Toast.makeText(
                                this@HistoryResultActivity,
                                getString(R.string.image_saved_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
                Handler().postDelayed({ isClickable = true }, 500)
            }

        }

        // hien thi anh len tren ivHistoryResult
        historyModel?.let {
            if (!it.imageResult.isNullOrEmpty()) {
                if (it.type == "remove_background_done" || it.type =="remove_background") {
                    Glide.with(this).asBitmap()
                        .load(historyModel?.imageResult)
                        .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(binding.ivHistoryResultCrop)
                } else {

                    binding.ivHistoryResultCrop.visibility = View.INVISIBLE
                    binding.ivHistoryResultBrush.visibility = View.VISIBLE
                    binding.tvOption1.text = getString(R.string.export)
                    binding.ivOption1.setImageResource(R.drawable.ic_export_history)

                    Glide.with(this).asBitmap()
                        .load(historyModel?.imageResult)
                        .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                binding.ivHistoryResultBrush.setImageFromBitmap(resource)
                                Log.d("TAG_IMAGE_RESULT2", "initView: ${it.type}")
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                }
            }
        }
        //
        binding.llOption1.tap {
            if(isClickable){
                isClickable = false
                if (historyModel?.type == "remove_background_done" || historyModel?.type =="remove_background") {
                    val intent =
                        Intent(this@HistoryResultActivity, ResultRemoveBackGroundActivity::class.java)
                    intent.putExtra(Constants.INTENT_RESULT, historyModel)
                    intent.putExtra(Constants.INTENT_EDIT_FROM, Constants.INTENT_EDIT_FROM_HISTORY)
                    startActivity(intent)
                } else {
                    Glide.with(this)
                        .asBitmap()
                        .load(historyModel?.imageResult)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                saveFileToDownload(resource)
                                Toast.makeText(
                                    this@HistoryResultActivity,
                                    R.string.image_saved_successfully,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })

                }
                Handler().postDelayed({ isClickable = true }, 500)
            }

        }

        binding.ivBack.tap {
            finish()
        }

        binding.llShare.tap {
            if(isClickable){
                isClickable = false
                shareImageFromCache(historyModel?.imageResult.toString())
                Handler().postDelayed({isClickable = true}, 500)
            }

        }

    }

    private fun shareImageFromCache(imageUrl: String) {
        try {
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
                                val filePath =
                                    createDownloadFile(this@HistoryResultActivity)
                                val file = File(filePath)

                                val fos = FileOutputStream(file)
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                                fos.close()

                                val imageUri = FileProvider.getUriForFile(
                                    this@HistoryResultActivity,
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
        } catch (_: Exception) {
        }
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



}
