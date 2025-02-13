package com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.a8_app_utils.parcelable
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveObjectByListBinding
import com.vm.backgroundremove.objectremove.dialog.LoadingDialog
import com.vm.backgroundremove.objectremove.dialog.ProcessingDialog
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackGroundViewModel
import com.vm.backgroundremove.objectremove.ui.main.remove_object.ResultRemoveObjectActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.ProjectsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ResultRemoveObjectByList :
    BaseActivity<ActivityRemoveObjectByListBinding, RemoveBackGroundViewModel>() {
    private var historyModel: HistoryModel? = null
    private var type = ""
    private var bitmap: Bitmap? = null
    private var filePath = ""
    private var listOther = ""
    private lateinit var processingDialog: ProcessingDialog
    override fun createBinding(): ActivityRemoveObjectByListBinding {
        return ActivityRemoveObjectByListBinding.inflate(layoutInflater)
    }

    private lateinit var dialog: LoadingDialog
    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value


    override fun initView() {
        super.initView()
        dialog = LoadingDialog(this)
        binding.ivBack.tap {
            finish()
        }
        binding.ivExport.tap {
            val intent = Intent(this@ResultRemoveObjectByList, ProjectsActivity::class.java)
            startActivity(intent)
        }
        processingDialog = ProcessingDialog(this@ResultRemoveObjectByList)
        type = intent.getStringExtra(Constants.TYPE_HISTORY).toString()
        listOther = intent.getStringExtra("listOther").toString()
        try {
            historyModel = intent.parcelable<HistoryModel>(Constants.INTENT_RESULT)
            Log.d("TAG_MODEL", "$historyModel")
            val items = convertOtherToList(listOther)
            viewModel.setItemList(items)
            val fragment = ResultRemoveObjectByListFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_rm_object_by_list, fragment)
                .commit()
            historyModel?.let { historyModel ->
                if (!historyModel.imageResult.isNullOrEmpty()) {
                    Glide.with(this).asBitmap()
                        .load(historyModel.imageResult)
                        .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap, transition: Transition<in Bitmap>?
                            ) {
                                bitmap = resource
                                binding.ivRmvObject.setImageFromBitmap(bitmap!!)
                                val fileName = "image_${System.currentTimeMillis()}.png"
                                filePath = filesDir.absolutePath + "/images/$fileName"
                                saveBitmapToPath(bitmap!!, filePath)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                }

            }
            binding.ivBeforeAfter.tap {
                val uriImage = Uri.parse(historyModel?.imageCreate.toString())
                binding.ivRmvObject.toggleImage(bitmap!!, uriImage)
            }
            binding.ivExport.tap {
                val imageUrl = historyModel?.imageResult?.takeIf { it.isNotEmpty() }

                if (imageUrl != null) {
                    if (imageUrl != null) {
                        dialog.setOnDismissListener {
                            downloadImageFromUrl(this, imageUrl)
                        }
                        dialog.showWithTimeout(3000)
                    } else {
                        Log.d("TAG_IMAGE", "Image URL is null or empty")
                    }

                }

            }
        } catch (_: Exception) {
        }
    }

    private fun convertOtherToList(other: String): List<String> {
        val cleanedString = other.removePrefix("[[").removeSuffix("]]")
        return cleanedString.split(",").map { it.trim() }
    }

    fun saveBitmapToPath(bitmap: Bitmap, filePath: String) {
        try {
            val file = File(filePath)
            file.parentFile?.mkdirs() // Tạo thư mục nếu chưa tồn tại
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // Lưu ảnh dưới dạng PNG
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
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
                    val intent = Intent(
                        this@ResultRemoveObjectByList,
                        ResultRemoveObjectActivity::class.java
                    )
                    intent.putExtra(Constants.INTENT_RESULT, historyModel)
                    startActivity(intent)
                    finish()
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