package com.vm.backgroundremove.objectremove.ui.main.home

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.databinding.ActivityHomeBinding
import com.vm.backgroundremove.objectremove.dialog.DetectingDialog
import com.vm.backgroundremove.objectremove.dialog.ProcessingDialog
import com.vm.backgroundremove.objectremove.ui.common.setting.SettingActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_object.RemoveObjectActivity

class HomeActivity : BaseActivity<ActivityHomeBinding, BaseViewModel>() {
    override fun createBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()

        binding.ctlOptionRemoveBg.tap {
            val intent = Intent(this, RemoveBackgroundActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.ctlOptionRemoveObj.tap {
            val intent = Intent(this, RemoveObjectActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.ctlSetting.tap {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.tvViewAll.tap {
            // Hiển thị dialog
            val detectingDialog = DetectingDialog(this)
            detectingDialog.showWithTimeout(5000) // Hiển thị dialog trong 5 giây
        }

        binding.ctlYourProjects.tap {
            // Hiển thị dialog
            val processingDialog = ProcessingDialog(this)
            processingDialog.showWithTimeout(5000) // Hiển thị dialog trong 5 giây
        }
    }
}