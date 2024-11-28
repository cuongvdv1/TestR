package com.vm.backgroundremove.objectremove.ui.main.remove_background
import android.view.View
import androidx.core.content.ContextCompat
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorAdapter
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorSelectorListener

import com.vm.backgroundremove.objectremove.ui.main.remove_background.generate.GenerateResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class RemoveBackgroundActivity : BaseActivity<ActivityRemoveBackgroundBinding,RemoveBackGroundViewModel>() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var colorAdapter: ColorAdapter

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
        val multipartFromCategory = createMultipartFromFile(filePath, "categoryImage")

        multipartFromGallery?.let { multipart ->
            viewModel.upLoadImage(
                Constants.ITEM_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                Constants.CLIENT_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                Constants.CLIENT_MEMO.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
                multipart // Đảm bảo multipart không null ở đây
            )
        } ?: run {
            Log.e("UploadError", "File is invalid or missing")
        }

        viewModel.upLoadImage.observe(this){response ->
            Log.d("tag12340","response $response")
        }
        binding.tvChooseBgOp1.tap {
            binding.tvChooseBgOp1.setTextColor(ContextCompat.getColor(this, R.color.color_FF0000))
            binding.tvChooseBgOp2.setTextColor(ContextCompat.getColor(this, R.color.color_37414B))
            binding.ivRedo.visibility = View.VISIBLE
            binding.ivUndo.setImageResource(R.drawable.ic_undo_off)
            binding.ivBeforeAfter.setImageResource(R.drawable.ic_before_after)
            binding.tvEdit.setText(R.string.edit)
            fragment.showColorList()
        }
        binding.tvChooseBgOp2.tap {
            binding.tvChooseBgOp2.setTextColor(ContextCompat.getColor(this, R.color.color_FF0000))
            binding.tvChooseBgOp1.setTextColor(ContextCompat.getColor(this, R.color.color_37414B))
            fragment.showBackgroundList()

        }
        binding.ivBack.tap {
            finish()
        }}
    fun setNewImage(){
        binding.ivBeforeAfter.setImageResource(R.drawable.ic_selected)
        binding.ivRedo.visibility = View.GONE
        binding.ivUndo.setImageResource(R.drawable.ic_back)
        binding.tvChooseBgOp2.setText(R.string.gradient)
        binding.tvEdit.setText(R.string.color_picker)

    }

    override fun viewModel() {
        super.viewModel()
    }
    fun createMultipartFromFile(filePath: String?, partName: String): MultipartBody.Part? {
        // Kiểm tra nếu filePath rỗng hoặc null
        if (filePath.isNullOrEmpty()) return null

        val file = File(filePath) // Tạo File từ đường dẫn
        return if (file.exists()) { // Kiểm tra nếu file tồn tại
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull()) // Định dạng loại file
            MultipartBody.Part.createFormData(partName, file.name, requestFile) // Tạo MultipartBody.Part
        } else {
            null
        }
    }
}