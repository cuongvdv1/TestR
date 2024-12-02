package com.vm.backgroundremove.objectremove.database


import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val processDao: HistoryDao) {
    fun getAllProcesses(): Flow<List<HistoryModel>> = processDao.getAllProcess()
    suspend fun insertProcess(processModel: HistoryModel): Long {
        return processDao.insertProcess(processModel)
    }

    suspend fun updateProcessWithImage(processId: Long, imgUrl: String) {
        processDao.updateImage(processId, imgUrl)
    }

    suspend fun updateProcessProgress(processId: Long, progress: Int) {
        processDao.updateProcess(processId, progress)
    }

    suspend fun updateStatus(processId: Long, status: String) {
        processDao.updateStatus(processId, status)
    }

    fun getRowCount(): Int = processDao.getRowCount()

    suspend fun deleteProcess(processModel: HistoryModel) {
        processDao.deleteProcess(processModel)
    }

    suspend fun getProcessByID(id: Long): HistoryModel = processDao.getProcessByID(id)


}