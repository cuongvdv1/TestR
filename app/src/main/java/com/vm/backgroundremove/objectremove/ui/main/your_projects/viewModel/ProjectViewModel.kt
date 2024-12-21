package com.vm.backgroundremove.objectremove.ui.main.your_projects.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.database.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ProjectViewModel(private val historyRepository: HistoryRepository): ViewModel() {
    private val _arrProcess = MutableStateFlow<List<HistoryModel>>(emptyList())
    val arrProcess: StateFlow<List<HistoryModel>> get() = _arrProcess


    init {
        getAllProcess()

    }

    fun getAllProcess() {
        viewModelScope.launch(Dispatchers.IO) {
            historyRepository.getAllProcesses().distinctUntilChanged().collect { processes ->
                _arrProcess.value = processes.reversed()
            }
        }
    }

    fun deleteHistory(historyModel: HistoryModel) {
        viewModelScope.launch(Dispatchers.IO) {
            historyRepository.deleteProcess(historyModel)
            getAllProcess()
        }
    }
    fun updatePhotoEditList() {
        viewModelScope.launch {
            arrProcess.collect { arrProcess ->
                val filteredList = arrProcess.filter { it.type == "remove_background_done" }
                _arrProcess.value = filteredList
            }
        }
    }
}
