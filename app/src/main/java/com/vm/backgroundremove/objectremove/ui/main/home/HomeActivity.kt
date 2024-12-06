package com.vm.backgroundremove.objectremove.ui.main.home

import android.content.Intent
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityHomeBinding
import com.vm.backgroundremove.objectremove.dialog.DetectingDialog
import com.vm.backgroundremove.objectremove.dialog.ProcessingDialog
import com.vm.backgroundremove.objectremove.ui.common.setting.SettingActivity
import com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg.ChoosePhotoActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.RemoveBackgroundSaveActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.YourProjectsActivity

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
            val intent = Intent(this, ChoosePhotoActivity::class.java)
            intent.putExtra(Constants.NAME_INTENT_FROM_HOME, Constants.INTENT_FROM_HOME_TO_BACKGROUND)
            startActivity (intent)
        }

        binding.ctlOptionRemoveObj.tap {
            val intent = Intent(this, ChoosePhotoActivity::class.java)
            intent.putExtra(Constants.NAME_INTENT_FROM_HOME, Constants.INTENT_FROM_HOME_TO_OBJECT)
            startActivity(intent)
        }
        binding.ctlYourProjects.tap {
            val intent = Intent(this, RemoveBackgroundSaveActivity::class.java)
            startActivity(intent)
        }

        binding.ctlSetting.tap {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        binding.tvViewAll.tap {
            // Hiển thị dialog
            val detectingDialog = DetectingDialog(this)
            detectingDialog.showWithTimeout(5000) // Hiển thị dialog trong 5 giây
        }

    }
}