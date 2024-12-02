package com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg

import android.content.Intent
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import java.io.File

class ResultImageGalleryActivity :
    BaseActivity<ActivityRemoveBackgroundBinding, BaseViewModel>() {
    override fun createBinding(): ActivityRemoveBackgroundBinding {
        return ActivityRemoveBackgroundBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

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

//        binding.tvSelected.tap {
//            val intent = Intent(this@ResultImageGalleryActivity, ImagePreviewActivity::class.java)
//            intent.putExtra(Constants.IMG_GALLERY_PATH, imgPathGallery)
//            intent.putExtra(Constants.IMG_CAMERA_PATH, imagePathCamera)
//            intent.putExtra(Constants.IMG_CATEGORY_PATH, filePath)
//            startActivity(intent)
//
//        }
//
//        binding.icBackAiPortraits.tap {
//            finish()
//        }
    }
}