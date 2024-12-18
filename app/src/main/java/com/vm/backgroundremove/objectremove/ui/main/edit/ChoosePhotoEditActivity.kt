package com.vm.backgroundremove.objectremove.ui.main.edit

import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.databinding.ActivityChoosePhotoEditBinding

class ChoosePhotoEditActivity : BaseActivity<ActivityChoosePhotoEditBinding,BaseViewModel>() {
    override fun createBinding(): ActivityChoosePhotoEditBinding {
        return ActivityChoosePhotoEditBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()

    }
}