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
import androidx.lifecycle.ViewModelProvider
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
import com.vm.backgroundremove.objectremove.dialog.LoadingDialog
import com.vm.backgroundremove.objectremove.dialog.ProcessingDialog
import com.vm.backgroundremove.objectremove.ui.main.progress.ProessingActivity
import com.vm.backgroundremove.objectremove.ui.main.progress.ProessingRefineActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackGroundViewModel
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity.Companion.KEY_GENERATE
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity.Companion.KEY_REMOVE
import com.vm.backgroundremove.objectremove.ui.main.remove_background.generate.GenerateResponse
import com.vm.backgroundremove.objectremove.ui.main.remove_object.ResultRemoveObjectActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.viewModel.ProjectViewModel
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
    private var listOtherSelected: String? = null
    private var listOtherAfterRemove: String? = null
    private var listOtherByIntent =""

    private lateinit var dialog: LoadingDialog
    var listOther2 = listOf<String>()
    private var bitmap: Bitmap? = null
    private lateinit var processingDialog: ProcessingDialog

    //    private lateinit var projectViewModel: ProjectViewModel
    override fun createBinding(): ActivityRemoveObjectByListBinding {
        return ActivityRemoveObjectByListBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value


    override fun initView() {
        super.initView()
        dialog= LoadingDialog(this)
//        projectViewModel = viewModel<ProjectViewModel>().value
        binding.ivBack.tap {
            finish()
        }
        processingDialog = ProcessingDialog(this@RemoveObjectByListActivity)
        type = intent.getStringExtra(Constants.TYPE_HISTORY).toString()

        listOtherByIntent = intent.getStringExtra("listOther").toString()
        Log.d("TAG_MODEL", "listOtherByIntent $listOtherByIntent")
        listOtherAfterRemove = intent.getStringExtra("listOther")
        Log.d("TAG_MODEL", "$listOtherAfterRemove")
        listOtherSelected = intent.getStringExtra("listOtherSelected")
        Log.d("TAG_MODEL1", "$listOtherSelected")

        try {
            historyModel = intent.parcelable<HistoryModel>(Constants.INTENT_RESULT)

            listOther2 = convertOtherToList(historyModel?.other.toString())
            val listOtherAfterRemoveList = listOtherAfterRemove?.let {
                convertOtherToList(it)
            } ?: emptyList()
            Log.d("TAG_MODEL", "$historyModel")

            if(listOtherByIntent.isNotEmpty() && listOtherByIntent != "" && listOtherByIntent != "null" && listOtherByIntent != "[]" && listOtherByIntent != null){
                listOther2 = listOtherByIntent.removeSurrounding("[", "]").split(", ").map { it.trim() }
            }
            viewModel.setItemList(listOther2)
            val selectedItems = listOtherSelected?.let {
                convertStringToList(it)
            } ?: emptyList()

            // Disable các item đã có trong listOther
            val itemsWithState = listOtherAfterRemoveList?.map { item ->
                item to selectedItems.contains(item)
            }

            // Gửi dữ liệu vào Adapter (cập nhật Adapter)

            val fragment = RemoveObjectByListFragment()
            if (listOtherAfterRemove != null){

                viewModel.setItemList(itemsWithState!!.map { it.first }) // Cập nhật list gốc
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_rm_object_by_list, fragment)
                .commit()

            // Pass trạng thái disable vào Fragment qua ViewModel
            viewModel.setItemDisabledState(selectedItems )

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
                        if (text.isNullOrEmpty()) {
                            return@observe
                        }
                        if (bitmap != null) {
                            getBitmapFrom(this, historyModel.imageCreate) {
                                uploadImageRemoveObjectByList(it, text.toString())
                            }
                        }
                        viewModel.upLoadImage.observe(this) { response ->

                            if (response.task_id.isNotBlank() && response.cf_url.isNotBlank() && response.success) {
                                startDataGenerateByListSelected(
                                    response,
                                    listOther2.toString(),
                                    text
                                )
                            } else {
                                processingDialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "Failed to process the image. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@observe
                            }

//                            projectViewModel.deleteHistory(historyModel)
                        }
                    }
                    viewModel.textByList.observe(this) { text ->
                        if (text.isNullOrEmpty()) {
                            return@observe
                        }
                        if (bitmap != null) {
                            getBitmapFrom(this, historyModel.imageCreate) {
                                uploadImageRemoveObjectByList(it, text.toString())
                            }
                        }
                        viewModel.upLoadImage.observe(this) { response ->

                            if (response.task_id.isNotBlank() && response.cf_url.isNotBlank() && response.success) {
                                startDataGenerate(response, historyModel?.other.toString())
                            } else {
                                processingDialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "Failed to process the image. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@observe
                            }

//                            projectViewModel.deleteHistory(historyModel)
                        }
                    }
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

    private fun convertStringToList(input: String?): List<String> {
        return input?.removeSurrounding("[", "]")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() } ?: emptyList()
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


    private fun startDataGenerateByListSelected(
        uploadResponse: UpLoadImagesResponse,
        listOther: String,
        listOtherSelected: String
    ) {
        processingDialog.dismiss()
        val modelGenerate = GenerateResponse()
        modelGenerate.cf_url = uploadResponse.cf_url
        modelGenerate.task_id = uploadResponse.task_id
        modelGenerate.imageCreate = Constants.ITEM_CODE_RMOBJECT
        if (modelGenerate.cf_url != null) {
            startActivity(
                Intent(
                    this@RemoveObjectByListActivity,
                    ProessingActivity::class.java
                ).apply {
                    putExtra(KEY_GENERATE, modelGenerate)
                    putExtra(KEY_REMOVE, Constants.ITEM_CODE_RMOBJECT)
                    putExtra("imageCreate", historyModel?.imageCreate)
                    putExtra("type_process", "remove_obj_by_list_text")
                    putExtra("listOther", listOther)
                    putExtra("listOtherSelected", listOtherSelected)
//                putExtra(LIMIT_NUMBER_GENERATE, numberGenerate)
                })
            finish()
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
        if (modelGenerate.cf_url != null) {
            startActivity(
                Intent(
                    this@RemoveObjectByListActivity,
                    ProessingActivity::class.java
                ).apply {
                    putExtra(KEY_GENERATE, modelGenerate)
                    putExtra(KEY_REMOVE, Constants.ITEM_CODE_RMOBJECT)
                    putExtra("imageCreate", historyModel?.imageCreate)
                    putExtra("type_process", "remove_obj_by_list_text")
                    putExtra("listOther", listOther)

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
                    val intent = Intent(this@RemoveObjectByListActivity, ResultRemoveObjectActivity::class.java)
                    intent.putExtra(Constants.INTENT_RESULT,historyModel)
                    startActivity(intent)
                    finish()
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