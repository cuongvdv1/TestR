package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorAdapter

class RemoveBackgroundActivity : BaseActivity<ActivityRemoveBackgroundBinding,BaseViewModel>() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var colorAdapter: ColorAdapter

    override fun createBinding(): ActivityRemoveBackgroundBinding {
        return ActivityRemoveBackgroundBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initView() {
        super.initView()

        val colors = listOf(
            Pair("color_FF37414B", R.color.color_FF37414B),
            Pair("color_FF6846", R.color.color_FF6846),
            Pair("color_FECE51",R.color.color_FECE51),
        )

        binding.tvChooseBgOp1.tap {
            val chooseBackGroundColorFragment = ChooseBackGroundColorFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, chooseBackGroundColorFragment)
                .commit()
        }

    }
}