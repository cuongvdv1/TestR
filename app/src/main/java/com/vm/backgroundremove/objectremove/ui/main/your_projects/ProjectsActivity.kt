package com.vm.backgroundremove.objectremove.ui.main.your_projects

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
import com.vm.backgroundremove.objectremove.databinding.ActivityYourProjectsBinding
import com.vm.backgroundremove.objectremove.dialog.DialogExit
import com.vm.backgroundremove.objectremove.ui.common.setting.SettingActivity
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import com.vm.backgroundremove.objectremove.ui.main.progress.ProcessingActivity
import com.vm.backgroundremove.objectremove.ui.main.progress.ProessingRefineActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist.RemoveObjectByListActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.adapter.ProjectAdapter
import com.vm.backgroundremove.objectremove.ui.main.your_projects.viewModel.ProjectViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProjectsActivity : BaseActivity<ActivityYourProjectsBinding, ProjectViewModel>(), DialogExit.OnPress  {

    private var jobProcess: Job? = null
    private val projectAdapter by lazy { ProjectAdapter() }
    private lateinit var dialogExit : DialogExit
    override fun createBinding() = ActivityYourProjectsBinding.inflate(layoutInflater)

    override fun setViewModel() = viewModel<ProjectViewModel>().value

    override fun initView() {
        binding.rcvHistory.adapter = projectAdapter

        binding.rcvHistory.itemAnimator = null

        binding.ctlSetting.tap {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.ctlHome.tap {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        dialogExit = DialogExit(this@ProjectsActivity)
        onBackPressedDispatcher.addCallback(this) {
            dialogExit.show()
        }
    }

    override fun bindView() {
        super.bindView()
        projectAdapter.setOonDeleteClick {
            viewModel.deleteHistory(it)
            jobProcess = lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.arrProcess.collect { arrProcess ->
                        if (arrProcess.size == 0){
                            binding.ivEmpty.visibility = View.VISIBLE
                            binding.rcvHistory.visibility = View.GONE
                            binding.clEmpty.visibility = View.VISIBLE
                            binding.tvEmpty.visibility = View.VISIBLE
                        }else{
                            binding.ivEmpty.visibility = View.GONE
                            binding.tvEmpty.visibility = View.GONE

                            binding.clEmpty.visibility = View.GONE
                            binding.rcvHistory.visibility = View.VISIBLE
                        }
                        projectAdapter.submitList(arrProcess)
                    }
                }
            }
        }
        projectAdapter.setOnViewMoreClick {
            if (it.isSuccess()) {
                if (it.type.equals("remove_obj_by_list")){
                    val intent = Intent(this, RemoveObjectByListActivity::class.java)
                    Log.d("TAG", "bindView: ${it.idWorker}")
                    intent.putExtra(Constants.INTENT_RESULT, it)
                    startActivity(intent)
                }
                else {
                    val intent =
                        Intent(this@ProjectsActivity, HistoryResultActivity::class.java)
                    intent.putExtra(Constants.INTENT_RESULT, it)
                    startActivity(intent)
                }

            } else {

                    val intent = Intent(this, ProcessingActivity::class.java)
                    intent.putExtra(Constants.WORK_UUID, it.idWorker)
                    intent.putExtra(Constants.KEY_PROCESS, it)
                    intent.putExtra("type_process",it.type)
                    startActivity(intent)
            }
        }
    }

    override fun viewModel() {
        super.viewModel()
        jobProcess = lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.arrProcess.collect { arrProcess ->

                    val filteredList = arrProcess.filter { it.type != "remove_obj_by_list" }
                    if (filteredList.size == 0){
                        binding.ivEmpty.visibility = View.VISIBLE
                        binding.rcvHistory.visibility = View.GONE
                        binding.clEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.VISIBLE
                    }else{
                        binding.ivEmpty.visibility = View.GONE
                        binding.tvEmpty.visibility = View.GONE

                        binding.clEmpty.visibility = View.GONE
                        binding.rcvHistory.visibility = View.VISIBLE
                    }
                    projectAdapter.submitList(filteredList)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        jobProcess?.cancel()
    }
    override fun send(s: Int) {
        finishAffinity()
    }
}
