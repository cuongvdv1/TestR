package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundProcessBinding
import com.vm.backgroundremove.objectremove.databinding.ActivityRemovebackgroundSaveBinding
import com.vm.backgroundremove.objectremove.databinding.ActivityYourProjectsResultBinding
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.ProjectsActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.YourProjectsActivity
import java.io.File
import java.io.FileOutputStream

class DownloadRemoveBackgroundActivity :
    BaseActivity<ActivityRemovebackgroundSaveBinding, BaseViewModel>() {
    private var imageUrl :String? = null
    private var isClickable = true
    private var historyModel: HistoryModel? = null
    override fun createBinding(): ActivityRemovebackgroundSaveBinding {
        return ActivityRemovebackgroundSaveBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()
        imageUrl = intent.getStringExtra(Constants.INTENT_IMG_RESULT_PATH) ?: ""
        Log.d("TAG_IMAGE_INTENT", "imageUrl: $imageUrl")
        historyModel = intent.parcelable<HistoryModel>(Constants.INTENT_RESULT)

        if (!imageUrl.isNullOrEmpty()) {
            Log.d("TAG_IMAGE_INTENT_2", "imageUrl: $imageUrl")
            Glide.with(this).asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap>?
                    ) {
                        binding.ivHistoryResult.setImageFromBitmap(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } else {
            Glide.with(this).asBitmap()
                .load(historyModel?.imageResult)
                .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(object :CustomTarget<Bitmap>(){
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        binding.ivHistoryResult.setImageFromBitmap(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }

        binding.llSaveToDevice.tap {
            if (isClickable) {
                isClickable = false // Vô hiệu hóa click tiếp theo
                Glide.with(this)
                    .asBitmap()
                    .load(historyModel?.imageResult)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            // Chia sẻ file ảnh sau khi tải về thành công
                            saveFileToDownload(resource)
                            Toast.makeText(
                                this@DownloadRemoveBackgroundActivity,
                                "Image saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            Log.d("TAG_IMAGE", "onLoadCleared: ")
                        }
                    })

                // Khôi phục trạng thái sau một khoảng thời gian (nếu cần)
                binding.llShareWithFriends.postDelayed({ isClickable = true }, 500)
                binding.llSaveToDevice.postDelayed({isClickable = true},500)
            }


        }
        binding.llShareWithFriends.tap {
            if (isClickable) {
                isClickable = false
                shareImageFromCache(historyModel?.imageResult.toString())
                binding.llSaveToDevice.postDelayed({ isClickable = true }, 500)
                binding.llShareWithFriends.postDelayed({ isClickable = true }, 500)
            }

        }

        binding.ivHome.tap {
            val intent = Intent(this@DownloadRemoveBackgroundActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ivHistory.tap {
            val intent = Intent(this@DownloadRemoveBackgroundActivity, ProjectsActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun shareImageFromCache(imageUrl: String) {
        try {
//            val imageUrl = historyModel?.imageResult
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
                                    createDownloadFile(this@DownloadRemoveBackgroundActivity)
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
        } catch (_: Exception) {
        }
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