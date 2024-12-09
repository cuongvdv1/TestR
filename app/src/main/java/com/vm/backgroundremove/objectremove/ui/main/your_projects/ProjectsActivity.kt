package com.vm.backgroundremove.objectremove.ui.main.your_projects

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lib.admob.interstitialAds.base.InterCallback
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityYourProjectsBinding
import com.vm.backgroundremove.objectremove.dialog.DetectingDialog
import com.vm.backgroundremove.objectremove.ui.common.setting.SettingActivity
import com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg.ChoosePhotoActivity
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import com.vm.backgroundremove.objectremove.ui.main.progress.ProessingActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.DownloadRemoveBackgroundActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.ResultRemoveBackGroundActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.adapter.ProjectAdapter
import com.vm.backgroundremove.objectremove.ui.main.your_projects.viewModel.ProjectViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProjectsActivity : BaseActivity<ActivityYourProjectsBinding, ProjectViewModel>() {

    private var jobProcess: Job? = null
    private val projectAdapter by lazy { ProjectAdapter() }

    override fun createBinding() = ActivityYourProjectsBinding.inflate(layoutInflater)

    override fun setViewModel() = viewModel<ProjectViewModel>().value

    override fun initView() {
        binding.rcvHistory.adapter = projectAdapter
        binding.rcvHistory.itemAnimator = null

        binding.ctlSetting.tap {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        binding.ctlHome.tap {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun bindView() {
        super.bindView()
        projectAdapter.setOnViewMoreClick {
            if (it.isSuccess()) {
                val intent =
                    Intent(this@ProjectsActivity,DownloadRemoveBackgroundActivity::class.java)
                intent.putExtra(Constants.INTENT_RESULT, it)
                startActivity(intent)


            } else {
                val intent = Intent(this, ProessingActivity::class.java)
                intent.putExtra(Constants.WORK_UUID, it.idWorker)
                intent.putExtra(Constants.KEY_PROCESS, it)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun viewModel() {
        super.viewModel()
        jobProcess = lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.arrProcess.collect { arrProcess ->
                    projectAdapter.submitList(arrProcess)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        jobProcess?.cancel()
    }
}
