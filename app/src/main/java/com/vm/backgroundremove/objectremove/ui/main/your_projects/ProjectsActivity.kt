package com.vm.backgroundremove.objectremove.ui.main.your_projects

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.databinding.ActivityYourProjectsBinding
import com.vm.backgroundremove.objectremove.ui.main.your_projects.adapter.ProjectAdapter
import com.vm.backgroundremove.objectremove.ui.main.your_projects.viewModel.ProjectViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProjectsActivity: BaseActivity<ActivityYourProjectsBinding, ProjectViewModel>() {

    private var jobProcess: Job? = null
    private val projectAdapter by lazy { ProjectAdapter() }

    override fun createBinding() = ActivityYourProjectsBinding.inflate(layoutInflater)

    override fun setViewModel() = viewModel<ProjectViewModel>().value

    override fun initView() {
        binding.rcvHistory.adapter = projectAdapter
        binding.rcvHistory.itemAnimator = null
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
