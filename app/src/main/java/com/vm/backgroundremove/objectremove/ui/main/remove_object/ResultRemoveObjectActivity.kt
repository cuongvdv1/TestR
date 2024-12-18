package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lib.admob.resumeAds.AppOpenResumeManager
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.a8_app_utils.parcelable
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityResultRemoveObjectBinding
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import java.io.File
import java.io.FileOutputStream

class ResultRemoveObjectActivity :
    BaseActivity<ActivityResultRemoveObjectBinding, BaseViewModel>() {
    private var historyModel: HistoryModel? = null
    private var type = ""
    override fun createBinding(): ActivityResultRemoveObjectBinding {
        return ActivityResultRemoveObjectBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()
        binding.icHome.tap {
            startActivity(Intent(this@ResultRemoveObjectActivity, HomeActivity::class.java))
        }
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
                                // Đây là nơi bạn nhận được Bitmap
                                val bitmap: Bitmap = resource
                                binding.ivHistoryResult.setImageFromBitmap(bitmap)
                            }
                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })

                }
            }
            binding.llShareWithFriend.tap { shareImageFromCache() }
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
                                val filePath = createDownloadFile(this@ResultRemoveObjectActivity)
                                val file = File(filePath)

                                val fos = FileOutputStream(file)
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                                fos.close()

                                val imageUri = FileProvider.getUriForFile(
                                    this@ResultRemoveObjectActivity,
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

}