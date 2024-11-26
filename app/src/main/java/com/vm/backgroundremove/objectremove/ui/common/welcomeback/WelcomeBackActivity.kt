package com.vm.backgroundremove.objectremove.ui.common.welcomeback

import com.util.RemoteConfig
import com.vm.backgroundremove.objectremove.a1_common_utils.RemoteConfigKey
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.app.RemoteConfigAppAllModel
import com.vm.backgroundremove.objectremove.databinding.ActivityWelcomeBackBinding

class WelcomeBackActivity : BaseActivity<ActivityWelcomeBackBinding, BaseViewModel>() {

    var remoteConfigAppAllModel: RemoteConfigAppAllModel? = null

    override fun createBinding() = ActivityWelcomeBackBinding.inflate(layoutInflater)

    override fun setViewModel() = BaseViewModel()


    override fun initView() {
        super.initView()

        remoteConfigAppAllModel = RemoteConfig.getConfigObject(
            this,
            RemoteConfigKey.app_config,
            RemoteConfigAppAllModel::class.java
        )

        binding.title.setText(remoteConfigAppAllModel?.welcomeback_tittle)

        binding.tvContinue.setOnClickListener {
            finish()
        }
    }

}