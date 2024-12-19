package com.vm.backgroundremove.objectremove.ui.main.remove_object.bytext

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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.a8_app_utils.parcelable
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityResultSaveBinding
import com.vm.backgroundremove.objectremove.dialog.LoadingDialog
import com.vm.backgroundremove.objectremove.dialog.ProcessingDialog
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import com.vm.backgroundremove.objectremove.ui.main.progress.ProcessingActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackGroundViewModel
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity.Companion.KEY_GENERATE
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity.Companion.KEY_REMOVE
import com.vm.backgroundremove.objectremove.ui.main.remove_background.generate.GenerateResponse
import com.vm.backgroundremove.objectremove.ui.main.remove_object.ResultRemoveObjectActivity
import com.vm.backgroundremove.objectremove.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ResultRemoveObjectAndDowLoadActivity :
    BaseActivity<ActivityResultSaveBinding, RemoveBackGroundViewModel>() {
    override fun createBinding(): ActivityResultSaveBinding {
        return ActivityResultSaveBinding.inflate(layoutInflater)
    }

    private lateinit var processingDialog1: ProcessingDialog
    private var historyModel: HistoryModel? = null
    private var bitmap: Bitmap? = null
    private var type = ""
    private lateinit var dialog: LoadingDialog

    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value

    override fun initView() {
        super.initView()
        processingDialog1 = ProcessingDialog(this@ResultRemoveObjectAndDowLoadActivity)
        dialog = LoadingDialog(this@ResultRemoveObjectAndDowLoadActivity)
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fl_rm_object_by_list_dowload, RemoveByTextResultFragment()).commit()

        binding.ivHome.tap {
            startActivity(Intent(this, HomeActivity::class.java))
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
                                binding.ivHistoryResult.setImageFromBitmap(bitmap!!)

                            }

                            override fun onLoadCleared(placeholder: Drawable?) {

                            }
                        })
                }


//                binding.ivBeforeAfter.tap {
//                    val uriImage = Uri.parse(historyModel?.imageCreate.toString())
//                    binding.ivRmvObject.toggleImage(bitmap!!, uriImage)
//                }
            }

        } catch (_: Exception) {
        }

        binding.llSaveToDevice.tap {
            val imageUrl = historyModel?.imageResult?.takeIf { it.isNotEmpty() }

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

    private fun uploadImageRemoveBackground(bitMap: Bitmap, objectRemovelist: String) {
        processingDialog1.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Giới hạn thời gian tải lên là 30 giây
                withTimeout(30000) {
                    val resizedBitmap = bitMap.let { Utils.scaleBitmap(it) }
                    val tempFile = resizedBitmap?.let {
                        Utils.getFileFromScaledBitmap(
                            this@ResultRemoveObjectAndDowLoadActivity,
                            it,
                            Utils.NAME_IMAGE + "_" + System.currentTimeMillis()
                        )
                    }

                    if (tempFile != null) {
                        val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                        val multipart = MultipartBody.Part.createFormData(
                            Constants.PAYLOAD_REPLACE_SRC,
                            tempFile.name,
                            requestBody
                        )
                        viewModel.upLoadImage(
                            Constants.ITEM_CODE_RMOBJECT.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                            Constants.CLIENT_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                            Constants.CLIENT_MEMO.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                            multipart,
                            objectRemovelist.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull())
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                // Đóng dialog và hiển thị thông báo nếu quá 30 giây
                processingDialog1.dismiss()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@ResultRemoveObjectAndDowLoadActivity,
                        getString(R.string.upload_timeout_please_try_again),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                processingDialog1.dismiss()
                CoroutineScope(Dispatchers.Main).launch {

                }
            }
        }
    }

    private fun startDataGenerate(uploadResponse: UpLoadImagesResponse, imageCreate: String) {
        processingDialog1.dismiss()
        val modelGenerate = GenerateResponse()
        modelGenerate.cf_url = uploadResponse.cf_url
        modelGenerate.task_id = uploadResponse.task_id
        modelGenerate.imageCreate = Constants.ITEM_CODE_RMOBJECT
//        val numberGenerate = limitNumber.toInt() - isCountGenerate
        if (modelGenerate.cf_url != null) {
            startActivity(
                Intent(
                    this@ResultRemoveObjectAndDowLoadActivity,
                    ProcessingActivity::class.java
                ).apply {
                    putExtra(KEY_GENERATE, modelGenerate)
                    putExtra(KEY_REMOVE, Constants.ITEM_CODE_RMOBJECT)
                    putExtra("imageCreate", imageCreate)
                    putExtra("type_process", "remove_obj_by_text")
//                putExtra(LIMIT_NUMBER_GENERATE, numberGenerate)
                })
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

                CoroutineScope(Dispatchers.Main).launch {
                    val intent = Intent(
                        this@ResultRemoveObjectAndDowLoadActivity,
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