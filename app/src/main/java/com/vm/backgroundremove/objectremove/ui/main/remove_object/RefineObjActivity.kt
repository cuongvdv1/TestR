package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.content.Intent
import android.graphics.Bitmap
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveObjectBinding
import com.vm.backgroundremove.objectremove.dialog.ProcessingDialog
import com.vm.backgroundremove.objectremove.ui.main.progress.ProessingActivity
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

class RefineObjActivity :
    BaseActivity<ActivityRemoveObjectBinding, RemoveBackGroundViewModel>() {
    private lateinit var processingDialog: ProcessingDialog
    override fun createBinding() = ActivityRemoveObjectBinding.inflate(layoutInflater)


    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value

    override fun onDestroy() {
        super.onDestroy()
        intent = null
    }

    override fun initView() {
        super.initView()
        processingDialog = ProcessingDialog(this@RefineObjActivity)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_rm_object, RemoveObjectFragment()).commit()
        val imgPathGallery = intent.getStringExtra(Constants.IMG_GALLERY_PATH)
        val imagePathCamera = intent.getStringExtra(Constants.IMG_CAMERA_PATH)
        if (imgPathGallery != null) {
            getBitmapFrom(this, imgPathGallery) {
                binding.ivRmvObject.setImageFromBitmap(it)
            }
        } else if (imagePathCamera != null) {
            getBitmapFrom(this, imagePathCamera) {
                binding.ivRmvObject.setImageFromBitmap(it)
            }
        }
        viewModel.triggerRemove.observe(this) {
            viewModel.text.observe(this) { text ->
                if (imgPathGallery != null) {
                    getBitmapFrom(this, imgPathGallery) {
                        uploadImageRemoveBackground(it, text.toString())
                    }

                } else if (imagePathCamera != null) {
                    getBitmapFrom(this, imagePathCamera) {
                        uploadImageRemoveBackground(it, text.toString())
                    }
                }
            }
            viewModel.upLoadImage.observe(this) { response ->
                startDataGenerate(response)
            }
        }
        viewModel.triggerRemoveByList.observe(this) {
            viewModel.textByList.observe(this) { text ->
                if (imgPathGallery != null) {
                    getBitmapFrom(this, imgPathGallery) {
                        uploadImageRemoveBackground(it, text.toString())
                    }
                } else if (imagePathCamera != null) {
                    getBitmapFrom(this, imagePathCamera) {
                        uploadImageRemoveBackground(it, text.toString())
                    }
                }
            }
            viewModel.upLoadImage.observe(this) { response ->
                startDataGenerateByList(response)
            }
        }

    }

    private fun startDataGenerate(uploadResponse: UpLoadImagesResponse) {
        processingDialog.dismiss()
        val modelGenerate = GenerateResponse()
        modelGenerate.cf_url = uploadResponse.cf_url
        modelGenerate.task_id = uploadResponse.task_id
        modelGenerate.imageCreate = Constants.ITEM_CODE_RMOBJECT_REFINE_OBJ

//        val numberGenerate = limitNumber.toInt() - isCountGenerate
        startActivity(
            Intent(
                this@RefineObjActivity,
                ProessingActivity::class.java
            ).apply {
                putExtra(KEY_GENERATE, modelGenerate)
                putExtra(KEY_REMOVE, Constants.ITEM_CODE_RMOBJECT_REFINE_OBJ)
//                putExtra(LIMIT_NUMBER_GENERATE, numberGenerate)
            })
        finish()
    }

    private fun startDataGenerateByList(uploadResponse: UpLoadImagesResponse) {
        processingDialog.dismiss()
        val modelGenerate = GenerateResponse()
        modelGenerate.cf_url = uploadResponse.cf_url
        modelGenerate.task_id = uploadResponse.task_id
        modelGenerate.imageCreate = Constants.ITEM_CODE_RMOBJECT_REFINE_OBJ
//        val numberGenerate = limitNumber.toInt() - isCountGenerate
        startActivity(
            Intent(
                this@RefineObjActivity,
                ProessingActivity::class.java
            ).apply {
                putExtra(KEY_GENERATE, modelGenerate)
                putExtra(KEY_REMOVE, Constants.ITEM_CODE_RMOBJECT_REFINE_OBJ)
//                putExtra(LIMIT_NUMBER_GENERATE, numberGenerate)
            })
        finish()
    }

    private fun uploadImageRemoveBackground(bitMap: Bitmap, objectRemovelist: String) {
        processingDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            // resize lai kich thuoc va luu anh vao cache
            val resizedBitmap = bitMap?.let { Utils.scaleBitmap(it) }
            val tempFile = resizedBitmap?.let {
                Utils.getFileFromScaledBitmap(
                    this@RefineObjActivity,
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
                    Constants.ITEM_CODE_RMOBJECT_REFINE_OBJ.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                    Constants.CLIENT_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                    Constants.CLIENT_MEMO.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                    multipart,
                    objectRemovelist.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull())
                )
            }
        }
    }


}