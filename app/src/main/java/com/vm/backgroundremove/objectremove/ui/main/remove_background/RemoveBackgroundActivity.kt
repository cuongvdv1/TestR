package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import com.vm.backgroundremove.objectremove.dialog.ProcessingDialog
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

class RemoveBackgroundActivity :
    BaseActivity<ActivityRemoveBackgroundBinding, RemoveBackGroundViewModel>() {
    override fun createBinding(): ActivityRemoveBackgroundBinding {
        return ActivityRemoveBackgroundBinding.inflate(layoutInflater)
    }

    private lateinit var processingDialog: ProcessingDialog
    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value

    override fun initView() {
        super.initView()
        window.statusBarColor = ContextCompat.getColor(this, R.color.color_F0F8FF)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        binding.cvRmvBg.visibility = View.GONE
        binding.ivBeforeAfter.setImageResource(R.drawable.ic_before_after_20)
        processingDialog = ProcessingDialog(this@RemoveBackgroundActivity)
        val imgPathGallery = intent.getStringExtra(Constants.IMG_GALLERY_PATH)
        val imagePathCamera = intent.getStringExtra(Constants.IMG_CAMERA_PATH)
        if (!imagePathCamera.isNullOrEmpty()) {
            getBitmapFrom(this, imagePathCamera) {
                uploadImageRemoveBackground(it)
                binding.ivRmvBg.setImageFromBitmap(it)
            }
        } else if (!imgPathGallery.isNullOrEmpty()) {
//            uploadImageRemoveBackground(imgPathGallery)
            Log.d("tag111", "$imgPathGallery")
            getBitmapFrom(this, imgPathGallery) {
                uploadImageRemoveBackground(it)
                binding.ivRmvBg.setImageFromBitmap(it)
            }
        }

        // Tạo fragment
        val fragment = ChooseBackGroundColorFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()
        viewModel.upLoadImage.observe(this) { response ->
            if (imgPathGallery != null) {
                startDataGenerate(response, imgPathGallery)
            } else if (imagePathCamera != null) {
                startDataGenerate(response, imagePathCamera)
            }
        }
        binding.ivBack.tap {
            finish()
        }
    }

    fun setNewImage() {
        binding.ivRedo.visibility = View.GONE
        binding.ivUndo.setImageResource(R.drawable.ic_cancel)
    }

    companion object {
        const val KEY_GENERATE = "KEY_GENERATE"
        const val LIMIT_NUMBER_ERROR = "LIMIT_NUMBER_ERROR"
        const val LIMIT_NUMBER_GENERATE = "LIMIT_NUMBER_GENERATE"
        const val KEY_REMOVE = "KEY_REMOVE"
    }

    private fun startDataGenerate(uploadResponse: UpLoadImagesResponse, imageCreate: String) {
        processingDialog.dismiss()
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
                putExtra("imageCreate", imageCreate)
                putExtra("type_process", "remove_background")
//                putExtra(LIMIT_NUMBER_GENERATE, numberGenerate)
            })
        finish()
    }

    private fun uploadImageRemoveBackground(bitMap: Bitmap) {
        // chuyen tu path sang bitmap
//        val bitMap = path?.let { Utils.getBitmapFromPath(it) }
        processingDialog.show()
        CoroutineScope(Dispatchers.IO).launch {

            // resize lai kich thuoc va luu anh vao cache
            val resizedBitmap = bitMap?.let { Utils.scaleBitmap(it) }
            val tempFile = resizedBitmap?.let {
                Utils.getFileFromScaledBitmap(
                    this@RemoveBackgroundActivity,
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
                    Constants.ITEM_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                    Constants.CLIENT_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                    Constants.CLIENT_MEMO.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                    multipart,
                    "".toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull())
                )
            }
        }
    }

    override fun viewModel() {
        super.viewModel()
    }
}

