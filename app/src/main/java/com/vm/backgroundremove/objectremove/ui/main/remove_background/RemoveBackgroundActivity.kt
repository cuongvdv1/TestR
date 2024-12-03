package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import com.vm.backgroundremove.objectremove.ui.main.progress.ProcessActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorAdapter
import com.vm.backgroundremove.objectremove.ui.main.remove_background.generate.GenerateResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class RemoveBackgroundActivity :
    BaseActivity<ActivityRemoveBackgroundBinding, RemoveBackGroundViewModel>() {
    override fun createBinding(): ActivityRemoveBackgroundBinding {
        return ActivityRemoveBackgroundBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value

    override fun initView() {
        super.initView()

        val imgPathGallery = intent.getStringExtra(Constants.IMG_GALLERY_PATH)
        val imagePathCamera = intent.getStringExtra(Constants.IMG_CAMERA_PATH)

        val filePath = intent.getStringExtra(Constants.IMG_CATEGORY_PATH)
        Log.d("TAG123", "filePath: $filePath")
        if (!imagePathCamera.isNullOrEmpty()) {
            Glide.with(this)
                .load(File(imagePathCamera))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.ivRmvBg)

        } else if (!imgPathGallery.isNullOrEmpty()) {
            Glide.with(this)
                .load(imgPathGallery)
                .into(binding.ivRmvBg)
        }

        val fragment = ChooseBackGroundColorFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()


        val multipartFromCamera = createMultipartFromFile(imagePathCamera, "cameraImage")
        val multipartFromGallery = createMultipartFromFile(imgPathGallery, "galleryImage")

        multipartFromGallery?.let { multipart ->
            viewModel.upLoadImage(
                Constants.ITEM_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                Constants.CLIENT_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                Constants.CLIENT_MEMO.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                multipart
            )
        } ?: run {
            Log.e("UploadError", "File is invalid or missing")
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
        binding.tvChooseBgOp1.tap {
            binding.tvChooseBgOp1.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_FF0000
                )
            )
            binding.tvChooseBgOp2.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.color_37414B
                )
            )
            binding.ivRedo.visibility = View.VISIBLE
            binding.ivUndo.setImageResource(R.drawable.ic_undo_off)
            binding.ivBeforeAfter.setImageResource(R.drawable.ic_before_after)
            binding.tvEdit.setText(R.string.edit)
            fragment.showColorList()
        }
    }
    fun createMultipartFromFile(filePath: String?, partName: String): MultipartBody.Part? {
        // Kiểm tra nếu filePath rỗng hoặc null
        if (filePath.isNullOrEmpty()) return null

        val file = File(filePath) // Tạo File từ đường dẫn
        return if (file.exists()) { // Kiểm tra nếu file tồn tại
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull()) // Định dạng loại file
            MultipartBody.Part.createFormData(
                partName,
                file.name,
                requestFile
            )
        } else {
            null
        }
    }
    fun setNewImage(){
        binding.ivBeforeAfter.setImageResource(R.drawable.ic_selected)
        binding.ivRedo.visibility = View.GONE
        binding.ivUndo.visibility = View.GONE
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
                ProcessActivity::class.java
            ).apply {
                putExtra(KEY_GENERATE, modelGenerate)
                putExtra(KEY_REMOVE,Constants.ITEM_CODE)
//                putExtra(LIMIT_NUMBER_GENERATE, numberGenerate)
            })
        finish()
    }


    private fun startToProcess(){
            startActivity(
                Intent(
                    this@RemoveBackgroundActivity,
                    ProcessActivity::class.java
                ).apply {
                    putExtra(LIMIT_NUMBER_ERROR, LIMIT_NUMBER_ERROR)
                })
            finish()
    }
}

