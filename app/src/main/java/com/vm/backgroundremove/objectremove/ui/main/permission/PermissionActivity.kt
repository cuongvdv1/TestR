package com.vm.backgroundremove.objectremove.ui.main.permission

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.SharePrefUtils
import com.vm.backgroundremove.objectremove.databinding.ActivityPermissionBinding
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity

class PermissionActivity : BaseActivity<ActivityPermissionBinding, BaseViewModel>() {
    var countRequestPermission: Int = 0
    override fun createBinding(): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun onResume() {
        super.onResume()
        setStatusPermissionPhoto()
        setStatusPermissionNoti()
        checkAllPermissions()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun initView() {
        super.initView()
        SharePrefUtils.setFristTimePermission(this)
        SharePrefUtils.countPermission(this)
        setStatusPermissionPhoto()
        setStatusPermissionNoti()
        checkAllPermissions()
        binding.ivSwitchCamera.tap {
            countRequestPermission++
            if (checkPermissions()) {
                binding.ivSwitchCamera.setImageResource(R.drawable.ic_switch_on)
            } else {
                if (countRequestPermission <= 2) {
                    requestPermissionPhoto()
                } else {
                    dialogPermission()
                    showDialogPermission()
                    setStatusPermissionPhoto()
                    checkAllPermissions()
                }
            }
        }
        binding.ivSwitchNoti.tap{
            countRequestPermission++
            if(checkNotiPermission()){
                binding.ivSwitchNoti.setImageResource(R.drawable.ic_switch_on)
            }else{
                if(countRequestPermission<=2){
                    requestPermissionNoti()
                }else{
                    dialogPermission()
                    showDialogPermission()
                    setStatusPermissionNoti()
                    checkAllPermissions()
                }
            }
        }


        binding.ivSelectedPermission.tap {
            startActivity(Intent(this@PermissionActivity, HomeActivity::class.java))
            finishAffinity()
        }
        binding.tvContinue.tap {
            startActivity(Intent(this@PermissionActivity, HomeActivity::class.java))
            finishAffinity()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }
    fun setStatusPermissionPhoto() {
        if (checkPermissions()) {
            binding.ivSwitchCamera.setImageResource(R.drawable.ic_switch_on)
        } else {
            binding.ivSwitchCamera.setImageResource(R.drawable.ic_switch_off)
        }
    }
    fun setStatusPermissionNoti() {
        if(checkNotiPermission()){
            binding.ivSwitchNoti.setImageResource(R.drawable.ic_switch_on)
        }else{
            binding.ivSwitchNoti.setImageResource(R.drawable.ic_switch_off)
        }
    }
    fun checkAllPermissions() {
        if(checkPermissions() && checkNotiPermission()){
            binding.ivSelectedPermission.visibility =View.VISIBLE
        }else{
            binding.ivSelectedPermission.visibility = View.GONE
        }
    }
}