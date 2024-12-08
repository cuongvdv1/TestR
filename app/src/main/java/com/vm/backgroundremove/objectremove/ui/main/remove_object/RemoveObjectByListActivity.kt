package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.a8_app_utils.parcelable
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveObjectByListBinding
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
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class RemoveObjectByListActivity :
    BaseActivity<ActivityRemoveObjectByListBinding, RemoveBackGroundViewModel>() {
    private var historyModel: HistoryModel? = null
    private var type = ""
    private var bitmap: Bitmap? = null

    override fun createBinding(): ActivityRemoveObjectByListBinding {
        return ActivityRemoveObjectByListBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value


    override fun initView() {
        super.initView()

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
                            startDataGenerate(response)
                        }
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

    private fun uploadImageRemoveObjectByList(bitMap: Bitmap, objectRemovelist: String) {

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

    private fun startDataGenerate(uploadResponse: UpLoadImagesResponse) {
        val modelGenerate = GenerateResponse()
        modelGenerate.cf_url = uploadResponse.cf_url
        modelGenerate.task_id = uploadResponse.task_id
        modelGenerate.imageCreate = Constants.ITEM_CODE_RMOBJECT
//        val numberGenerate = limitNumber.toInt() - isCountGenerate
        startActivity(
            Intent(
                this@RemoveObjectByListActivity,
                ProessingActivity::class.java
            ).apply {
                putExtra(KEY_GENERATE, modelGenerate)
                putExtra(KEY_REMOVE, Constants.ITEM_CODE_RMOBJECT)
//                putExtra(LIMIT_NUMBER_GENERATE, numberGenerate)
            })
        finish()
    }
}