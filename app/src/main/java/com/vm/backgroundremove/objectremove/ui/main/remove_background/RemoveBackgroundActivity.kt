package com.vm.backgroundremove.objectremove.ui.main.remove_background
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorAdapter
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorSelectorListener


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
        val fragment = ChooseBackGroundColorFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()

        binding.tvChooseBgOp1.tap {
            binding.tvChooseBgOp1.setTextColor(ContextCompat.getColor(this, R.color.color_FF0000))
            binding.tvChooseBgOp2.setTextColor(ContextCompat.getColor(this, R.color.color_37414B))
            binding.ivRedo.visibility = View.VISIBLE
            binding.ivUndo.setImageResource(R.drawable.ic_undo_off)
            binding.ivBeforeAfter.setImageResource(R.drawable.ic_before_after)
            binding.tvEdit.setText(R.string.edit)
            fragment.showColorList()
        }
        binding.tvChooseBgOp2.tap {
            binding.tvChooseBgOp2.setTextColor(ContextCompat.getColor(this, R.color.color_FF0000))
            binding.tvChooseBgOp1.setTextColor(ContextCompat.getColor(this, R.color.color_37414B))
            fragment.showBackgroundList()

        }
    }
    fun setNewImage(){
        binding.ivBeforeAfter.setImageResource(R.drawable.ic_selected)
        binding.ivRedo.visibility = View.GONE
        binding.ivUndo.setImageResource(R.drawable.ic_back)
        binding.tvChooseBgOp2.setText(R.string.gradient)
        binding.tvEdit.setText(R.string.color_picker)

    }
}