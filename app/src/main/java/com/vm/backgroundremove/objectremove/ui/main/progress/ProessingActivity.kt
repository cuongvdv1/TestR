package com.vm.backgroundremove.objectremove.ui.main.progress

import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.WorkInfo
import com.util.CheckInternet
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.a8_app_utils.ProcessState
import com.vm.backgroundremove.objectremove.a8_app_utils.convertToObject
import com.vm.backgroundremove.objectremove.a8_app_utils.getParcelable
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityProcessBinding
import com.vm.backgroundremove.objectremove.ui.common.nointernet.NoInternetActivity
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity.Companion.KEY_REMOVE
import com.vm.backgroundremove.objectremove.ui.main.remove_background.ResultRemoveBackGroundActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.generate.GenerateResponse
import com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist.RemoveObjectByListActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist.ResultRemoveObjectByList
import com.vm.backgroundremove.objectremove.ui.main.remove_object.bytext.ResultRemoveObjectAndDowLoadActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.ProjectsActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.YourProjectsActivity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.util.UUID

class ProessingActivity : BaseActivity<ActivityProcessBinding, ProcessViewModel>() {
    private var generateResponse: GenerateResponse? = null
    private var itemCode: String? = null
    private var taskId: String? = null
    private var cfUrl: String? = null
    private var uuid: String? = null
    private var imageCreate: String? = null
    private var type_process : String? = null
    private var listOther : String? =null
    private var keyRemove : String? =null

    override fun createBinding(): ActivityProcessBinding {
        return ActivityProcessBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): ProcessViewModel = viewModel<ProcessViewModel>().value

    override fun initView() {
        super.initView()
        binding.icHomeGenerate.tap {
            startActivity(Intent(this@ProessingActivity,HomeActivity::class.java))
            finish()
        }
        binding.icHistory.tap {
            startActivity(Intent(this@ProessingActivity, ProjectsActivity::class.java))
            finish()
        }
        type_process = intent.getStringExtra("type_process")

        if (type_process == "remove_obj_by_list"){
        binding.tvYourPhoto.text = getString(R.string.your_photo_is_being_remove_object)
        }else if (type_process == "remove_obj_by_text"){
            binding.tvYourPhoto.text = getString(R.string.your_photo_is_being_remove_object)
        }else if (type_process == "remove_obj_by_list_text"){
            binding.tvYourPhoto.text = getString(R.string.your_photo_is_being_remove_object)
        }else{
            binding.tvYourPhoto.text = getString(R.string.your_photo_is_nbeing_remove_bg)
        }
        Log.d("ProessingActivity","ProessingActivity")
        generateResponse = intent.getParcelable<GenerateResponse>(RemoveBackgroundActivity.KEY_GENERATE)

        imageCreate = intent.getStringExtra("imageCreate")
        listOther = intent.getStringExtra("listOther")
        keyRemove = intent.getStringExtra(KEY_REMOVE)
        generateResponse?.let {
            taskId = it.task_id
            cfUrl = it.cf_url
            itemCode = it.imageCreate
            Log.d("ProcessActivity","$taskId")
            Log.d("ProcessActivity","$cfUrl")
            Log.d("ProcessActivity","$itemCode")
        }

        uuid = intent.getStringExtra(Constants.WORK_UUID)
        if (!uuid.isNullOrEmpty()) {
            val uuid = UUID.fromString(uuid)
            viewModel.trackProcessUUID.value = uuid
            listenerWorker()
            viewModel.getNumProgressing()
                binding.tvNumberPosition.text = viewModel.historyModel?.process?.toString() ?: ""
        } else {
            if (type_process == "remove_obj_by_list_text"){
                viewModel.newProcessItem(type_process!!, imageCreate!!, taskId!!, cfUrl!!)
                viewModel.onNewID()
                viewModel.getNumProgressing()
            }else{
                viewModel.newProcessItem(type_process!!, imageCreate!!, taskId!!, cfUrl!!)
                viewModel.onNewID()
                viewModel.getNumProgressing()
            }

        }
//        }
    }

    override fun bindView() {
        super.bindView()
    }

