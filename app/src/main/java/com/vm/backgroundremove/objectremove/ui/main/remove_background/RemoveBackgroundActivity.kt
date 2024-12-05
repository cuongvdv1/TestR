package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.Intent
import android.util.Log
import android.view.View
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import com.vm.backgroundremove.objectremove.ui.main.progress.ProessingActivity
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

class RemoveBackgroundActivity :
    BaseActivity<ActivityRemoveBackgroundBinding, RemoveBackGroundViewModel>() {
    private var check_clicked_color: Boolean = false
    override fun createBinding(): ActivityRemoveBackgroundBinding {
        return ActivityRemoveBackgroundBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value

    override fun initView() {
        super.initView()
        val imgPathGallery = intent.getStringExtra(Constants.IMG_GALLERY_PATH)
        val imagePathCamera = intent.getStringExtra(Constants.IMG_CAMERA_PATH)
        if (!imagePathCamera.isNullOrEmpty()) {
            uploadImageRemoveBackground(imagePathCamera)
            getBitmapFrom(this, imagePathCamera) {
                binding.ivRmvBg.setBitmap(it)
            }
        } else if (!imgPathGallery.isNullOrEmpty()) {
            uploadImageRemoveBackground(imgPathGallery)
            getBitmapFrom(this, imgPathGallery) {
                binding.ivRmvBg.setBitmap(it)
            }
        }

        // Tạo fragment
        val fragment = ChooseBackGroundColorFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()
        viewModel.upLoadImage.observe(this) { response ->
            startDataGenerate(response)
        }
    }

    fun createMultipartFromFile(filePath: String?, partName: String): MultipartBody.Part? {
        Log.d("TAG_URL", "createMultipartFromFile: $filePath")
        // Kiểm tra nếu filePath rỗng hoặc null
        if (filePath.isNullOrEmpty()) return null

        val file = File(filePath)
        if (!file.exists()) {
            Log.d("hehehee", "File does not exist: $filePath")
            return null
        }

        Log.d("hehehee", "File exists: ${file.absolutePath}")

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name , requestFile)
    }

    fun setNewImage() {
        binding.ivBeforeAfter.setImageResource(R.drawable.ic_selected)
        binding.ivRedo.visibility = View.GONE
        binding.ivUndo.setImageResource(R.drawable.ic_cancel)
    }

    companion object {
        const val KEY_GENERATE = "KEY_GENERATE"
        const val LIMIT_NUMBER_ERROR = "LIMIT_NUMBER_ERROR"
        const val LIMIT_NUMBER_GENERATE = "LIMIT_NUMBER_GENERATE"
        const val KEY_REMOVE = "KEY_REMOVE"
    }

    private fun startDataGenerate(uploadResponse: UpLoadImagesResponse) {
        val modelGenerate = GenerateResponse()
        modelGenerate.cf_url = uploadResponse.cf_url
        modelGenerate.task_id = uploadResponse.task_id
//        val numberGenerate = limitNumber.toInt() - isCountGenerate
        startActivity(
            Intent(
                this@RemoveBackgroundActivity,
                ProessingActivity::class.java
            ).apply {
                putExtra(KEY_GENERATE, modelGenerate)
                putExtra(KEY_REMOVE, Constants.ITEM_CODE)
//                putExtra(LIMIT_NUMBER_GENERATE, numberGenerate)
            })
        finish()
    }

    private fun startToProcess() {
        startActivity(
            Intent(
                this@RemoveBackgroundActivity,
                ProessingActivity::class.java
            ).apply {
                putExtra(LIMIT_NUMBER_ERROR, LIMIT_NUMBER_ERROR)
            })
        finish()
    }

    fun receiveDataFromFragment(value: Boolean) {
        // Xử lý giá trị bạn nhận được từ Fragment
        if (value) {
            check_clicked_color = true
        } else {
            check_clicked_color = false
        }
    }

    private fun uploadImageRemoveBackground(path: String) {
        // chuyen tu path sang bitmap
        val bitMap = path?.let { Utils.getBitmapFromPath(it) }
        CoroutineScope(Dispatchers.IO).launch {

            // resize lai kich thuoc va luu anh vao cache
            val resizedBitmap = bitMap?.let { Utils.scaleBitmap(it) }
            val tempFile = resizedBitmap?.let {
                Utils.getFileFromScaledBitmap(
                    this@RemoveBackgroundActivity,
                    it,
                    Utils.NAME_IMAGE +"_"+ System.currentTimeMillis()
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
                    Constants.ITEM_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                    Constants.CLIENT_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                    Constants.CLIENT_MEMO.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                    multipart
                )
            }
        }
    }

    override fun viewModel() {
        super.viewModel()
    }

}

