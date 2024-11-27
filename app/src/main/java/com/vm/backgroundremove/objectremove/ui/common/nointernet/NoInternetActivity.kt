package com.vm.backgroundremove.objectremove.ui.common.nointernet

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.util.CheckInternet
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.databinding.ActivityNoInternetBinding
import com.vm.backgroundremove.objectremove.ui.common.splash.SplashActivity


class NoInternetActivity : BaseActivity<ActivityNoInternetBinding, BaseViewModel>() {
    override fun createBinding() = ActivityNoInternetBinding.inflate(layoutInflater)
    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        super.initView()

        binding.tvRetry.setOnClickListener {
            if (CheckInternet.haveNetworkConnection(this)) {
                //Truong edit 20241008
                //fix khi co internet thi luon quay lai man splash
//                if (SplashWithProgessActivity.isLanguageStart) {
//                    startActivity(Intent(this, LanguageStartActivity::class.java))
//                    finish()
//                } else {
//                    finish()
//                }
                startActivity(Intent(this, SplashActivity::class.java))
                finish()

            } else {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (CheckInternet.haveNetworkConnection(this)) {
            //Truong edit 20241008
            //fix khi co internet thi luon quay lai man splash
//            if (SplashWithProgessActivity.isLanguageStart) {
//                startActivity(Intent(this, LanguageStartActivity::class.java))
//                finish()
//            } else {
//                finish()
//            }
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }
    }

}