    @SuppressLint("SetTextI18n")
    override fun viewModel() {
        super.viewModel()
        when (viewModel.getProcessState()) {
            ProcessState.SUCCESS -> {
                Log.d("ProcessActivity", "ProcessState.SUCCESS")
            }

            ProcessState.FAILED -> {
                Log.d("ProcessActivity", "ProcessState.FAILED")
            }

            ProcessState.PROCESSING -> {
                Log.d("ProcessActivity", "ProcessState.PROCESSING")
            }

            ProcessState.NEW -> {
                Log.d("ProcessActivity", "ProcessState.NEW")
            }
        }

        viewModel.trackProcessUUID.observe(this) {
            listenerWorker()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.numProcessStateFlow.collect { num ->
                    Log.d("TAG1234", "$num")
                    if (num > 0) {
                        binding.tvTimeEst.isVisible = true
                        binding.tvTimeEst.text = num.toString()
                    } else binding.tvTimeEst.isVisible = false
                }
            }
        }

    }

    @SuppressLint("SetTextI18n", "StringFormatMatches")
    private fun listenerWorker() {
        val uuid = viewModel.trackProcessUUID.value
        if (uuid != null) {
            viewModel.job = lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.workManager.getWorkInfoByIdFlow(uuid)
                        .shareIn(lifecycleScope, SharingStarted.Eagerly).collect { workInfo ->
                            if (workInfo == null) {
                                return@collect
                            }
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    when (type_process){
                                        "remove_background" -> {
                                            val processModelJson =
                                                workInfo.outputData.getString(Constants.INTENT_HISTORY_WORKER)
                                            val processModel =
                                                processModelJson?.convertToObject<HistoryModel>()
                                            viewModel.updateNumProcessing()
                                            viewModel.cancelProcessListener()
                                            val intent =
                                                Intent(this@ProessingActivity, ResultRemoveBackGroundActivity::class.java)
                                            intent.putExtra(Constants.INTENT_RESULT, processModel)
                                            startActivity(intent)
                                            finish()
                                            Log.d("ProcessActivity", "SUCCEEDED  $processModel")
                                        }
                                        "remove_obj_by_text"-> {
                                            val processModelJson =
                                                workInfo.outputData.getString(Constants.INTENT_HISTORY_WORKER)
                                            val processModel =
                                                processModelJson?.convertToObject<HistoryModel>()
                                            viewModel.updateNumProcessing()
                                            viewModel.cancelProcessListener()
                                            val intent =
                                                Intent(this@ProessingActivity, ResultRemoveObjectAndDowLoadActivity::class.java)
                                            intent.putExtra(Constants.INTENT_RESULT, processModel)
                                            startActivity(intent)
                                            finish()
                                            Log.d("ProcessActivity", "SUCCEEDED  $processModel")
                                        }
                                        "remove_obj_by_list" -> {
                                            val processModelJson =
                                                workInfo.outputData.getString(Constants.INTENT_HISTORY_WORKER)
                                            val processModel =
                                                processModelJson?.convertToObject<HistoryModel>()
                                            viewModel.updateNumProcessing()
                                            viewModel.cancelProcessListener()
                                            val intent =
                                                Intent(this@ProessingActivity, RemoveObjectByListActivity::class.java)
                                            intent.putExtra(Constants.INTENT_RESULT, processModel)
//                                            intent.putStringArrayListExtra("item_list", arrayListOf(processModel))
                                            startActivity(intent)
                                            finish()
                                            Log.d("ProcessActivity", "SUCCEEDED  $processModel")
                                        }
                                        else ->{
                                            val processModelJson =
                                                workInfo.outputData.getString(Constants.INTENT_HISTORY_WORKER)
                                            val processModel =
                                                processModelJson?.convertToObject<HistoryModel>()
                                            viewModel.updateNumProcessing()
                                            viewModel.cancelProcessListener()
                                            val intent =
                                                Intent(this@ProessingActivity, ResultRemoveObjectByList::class.java)
                                            intent.putExtra(Constants.INTENT_RESULT, processModel)
                                            intent.putExtra("listOther",listOther)
                                            startActivity(intent)
                                            finish()
                                            Log.d("ProcessActivity", "SUCCEEDED  $processModel")
                                        }
                                    }
                                }

                                WorkInfo.State.FAILED -> {
                                    val positionProcess =
                                        workInfo.progress.getInt("positionProcessing", 0)
                                    binding.prIndicator.progress = 100f
                                    viewModel.updateNumProcessing()
                                    binding.prIndicator.indicatorColor = ContextCompat.getColor(
                                        this@ProessingActivity,
                                        R.color.color_F64534
                                    )

                                    binding.prIndicator.endColor = ContextCompat.getColor(
                                        this@ProessingActivity,
                                        R.color.color_F64534
                                    )
                                    binding.txtProgress.text = "Error"
                                    binding.txtProgress.setTextColor(
                                        ContextCompat.getColor(
                                            this@ProessingActivity,
                                            R.color.color_353B40
                                        )
                                    )
                                    Log.d("ProcessActivity", "FAILED")
                                    viewModel.cancelProcessListener()
                                    binding.tvTimeEst.setTextColor(
                                        ContextCompat.getColor(
                                            this@ProessingActivity,
                                            R.color.color_353B40
                                        )
                                    )
                                    binding.tvNumberPosition.setTextColor(
                                        ContextCompat.getColor(
                                            this@ProessingActivity,
                                            R.color.color_353B40
                                        )
                                    )
                                    binding.tvNumberPosition.text = "$positionProcess"
                                    binding.animationBird.pauseAnimation()
                                    binding.animationClock.pauseAnimation()
                                }

                                WorkInfo.State.CANCELLED -> Log.d("ProcessActivity", "CANCELLED")

                                WorkInfo.State.ENQUEUED -> Log.d("ProcessActivity", "ENQUEUED")

                                WorkInfo.State.RUNNING -> {
                                    if (!CheckInternet.haveNetworkConnection(this@ProessingActivity)) {
                                        startActivity(
                                            Intent(
                                                this@ProessingActivity,
                                                NoInternetActivity::class.java
                                            )
                                        )
                                    }

                                    Log.d("ProcessActivity", "RUNNING")
                                    val positionProcess =
                                        workInfo.progress.getInt("positionProcessing", 0)
                                    val timeProcess = workInfo.progress.getString("timeProcessing")
                                    val status = workInfo.progress.getString("status")
                                    when (status) {
                                        "todo" -> {
                                            if (binding.tvTimeEst.text != "${timeProcess}s" || binding.tvNumberPosition.text != "$positionProcess") {
                                                binding.tvTimeEst.text = "${timeProcess}s"
                                                binding.tvNumberPosition.text = "$positionProcess"
                                            }
                                        }

                                        "doing" -> {
                                            if (binding.tvTimeEst.text != "${timeProcess}s" || binding.tvNumberPosition.text != "$positionProcess") {
                                                binding.tvTimeEst.text = "${timeProcess}s"
                                                binding.tvNumberPosition.text = "$positionProcess"
                                            }
                                        }
                                    }

                                    val progress = workInfo.progress.getInt("progress", 0)

                                    val currentProgress = binding.prIndicator.progress

                                    val progressAnimator = ObjectAnimator.ofFloat(
                                        binding.prIndicator,
                                        "progress",
                                        currentProgress,
                                        progress.toFloat()
                                    )
                                    progressAnimator.duration = 500
                                    progressAnimator.interpolator =
                                        DecelerateInterpolator()
                                    progressAnimator.addUpdateListener { animation ->

                                        val animatedValue = animation.animatedValue as Float
                                        binding.txtProgress.text = "${animatedValue.toInt()}%"
                                    }
                                    progressAnimator.start()
                                    Log.d("ProcessActivity", "Progress: $progress%")
                                }

                                WorkInfo.State.BLOCKED -> Log.d("ProcessActivity", "BLOCKED")

                                else -> Log.d("ProcessActivity", "STATE Null")
                            }
                        }
                }
            }
        } else {
            Log.e("ProcessActivity", "UUID is null. Cannot start worker listener.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelProcessListener()
    }


}