package com.vm.backgroundremove.objectremove.ui.common.update

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lib.admob.resumeAds.AppOpenResumeManager
import com.util.FirebaseLogEventUtils
import com.util.RemoteConfig
import com.util.appupdate.AppUpdateManager
import com.vm.backgroundremove.objectremove.MainActivity
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.RemoteConfigKey
import com.vm.backgroundremove.objectremove.a1_common_utils.ad.AdCommon
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens.RemoteConfigScreenAppUpdateModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.SystemUtil
import com.vm.backgroundremove.objectremove.databinding.ActivityAppUpdateBinding


class AppUpdateActivity : BaseActivity<ActivityAppUpdateBinding, BaseViewModel>() {

    private var remoteConfigScreenAppUpdateModel: RemoteConfigScreenAppUpdateModel? = null


    override fun createBinding() = ActivityAppUpdateBinding.inflate(layoutInflater)

    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        super.initView()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        binding.tvContent.setText(binding.tvContent.text.toString() + AppUpdateManager.updateContent)

        //btn update click
        binding.btnSubmit.tap {
            SystemUtil.openAppInPlayStore(this)
            val bundle = Bundle()

            AppOpenResumeManager.setEnableAdsResume(false)
            FirebaseLogEventUtils.logEventCommonWithName(this, "update_ok", bundle)
        }

        //btn cancel
        if (AppUpdateManager.checkUpdate == 2) {
            binding.btnCancel.visibility = View.GONE

        }

        //check next screen
        remoteConfigScreenAppUpdateModel = RemoteConfig.getConfigObject(
            this,
            RemoteConfigKey.screen_app_update,
            RemoteConfigScreenAppUpdateModel::class.java
        )

        var nextScreen = SystemUtil.getActivityClass(
            remoteConfigScreenAppUpdateModel?.next_screen_default,
            MainActivity::class.java
        )

        binding.btnCancel.tap {
            startActivity(Intent(this, nextScreen))
            val bundle = Bundle()
            FirebaseLogEventUtils.logEventCommonWithName(this, "update_cancel", bundle)
        }
        binding.ivWhatNew.tap {
            if (AppUpdateManager.updateContent.isNotEmpty()) {
                if (binding.tvContent.visibility == View.VISIBLE) {
                    binding.tvContent.visibility = View.GONE
                    binding.ivWhatNew.setBackgroundResource(R.drawable.ic_whatsnew)
                } else {
                    binding.tvContent.visibility = View.VISIBLE
                    binding.ivWhatNew.setBackgroundResource(R.drawable.ic_whatnew_dismiss)
                    if (!binding.tvContent.text.toString()
                            .contains(AppUpdateManager.updateContent)
                    ) {
                        binding.tvContent.text =
                            binding.tvContent.text.toString() + AppUpdateManager.updateContent
                    }
                }
            }
        }


    }

    override fun initAd() {
        //native
        AdCommon.loadAndShowAdNativeAdvance(
            lifecycle,
            RemoteConfigKey.ad_native_app_update,
            binding.frAd,
            null
        )
    }
}