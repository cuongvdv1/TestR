package com.vm.backgroundremove.objectremove.ui.main.remove_object

import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveObjectBinding

class RemoveObjectActivity : BaseActivity<ActivityRemoveObjectBinding, BaseViewModel>(){
    override fun createBinding() = ActivityRemoveObjectBinding.inflate(layoutInflater)


    override fun setViewModel() = BaseViewModel()


    override fun initView() {
        super.initView()

        supportFragmentManager.beginTransaction().replace(R.id.fl_rm_object,RemoveObjectFragment()).commit()
        val imgPathGallery = intent.getStringExtra(Constants.IMG_GALLERY_PATH)
        val imagePathCamera = intent.getStringExtra(Constants.IMG_CAMERA_PATH)
        if (imgPathGallery != null) {
            binding.ivRmvObject.setImageFromPath(imgPathGallery)
        } else if (imagePathCamera != null) {
            binding.ivRmvObject.setImageFromPath(imagePathCamera)
        }


    }




}