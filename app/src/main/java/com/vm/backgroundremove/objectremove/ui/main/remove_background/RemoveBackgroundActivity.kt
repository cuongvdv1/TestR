package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.Intent
import android.util.Log
import android.view.View
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import com.vm.backgroundremove.objectremove.ui.main.progress.ProcessActivity
import com.vm.backgroundremove.objectremove.ui.main.progress.ProessingActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.generate.GenerateResponse
import com.vm.backgroundremove.objectremove.util.getBitmapFrom
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
        Log.d("imagePathCamera", "imagePathCamera $imagePathCamera\n $imgPathGallery")
        val filePath = intent.getStringExtra(Constants.IMG_CATEGORY_PATH)
        Log.d("TAG123", "filePath: $filePath")
        if (!imagePathCamera.isNullOrEmpty()) {
            getBitmapFrom(this, imagePathCamera) {
                binding.ivRmvBg.setBitmap(it)
            }
        } else if (!imgPathGallery.isNullOrEmpty()) {
            getBitmapFrom(this, imgPathGallery) {
                binding.ivRmvBg.setBitmap(it)
            }
        }
        val fragment = ChooseBackGroundColorFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()

        val multipartFromCamera = createMultipartFromFile(imagePathCamera, "cameraImage")
        val multipartFromGallery = createMultipartFromFile(imgPathGallery, "galleryImage")
        Log.d("hehehee", "createMultipartFromFile $multipartFromCamera\n $multipartFromGallery")

        multipartFromGallery?.let { multipart ->
            viewModel.upLoadImage(
                Constants.ITEM_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                Constants.CLIENT_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                Constants.CLIENT_MEMO.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                multipart
            )
        }

        multipartFromCamera?.let { multipart ->
            viewModel.upLoadImage(
                Constants.ITEM_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                Constants.CLIENT_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                Constants.CLIENT_MEMO.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                multipart
            )
        }

        viewModel.upLoadImage.observe(this) { response ->
            Log.d("tag12340", "response $response")
            startDataGenerate(response)
        }



    }

    fun createMultipartFromFile(filePath: String?, partName: String): MultipartBody.Part? {
        // Kiểm tra nếu filePath rỗng hoặc null
        if (filePath.isNullOrEmpty()) return null

        val file = File(filePath)
        if (!file.exists()) {
            Log.d("hehehee", "File does not exist: $filePath")
            return null
        }

        Log.d("hehehee", "File exists: ${file.absolutePath}")

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
//    fun setNewBackGround(color:String){
//        binding.ivRmvBg.setBackgroundBitmap(color)
//    }

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
        modelGenerate.cf_url = uploadResponse.cf_url.toString()
        modelGenerate.task_id = uploadResponse.task_id.toString()
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
}

