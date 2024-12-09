package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityYourProjectsResultBinding
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import java.io.File

class DownloadRemoveBackgroundActivity:BaseActivity<ActivityYourProjectsResultBinding, BaseViewModel>() {
    private var imageUrl = ""
    private var historyModel : HistoryModel? = null
    override fun createBinding(): ActivityYourProjectsResultBinding {
        return ActivityYourProjectsResultBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()
        imageUrl = intent.getStringExtra(Constants.IMG_CAMERA_PATH) ?: ""
        historyModel = intent.getParcelableExtra(Constants.INTENT_RESULT)

        if (imageUrl.isEmpty().not()) {
            binding.tvTitleName.visibility = View.GONE
            binding.tvSaved.visibility = View.VISIBLE
            binding.ivHome.visibility = View.VISIBLE
            binding.llBtnShare.visibility = View.GONE
            binding.tvShare.text = getString(R.string.share_with_friends)
            binding.ivShare.setImageResource(R.drawable.ic_share)
            Log.d("TAG_IMAGE", "imageUrl: $imageUrl")
            Glide.with(this)
                .load(imageUrl)
                .into(binding.ivHistoryResult) // Assume you have an ImageView in your layout
        }else{
            Log.d("TAG_IMAGE", "imageUrl1111: $imageUrl")
            Log.d("TAG_IMAGE", "historyModel: ${historyModel?.imageResult}")
            Glide.with(this)
                .load(historyModel?.imageResult)
                .into(binding.ivHistoryResult) // Assume you have an ImageView in your layout
        }

        binding.llBtnExport.tap {
            shareImage(imageUrl)
        }

        binding.ivBack.tap {
            finish()
        }
        binding.ivHome.tap {
            val intent = Intent(this@DownloadRemoveBackgroundActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }


    private fun shareImage(url: String) {
        // Tải hình ảnh từ URL và lưu tạm vào cache
        Glide.with(this)
            .asFile()
            .load(url)
            .into(object : CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    // Chia sẻ file ảnh sau khi tải về thành công
                    shareFile(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                   Log.d("TAG_IMAGE", "onLoadCleared: ")
                }
            })
    }

    private fun shareFile(imageFile: File) {
        // Tạo URI từ File thông qua FileProvider
        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.provider", // Thay đổi theo package name
            imageFile
        )

        // Tạo Intent chia sẻ
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Cho phép quyền đọc file
        }

        // Hiển thị dialog chọn ứng dụng
        startActivity(Intent.createChooser(shareIntent, "Share image via"))
    }


}