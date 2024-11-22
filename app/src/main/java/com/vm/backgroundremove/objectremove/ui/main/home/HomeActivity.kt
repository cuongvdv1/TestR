package com.vm.backgroundremove.objectremove.ui.main.home

import android.content.Intent
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.databinding.ActivityHomeBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_backgound.RemoveBackgroundActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_object.RemoveObjectActivity

class HomeActivity:BaseActivity<ActivityHomeBinding,BaseViewModel>() {
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
        }

        binding.ctlOptionRemoveObj.tap {
            val intent = Intent(this, RemoveObjectActivity::class.java)
            startActivity(intent)
        }
    }
}