package com.vm.backgroundremove.objectremove.ui.main.permission

import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.databinding.ActivityPermissionBinding

class PermissionActivity:BaseActivity<ActivityPermissionBinding,BaseViewModel>() {
    override fun createBinding(): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()
    }
}