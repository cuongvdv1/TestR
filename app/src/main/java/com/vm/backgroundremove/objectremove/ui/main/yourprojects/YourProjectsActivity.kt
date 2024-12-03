package com.vm.backgroundremove.objectremove.ui.main.yourprojects

import android.content.Intent
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.databinding.ActivityYourProjectsBinding
import com.vm.backgroundremove.objectremove.ui.common.setting.SettingActivity
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_object.RemoveObjectActivity

//class YourProjectsActivity : BaseActivity<ActivityYourProjectsBinding, BaseViewModel>() {
//    override fun createBinding(): ActivityYourProjectsBinding {
//        return ActivityYourProjectsBinding.inflate(layoutInflater)
//    }
//
//    override fun setViewModel(): BaseViewModel {
//        return BaseViewModel()
//    }
//
//    override fun initView() {
//        super.initView()
//        binding.ctlHome.tap {
//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        binding.ctlSetting.tap {
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }
//}
