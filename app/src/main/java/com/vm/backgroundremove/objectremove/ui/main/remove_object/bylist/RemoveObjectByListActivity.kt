package com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
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
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveObjectByListBinding
import com.vm.backgroundremove.objectremove.dialog.ProcessingDialog
import com.vm.backgroundremove.objectremove.ui.main.progress.ProessingActivity
import com.vm.backgroundremove.objectremove.ui.main.progress.ProessingRefineActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackGroundViewModel
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity.Companion.KEY_GENERATE
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity.Companion.KEY_REMOVE
import com.vm.backgroundremove.objectremove.ui.main.remove_background.generate.GenerateResponse
import com.vm.backgroundremove.objectremove.util.Utils
import com.vm.backgroundremove.objectremove.util.getBitmapFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class RemoveObjectByListActivity :
    BaseActivity<ActivityRemoveObjectByListBinding, RemoveBackGroundViewModel>() {
    private var historyModel: HistoryModel? = null
    private var type = ""
    private var bitmap: Bitmap? = null
    private lateinit var processingDialog: ProcessingDialog
    override fun createBinding(): ActivityRemoveObjectByListBinding {
        return ActivityRemoveObjectByListBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value


    override fun initView() {
        super.initView()
        binding.ivBack.tap {
            finish()
        }
        processingDialog = ProcessingDialog(this@RemoveObjectByListActivity)
        type = intent.getStringExtra(Constants.TYPE_HISTORY).toString()
        try {
            historyModel = intent.parcelable<HistoryModel>(Constants.INTENT_RESULT)
            Log.d("TAG_MODEL", "$historyModel")
            val items = convertOtherToList(historyModel?.other.toString())
            viewModel.setItemList(items)
            val fragment = RemoveObjectByListFragment()

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

                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                }
                viewModel.triggerRemoveByListSelected.observe(this) {
                    viewModel.textByListSelected.observe(this) { text ->

                        if (bitmap != null) {
                            getBitmapFrom(this, historyModel.imageResult) {
                                uploadImageRemoveObjectByList(it, text.toString())
                            }
                        }
                        viewModel.upLoadImage.observe(this) { response ->
                            startDataGenerate(response, historyModel?.other.toString())
                        }
                    }
                }
            }
            binding.ivBeforeAfter.tap {
                Log.d("YEUTRINHLAMLUON",  historyModel?.imageCreate.toString())
                val uriImage = Uri.parse( historyModel?.imageCreate.toString())
                binding.ivRmvObject.toggleImage(bitmap!!, uriImage)
            }
            binding.ivExport.tap {
                downloadImageToGallery()
            }
        } catch (_: Exception) {
        }
    }

    private fun convertOtherToList(other: String): List<String> {
        val cleanedString = other.removePrefix("[[").removeSuffix("]]")
        return cleanedString.split(",").map { it.trim() }
    }

    private fun uploadImageRemoveObjectByList(bitMap: Bitmap, objectRemovelist: String) {
        processingDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            // resize lai kich thuoc va luu anh vao cache
            val resizedBitmap = bitMap?.let { Utils.scaleBitmap(it) }
            val tempFile = resizedBitmap?.let {
                Utils.getFileFromScaledBitmap(
                    this@RemoveObjectByListActivity,
                    it,
                    Utils.NAME_IMAGE + "_" + System.currentTimeMillis()
                )
            }

            if (tempFile != null) {
                val requestBody =
                    tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val multipart =
                    MultipartBody.Part.createFormData(
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
    }

    private fun startDataGenerate(
        uploadResponse: UpLoadImagesResponse,
        listOther: String
    ) {
        processingDialog.dismiss()
        val modelGenerate = GenerateResponse()
        modelGenerate.cf_url = uploadResponse.cf_url
        modelGenerate.task_id = uploadResponse.task_id
        modelGenerate.imageCreate = Constants.ITEM_CODE_RMOBJECT
        if (modelGenerate.cf_url != null){
            startActivity(
                Intent(
                    this@RemoveObjectByListActivity,
                    ProessingActivity::class.java
                ).apply {
                    putExtra(KEY_GENERATE, modelGenerate)
                    putExtra(KEY_REMOVE, Constants.ITEM_CODE_RMOBJECT)
                    putExtra("imageCreate", historyModel?.imageCreate)
                    putExtra("type_process", "remove_obj_by_list_text")
                    putExtra("listOther",listOther)
//                putExtra(LIMIT_NUMBER_GENERATE, numberGenerate)
                })
            finish()
        }
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



    private fun downloadImageToGallery() {
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
                            val filename = "IMG_${System.currentTimeMillis()}.jpg"
                            val fos: OutputStream?
                            val contentResolver = contentResolver

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                val contentValues = ContentValues().apply {
                                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                                    put(
                                        MediaStore.MediaColumns.RELATIVE_PATH,
                                        Environment.DIRECTORY_PICTURES
                                    )
                                }
                                val imageUri = contentResolver.insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    contentValues
                                )
                                fos = imageUri?.let { contentResolver.openOutputStream(it) }
                            } else {
                                val imagesDir =
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                val image = File(imagesDir, filename)
                                fos = FileOutputStream(image)
                            }

                            fos?.use {
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, it)
                            }
                            Toast.makeText(
                                this@RemoveObjectByListActivity,
                                "Download Success", Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            // Handle placeholder if needed
                        }
                    })
            }
        } catch (_: Exception) {
        }
    }

}