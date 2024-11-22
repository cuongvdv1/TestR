package com.vm.backgroundremove.objectremove.ui.main.user_perpose

import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.databinding.ActivityUserPurposeBinding

class UserPerposeActivity:BaseActivity<ActivityUserPurposeBinding, BaseViewModel>() {
    override fun createBinding(): ActivityUserPurposeBinding {
        return ActivityUserPurposeBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }
}