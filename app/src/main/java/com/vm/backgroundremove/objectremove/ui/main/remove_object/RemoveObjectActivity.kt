package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.bumptech.glide.Glide
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveObjectBinding
import com.vm.backgroundremove.objectremove.dialog.DetectingDialog
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

class RemoveObjectActivity :
    BaseActivity<ActivityRemoveObjectBinding, RemoveBackGroundViewModel>() {
    private lateinit var processingDialog: DetectingDialog
    private lateinit var processingDialog1: ProcessingDialog
    override fun createBinding() = ActivityRemoveObjectBinding.inflate(layoutInflater)


    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value

    override fun onDestroy() {
        super.onDestroy()
        intent = null
        processingDialog?.dismiss()
        processingDialog1?.dismiss()
    }

    override fun initView() {
        super.initView()
        binding.ivBack.tap {
            finish()
        }
        processingDialog = DetectingDialog(this@RemoveObjectActivity)
        processingDialog1 = ProcessingDialog(this@RemoveObjectActivity)
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
                if (response.success == false){
                    Toast.makeText(this, "Failed to process the image. Please try again.", Toast.LENGTH_SHORT).show()
                    return@observe
                }
                if (imgPathGallery != null) {
                    startDataGenerate(response, imgPathGallery)
                } else if (imagePathCamera != null) {
                    startDataGenerate(response, imagePathCamera)
                }
            }
        }
        viewModel.triggerRemoveByList.observe(this) {
            if (imgPathGallery != null) {
                getBitmapFrom(this, imgPathGallery) {
                    uploadImageRemoveBackgroundByList(it, "")
                }
            } else if (imagePathCamera != null) {
                getBitmapFrom(this, imagePathCamera) {
                    uploadImageRemoveBackgroundByList(it, "")
                }
            }

            viewModel.upLoadImage.observe(this) { response ->
                if (response.success == false){
                    Toast.makeText(this, "Failed to process the image. Please try again.", Toast.LENGTH_SHORT).show()
                    return@observe
                }
                if (imgPathGallery != null) {
                    startDataGenerateByList(response, imgPathGallery)
                } else if (imagePathCamera != null) {
                    startDataGenerateByList(response, imagePathCamera)
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
        if (modelGenerate.cf_url != null){
            startActivity(
                Intent(
                    this@RemoveObjectActivity,
                    ProessingActivity::class.java
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

    private fun startDataGenerateByList(uploadResponse: UpLoadImagesResponse, imageCreate: String) {
        processingDialog.dismiss()
        val modelGenerate = GenerateResponse()
        modelGenerate.cf_url = uploadResponse.cf_url
        modelGenerate.task_id = uploadResponse.task_id
        modelGenerate.imageCreate = Constants.ITEM_CODE_RMOBJECT_REFINE_OBJ
//        val numberGenerate = limitNumber.toInt() - isCountGenerate
        startActivity(
            Intent(
                this@RemoveObjectActivity,
                ProessingActivity::class.java
            ).apply {
                putExtra(KEY_GENERATE, modelGenerate)
                putExtra(KEY_REMOVE, Constants.ITEM_CODE_RMOBJECT_REFINE_OBJ)
                putExtra("imageCreate", imageCreate)
                putExtra("type_process", "remove_obj_by_list")
//                putExtra(LIMIT_NUMBER_GENERATE, numberGenerate)
            })
        finish()
    }
    private fun uploadImageRemoveBackgroundByList(bitMap: Bitmap, objectRemovelist: String) {
        processingDialog1.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Giới hạn thời gian tải lên là 30 giây
                withTimeout(30000) {
                    val resizedBitmap = bitMap.let { Utils.scaleBitmap(it) }
                    val tempFile = resizedBitmap?.let {
                        Utils.getFileFromScaledBitmap(
                            this@RemoveObjectActivity,
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
                            Constants.ITEM_CODE_RMOBJECT_REFINE_OBJ.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
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
                        this@RemoveObjectActivity,
                        "Upload timeout. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                // Xử lý các ngoại lệ khác
                processingDialog1.dismiss()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@RemoveObjectActivity,
                        "An error occurred: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
                            this@RemoveObjectActivity,
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
                        this@RemoveObjectActivity,
                        "Upload timeout. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                // Xử lý các ngoại lệ khác
                processingDialog1.dismiss()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@RemoveObjectActivity,
                        "An error occurred: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


}