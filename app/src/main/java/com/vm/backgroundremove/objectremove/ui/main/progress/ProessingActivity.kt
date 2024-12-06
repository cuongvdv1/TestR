package com.vm.backgroundremove.objectremove.ui.main.progress

import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.databinding.ActivityProessingBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.WorkInfo
import com.util.CheckInternet
import com.vm.backgroundremove.objectremove.MainActivity
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.a8_app_utils.ProcessState
import com.vm.backgroundremove.objectremove.a8_app_utils.convertToObject
import com.vm.backgroundremove.objectremove.a8_app_utils.getParcelable
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityProcessBinding
import com.vm.backgroundremove.objectremove.ui.common.nointernet.NoInternetActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.ResultRemoveBackGroundActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_background.generate.GenerateResponse
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

class ProessingActivity : BaseActivity<ActivityProcessBinding, ProcessViewModel>() {
    private var generateResponse: GenerateResponse? = null
    private var itemCode: String? = null
    private var taskId: String? = null
    private var cfUrl: String? = null
    private var uuid: String? = null
    override fun createBinding(): ActivityProcessBinding {
        return ActivityProcessBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): ProcessViewModel = viewModel<ProcessViewModel>().value

    override fun initView() {
        super.initView()
        generateResponse = intent.getParcelable<GenerateResponse>(RemoveBackgroundActivity.KEY_GENERATE)
        itemCode = intent.getStringExtra(Constants.ITEM_CODE)
        generateResponse?.let {
            taskId = it.task_id
            cfUrl = it.cf_url

            Log.d("ProcessActivity","$taskId")
            Log.d("ProcessActivity","$cfUrl")
        }

        uuid = intent.getStringExtra(Constants.WORK_UUID)
        if (!uuid.isNullOrEmpty()) {
            val uuid = UUID.fromString(uuid)
            viewModel.trackProcessUUID.value = uuid
            listenerWorker()
            viewModel.getNumProgressing()

//                binding.tvNumberPosition.text = getString(
//                    R.string.generate,
//                    viewModel.historyModel?.process
//                )
        } else {

            viewModel.newProcessItem("rmbg", "rmbg", taskId!!, cfUrl!!)
            viewModel.onNewID()
            viewModel.getNumProgressing()
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
//                    if (num > 0) {
//                        binding.tvNumberProcess.isVisible = true
//                        binding.tvNumberProcess.text = num.toString()
//                    } else binding.tvNumberProcess.isVisible = false
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

                                WorkInfo.State.FAILED -> {

                                    binding.tvYourPhoto.text ="your_request_cannot_be_nprocessed_at_this_time"
//                                        getString(R.string.)
                                    binding.tvYourNewLook.text = "sorry_for_the_inconvenience_please_try_again_later"
//                                        getString(R.string.)
                                    viewModel.updateNumProcessing()
                                    binding.prIndicator.indicatorColor = ContextCompat.getColor(
                                        this@ProessingActivity,
                                        R.color.endColor
                                    )
                                    binding.prIndicator.endColor = ContextCompat.getColor(
                                        this@ProessingActivity,
                                        R.color.color_000719
                                    )
                                    binding.txtProgress.text = "Error"
                                    binding.txtProgress.setTextColor(
                                        ContextCompat.getColor(
                                            this@ProessingActivity,
                                            R.color.color_00A3FF
                                        )
                                    )
                                    Log.d("ProcessActivity", "FAILED")
                                    viewModel.cancelProcessListener()
                                    binding.tvTimeEst.setTextColor(
                                        ContextCompat.getColor(
                                            this@ProessingActivity,
                                            R.color.endColor
                                        )
                                    )
                                    binding.tvNumberPosition.setTextColor(
                                        ContextCompat.getColor(
                                            this@ProessingActivity,
                                            R.color.color_00A3FF
                                        )
                                    )
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
                                            binding.tvTimeEst.text = "${timeProcess}s"
                                            binding.tvNumberPosition.text = "$positionProcess"
                                        }

                                        "doing" -> {
                                            binding.tvTimeEst.text = "${timeProcess}s"
                                            binding.tvNumberPosition.text = "$positionProcess"
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