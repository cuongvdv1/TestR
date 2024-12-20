package com.vm.backgroundremove.objectremove.ui.main.edit

import android.content.Intent
import android.os.Handler
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants

import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.database.HistoryRepository
import com.vm.backgroundremove.objectremove.databinding.ActivityChoosePhotoEditBinding
import com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg.ChoosePhotoActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.ResultRemoveBackGroundActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.viewModel.ProjectViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChoosePhotoEditActivity : BaseActivity<ActivityChoosePhotoEditBinding, ProjectViewModel>() {

    private var listPhotoRmv: List<HistoryModel> = emptyList()
    private lateinit var adapterPhotoEdit: ChoosePhotoEditAdapter
    private var historyModel : HistoryModel ?= null
    private var isClickable : Boolean = true


    private var jobProcess: Job? = null
    override fun createBinding(): ActivityChoosePhotoEditBinding {
        return ActivityChoosePhotoEditBinding.inflate(layoutInflater)
    }

    override fun setViewModel() = viewModel<ProjectViewModel>().value

    override fun initView() {
        super.initView()
        binding.ivBack.tap {
            finish()
        }
        adapterPhotoEdit = ChoosePhotoEditAdapter(this, onItemClick = { itemSelected ->
            if(isClickable){
                isClickable = false
                binding.ivSelected.visibility = View.VISIBLE
                historyModel = itemSelected
                var list = adapterPhotoEdit.listPhoto.map {
                    it.copy(isSelected = itemSelected.id == it.id)
                }
                adapterPhotoEdit.setData(list)
                Handler().postDelayed({ isClickable = true }, 500)
            }
        })

        binding.ivSelected.tap {
            val intent = Intent(this@ChoosePhotoEditActivity, ResultRemoveBackGroundActivity::class.java)
            intent.putExtra(Constants.INTENT_EDIT_FROM,Constants.INTENT_EDIT_FROM_HISTORY)
            intent.putExtra(Constants.INTENT_RESULT, historyModel)
            startActivity(intent)
        }
        binding.rvChoosePhoto.adapter = adapterPhotoEdit

        binding.btnTryNow.tap {
            val intent = Intent(this@ChoosePhotoEditActivity, ChoosePhotoActivity::class.java)
            intent.putExtra(Constants.NAME_INTENT_FROM_HOME,Constants.INTENT_FROM_HOME_TO_EDIT)
            startActivity(intent)
        }
    }

    fun setUI(isEmpty: Boolean){
        if (isEmpty) {
            binding.tvNoPhoto.visibility = View.VISIBLE
            binding.ivNoPhoto.visibility = View.VISIBLE
            binding.btnTryNow.visibility = View.VISIBLE
            binding.tvContent.visibility = View.GONE
        } else {
            binding.tvNoPhoto.visibility = View.GONE
            binding.ivNoPhoto.visibility = View.GONE
            binding.btnTryNow.visibility = View.GONE
            binding.tvContent.visibility = View.VISIBLE

        }
    }

    override fun viewModel() {
        super.viewModel()
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.arrProcess.collect { listPhotoEdit ->
                    val filter = listPhotoEdit.filter { it.type == "remove_background_done" }
                    listPhotoRmv = filter
                    adapterPhotoEdit.setData(filter)
                    setUI(filter.isEmpty())
                }
            }
        }

    }
}