package com.vm.backgroundremove.objectremove.ui.main.home

import android.content.Intent
import android.util.Log
import android.view.View

import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityHomeBinding
import com.vm.backgroundremove.objectremove.dialog.DialogExit
import com.vm.backgroundremove.objectremove.ui.common.setting.SettingActivity
import com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg.ChoosePhotoActivity
import com.vm.backgroundremove.objectremove.ui.main.progress.ProessingActivity
import com.vm.backgroundremove.objectremove.ui.main.progress.ProessingRefineActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.DownloadRemoveBackgroundActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.ProjectsActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.adapter.ProjectAdapter
import com.vm.backgroundremove.objectremove.ui.main.your_projects.viewModel.ProjectViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BaseActivity<ActivityHomeBinding, ProjectViewModel>(), DialogExit.OnPress {
    private var jobProcess: Job? = null
    private val projectAdapter by lazy { ProjectAdapter() }
    private lateinit var dialogExit : DialogExit
    override fun createBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun setViewModel() = viewModel<ProjectViewModel>().value

    override fun bindView() {
        super.bindView()

        projectAdapter.setOonDeleteClick {
            viewModel.deleteHistory(it)
            jobProcess = lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.arrProcess.collect { arrProcess ->
                        if (arrProcess.size == 0){
                            binding.ivEmpty.visibility = View.VISIBLE
                            binding.tvEmpty.visibility = View.VISIBLE
                        }else{

                            binding.ivEmpty.visibility = View.GONE
                            binding.tvEmpty.visibility = View.GONE
                            binding.rcvRecentProjects.visibility = View.VISIBLE
                        }
                        Log.d("HomeActivity", "Data size: ${arrProcess.size}")
                        if(arrProcess.size > 3){
                            projectAdapter.submitList(arrProcess.subList(0,3))
                        }else{
                            projectAdapter.submitList(arrProcess)
                        }
                    }
                }
            }

        }


        projectAdapter.setOnViewMoreClick {
            if (it.isSuccess()) {
                val intent =
                    Intent(this@HomeActivity, DownloadRemoveBackgroundActivity::class.java)
                intent.putExtra(Constants.INTENT_RESULT, it)
                startActivity(intent)


            } else {
                if (it.type.equals("rmobject_refine_obj")){
                    val intent = Intent(this, ProessingRefineActivity::class.java)
                    intent.putExtra(Constants.WORK_UUID, it.idWorker)
                    intent.putExtra(Constants.KEY_PROCESS, it)
                    intent.putExtra("type_process","remove_obj_by_list")
                    startActivity(intent)
                    finish()
                }else{
                    val intent = Intent(this, ProessingActivity::class.java)
                    intent.putExtra(Constants.WORK_UUID, it.idWorker)
                    intent.putExtra(Constants.KEY_PROCESS, it)
                    intent.putExtra("type_process",it.type)
                    startActivity(intent)
                    finish()
                }


            }
        }
    }

    override fun initView() {
        super.initView()
        dialogExit = DialogExit(this@HomeActivity)
        onBackPressedDispatcher.addCallback(this) {
            dialogExit.show()
        }
//        binding.rcvRecentProjects.visibility = View.VISIBLE
//        binding.ivEmpty.visibility = View.GONE
//        binding.tvEmpty.visibility = View.GONE

        binding.rcvRecentProjects.adapter = projectAdapter

        binding.rcvRecentProjects.itemAnimator = null


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
            val intent = Intent(this, ProjectsActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.ctlSetting.tap {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.tvViewAll.tap {
            val intent = Intent(this, ProjectsActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
    override fun viewModel() {
        super.viewModel()
        jobProcess = lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.arrProcess.collect { arrProcess ->
                    if (arrProcess.size == 0){
                        binding.ivEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.VISIBLE
                    }else{

                        binding.ivEmpty.visibility = View.GONE
                        binding.tvEmpty.visibility = View.GONE
                        binding.rcvRecentProjects.visibility = View.VISIBLE
                    }
                    Log.d("HomeActivity", "Data size: ${arrProcess.size}")
                    if(arrProcess.size > 3){
                        projectAdapter.submitList(arrProcess.subList(0,3))
                    }else{
                        projectAdapter.submitList(arrProcess)
                    }
                }
            }
        }
    }

    override fun send(s: Int) {
        finishAffinity()
    }
}