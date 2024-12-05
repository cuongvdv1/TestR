package com.vm.backgroundremove.objectremove.ui.main.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.v1.photo.enhance.ui.main.ai_portraits.generate.GenerateImageWorker
import com.v1.photo.enhance.ui.main.ai_portraits.generate.ITEM_GENERATE
import com.vm.backgroundremove.objectremove.a8_app_utils.ProcessState
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.database.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProcessViewModel(
    application: Application,
    private val dbHistoryRepository: HistoryRepository
) : AndroidViewModel(application) {
    var workManager = WorkManager.getInstance(application)
    var historyModel: HistoryModel? = null
    val trackProcessUUID: MutableLiveData<UUID> = MutableLiveData()
    var numProcessStateFlow: MutableStateFlow<Int> = MutableStateFlow(-1)
    var job: Job? = null
    private var countProgressJob: Job? = null

    //version 2  worker
    private fun startImageProcessing() {
        val data = Data.Builder().apply {
            putString(ITEM_GENERATE, historyModel?.convertToString())
        }
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<GenerateImageWorker>()
            .setConstraints(constraints)
            .setId(trackProcessUUID.value!!)
            .setInputData(data.build())
            .build()

        workManager.enqueue(workRequest)
    }

    fun newProcessItem(type: String, imageCreate: String, taskId: String, cfUrl: String) {
        historyModel = HistoryModel()
        historyModel?.imageCreate = imageCreate
        historyModel?.type = type
        historyModel?.cf_url = cfUrl
        historyModel?.task_id = taskId
        historyModel?.time = System.currentTimeMillis()
    }

    fun getProcessState(): ProcessState =
        if (historyModel?.isSuccess() == true)
            ProcessState.SUCCESS
        else if (historyModel?.isFailed() == true)
            ProcessState.FAILED
        else if (historyModel?.isProcessing() == true)
            ProcessState.PROCESSING
        else
            ProcessState.NEW


    fun getNumProgressing() {
        viewModelScope.launch(Dispatchers.IO) {
            val allProcessFlow = dbHistoryRepository.getAllProcesses()
            allProcessFlow.collect { arrProcessing ->
                // listener progress
                val item =
                    arrProcessing.find { it.idWorker == trackProcessUUID.value.toString() }
                item?.let {
                    historyModel = item
                }

                // calculator num processing
                val data = arrProcessing.filter { it.isProcessing() or it.isFailed() }
                if (data.isNotEmpty())
                    numProcessStateFlow.emit(data.size)
            }
        }
    }

    fun updateNumProcessing() {
        viewModelScope.launch {
            numProcessStateFlow.emit(numProcessStateFlow.value - 1)
        }
    }

    fun onNewID() {
        val uuid = UUID.randomUUID()
        historyModel?.idWorker = uuid.toString()
        setTrackUUID(uuid)
        startImageProcessing()
    }

    private fun setTrackUUID(id: UUID) {
        this.trackProcessUUID.value = id
    }

    fun cancelProcessListener() {
        job?.cancel()
    }

    fun resumeProcess(value: HistoryModel) {
        historyModel = value
        historyModel?.id = value.id
        setTrackUUID(UUID.fromString(historyModel?.idWorker))
    }


    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress


    fun retryProcess() {
        val uuid = UUID.fromString(historyModel?.idWorker)
        val worker = workManager.getWorkInfoById(uuid)
        if (worker.get().state.isFinished) {
            setTrackUUID(UUID.randomUUID())
            startImageProcessing()
        } else
            uuid?.let {
                setTrackUUID(uuid)
            }
    }

    fun cancelProgressTime() {
        countProgressJob?.cancel()
    }

}