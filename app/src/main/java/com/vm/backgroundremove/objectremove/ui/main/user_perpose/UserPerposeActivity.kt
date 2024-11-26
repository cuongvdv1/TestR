package com.vm.backgroundremove.objectremove.ui.main.user_perpose

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens.RemoteConfigScreenIntroModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.SystemUtil
import com.vm.backgroundremove.objectremove.databinding.ActivityUserPurposeBinding
import com.vm.backgroundremove.objectremove.ui.common.intro.IntroActivity


class UserPerposeActivity : BaseActivity<ActivityUserPurposeBinding, BaseViewModel>() {

    var isBgOn = false // Trạng thái của ivChooseRmvBg
    var isObjOn = false // Trạng thái của ivChooseRmvObj
    private var remoteConfigIntroModel: RemoteConfigScreenIntroModel? = null
    override fun createBinding() = ActivityUserPurposeBinding.inflate(layoutInflater)

    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        super.initView()
        window.statusBarColor = ContextCompat.getColor(this, R.color.color_F0F8FF)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding.ivChooseRmvBg.tap {
            if (isBgOn) {
                binding.ivChooseRmvBg.setBackgroundResource(R.drawable.ic_choose_purpose_off)
            } else {
                binding.ivChooseRmvBg.setBackgroundResource(R.drawable.ic_choose_purpose_on)
            }
            isBgOn = !isBgOn // Cập nhật trạng thái
            checkButtonsState() // Kiểm tra trạng thái của cả hai nút
        }

        binding.ivChooseRmvObj.tap {
            if (isObjOn) {
                binding.ivChooseRmvObj.setBackgroundResource(R.drawable.ic_choose_purpose_off)
            } else {
                binding.ivChooseRmvObj.setBackgroundResource(R.drawable.ic_choose_purpose_on)
            }
            isObjOn = !isObjOn // Cập nhật trạng thái
            checkButtonsState() // Kiểm tra trạng thái của cả hai nút
        }

        binding.ivClick.tap {
            nextAction()
        }

        binding.tvSkip.tap {
            nextAction()
        }
    }

    // Hàm kiểm tra trạng thái và hiển thị iv_click
    private fun checkButtonsState() {
        if (isBgOn && isObjOn) {
            binding.ivClick.visibility = View.VISIBLE // Hiển thị nút
        } else {
            binding.ivClick.visibility = View.GONE // Ẩn nút
        }
    }


    private fun nextAction() {
        val nextScreen = SystemUtil.getActivityClass(
            remoteConfigIntroModel?.next_screen_default,
            IntroActivity::class.java
        )


        val intent = Intent(this@UserPerposeActivity, nextScreen)
        startActivity(intent)
        finishAffinity()
    }


}