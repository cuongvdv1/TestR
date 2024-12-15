package com.v1.photo.enhance.ui.main.ai_portraits.generate


import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.a8_app_utils.convertToObject
import com.vm.backgroundremove.objectremove.api.ApiResult
import com.vm.backgroundremove.objectremove.api.repository.ResultImageRepository
import com.vm.backgroundremove.objectremove.database.DONE_STATE
import com.vm.backgroundremove.objectremove.database.FAILED_STATE
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.database.HistoryRepository
import com.vm.backgroundremove.objectremove.database.PROCESSING_STATE
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val ITEM_GENERATE = "itemGenerate"

class GenerateImageWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {
    private val remoteResultRepository: ResultImageRepository by inject()
    private val dbHistoryRepository: HistoryRepository by inject()
    private var processId: Long = -1
    private val progressComplete = CompletableDeferred<Unit>()
    private var processModel: HistoryModel? = null
    private var progressPercentage: Int = 0
    override suspend fun doWork(): Result {
        processModel = inputData.getString(ITEM_GENERATE)?.convertToObject<HistoryModel>()

        val task_id = processModel?.task_id
        Log.d("GenerateImageWorker", "Task ID: $task_id")
        val cf_url = processModel?.cf_url
        Log.d("GenerateImageWorker", "sdUrl: $cf_url")
        val row = dbHistoryRepository.getRowCount()
        val rowCount = if (row > 0) row + 1 else 1

        processModel?.let {
            it.name = if (it.type == "remove_background") {
                val count = dbHistoryRepository.getRowRemoveBGCount()
                "Remove BG $count"
            } else {
                val count = dbHistoryRepository.getRowObjectRemoveCount()
                "Object Remove $count"
            }
        }

        Log.e("GenerateImageWorker", processModel.toString())

        // Lưu HistoryModel vào cơ sở dữ liệu và lấy `processId`
        CoroutineScope(Dispatchers.IO).launch {
            processId = dbHistoryRepository.insertProcess(processModel!!)
        }

        setProgressAsync(Data.Builder().putLong("processId", processId).build())

        try {
            val finalResult = pollForImageResult(task_id!!, cf_url!!)

            if (finalResult != null) {
                Log.d("YEUBANTRINH",finalResult.toString())
                return finalResult
            }

            return Result.failure()
        } finally {

        }
    }

    private suspend fun pollForImageResult(task_id: String, cf_url: String): Result? {
        var attempts = 0
        while (attempts < 10) { // Giới hạn số lần retry để tránh vòng lặp vô hạn

            when (val result = remoteResultRepository.getImageResult(task_id,cf_url )) {
                is ApiResult.Success -> {
                    val data = result.data
                    Log.d("GenerateImageWorker", "Data $data")
                    if (data.success) {
                        when (data.status) {
                            "done" -> {
                                // Task hoàn thành
                                val imageUrlResult = data.img_url[0].image_url
                                val other = data.other
                                CoroutineScope(Dispatchers.IO).launch {
                                    dbHistoryRepository.updateProcessWithImage(
                                        processId,
                                        imageUrlResult
                                    )
                                    dbHistoryRepository.updateOther(
                                        processId,
                                        other.toString()
                                    )
                                    dbHistoryRepository.updateStatus(processId, DONE_STATE)
                                    dbHistoryRepository.updateProcessProgress(processId, 100)
                                    processModel = dbHistoryRepository.getProcessByID(processId)
                                }
                                delay(300) // Đợi thêm 0.3 giây để đảm bảo mọi thứ đồng bộ
                                val processModelJson = processModel?.convertToString()
                                val output = Data.Builder()
                                    .putString(Constants.INTENT_HISTORY_WORKER, processModelJson)
                                    .build()
                                Log.d("GenerateImageWorker", "Success: Image URL = $imageUrlResult")
                                progressComplete.complete(Unit)
                                return Result.success(output)
                            }

                            "doing" -> {

                                // Task đang xử lý
                                val estimatedTime =
                                    (20..30).random() * 1000L // Thời gian xử lý ước tính


                                CoroutineScope(Dispatchers.IO).launch {
                                    dbHistoryRepository.updateStatus(processId, PROCESSING_STATE)
                                }
                                val startTime = System.currentTimeMillis()
                                CoroutineScope(Dispatchers.Default).launch {
                                    trackProgress(estimatedTime, startTime, "doing", 0)
                                }
                                Log.d(
                                    "GenerateImageWorker",
                                    "Status = doing. Estimated time = $estimatedTime ms"
                                )

                                delay(estimatedTime)
                            }

                            "todo" -> {
                                // Task trong hàng đợi
//                                val position = data.position?.toIntOrNull() ?: 0

                                val estimatedTime = (20 + 5) * 1000L
                                val startTime = System.currentTimeMillis()


                                CoroutineScope(Dispatchers.Default).launch {
                                    trackProgress(estimatedTime, startTime, "todo", 0)
                                }

                                CoroutineScope(Dispatchers.IO).launch {
                                    dbHistoryRepository.updateStatus(processId, PROCESSING_STATE)
                                }
                                Log.d(
                                    "GenerateImageWorker",
                                    "Status = todo. Position = 0.Estimated time = $estimatedTime ms"
                                )


                                delay(estimatedTime)
                            }

                            else -> {
                                // Trạng thái không xác định
                                Log.e("GenerateImageWorker", "Unknown status: ${data.status}")
                                return handleFailure("Unknown status received: ${data.status}")
                            }
                        }
                    } else {
                        // Trường hợp `success = false`
                        return handleFailure(data.status)
                    }
                }

                is ApiResult.Error -> {
                    Log.e("GenerateImageWorker", "Network error: ${result.message}")
                    return handleFailure("Network error: ${result.message}")
                }

                is ApiResult.Exception -> {
                    Log.e("GenerateImageWorker", "Exception: ${result.exception}")
                    return handleFailure("Exception occurred: ${result.exception.message}")
                }
            }
            attempts++
        }
        progressComplete.complete(Unit)
        return null // Nếu không thành công sau 10 lần thử, trả về thất bại
    }

    private suspend fun trackProgress(
        totalDelay: Long,
        startTime: Long,
        status: String,
        positionProcessing: Int
    ) {
        val updateInterval = 500L // Cập nhật mỗi 500ms
        while (true) {
            val elapsedTime = System.currentTimeMillis() - startTime
            progressPercentage =
                (elapsedTime.toDouble() / totalDelay * 100).coerceAtMost(100.0).toInt()
            val remainingTime = ((totalDelay - elapsedTime) / 1000).coerceAtLeast(0)

            // Log tiến trình
            Log.d(
                "TrackProgress",
                "Status: $status, Progress: $progressPercentage%, Position: $positionProcessing, Remaining time: $remainingTime seconds"
            )

            // Gửi progress đầy đủ ra Activity
            val progressData = Data.Builder()
                .putString("status", status)
                .putInt("positionProcessing", positionProcessing)
                .putString("timeProcessing", remainingTime.toString())
                .putInt("progress", progressPercentage)
                .build()

            setProgressAsync(progressData)

            CoroutineScope(Dispatchers.IO).launch {
                dbHistoryRepository.updateProcessProgress(processId, progressPercentage)
            }

            if (elapsedTime >= totalDelay) {
                // Hoàn thành tiến trình
                setProgressAsync(
                    Data.Builder()
                        .putInt("progress", 90)
                        .putString("status", status)
                        .putInt("positionProcessing", positionProcessing)
                        .putString("timeProcessing", "0") // Khi hoàn thành, thời gian còn lại = 0
                        .build()
                )
                break
            }

            delay(updateInterval)
        }
    }

    private suspend fun handleFailure(errorMessage: String): Result {
        withContext(Dispatchers.IO) {
            dbHistoryRepository.updateStatus(processId, FAILED_STATE)
            dbHistoryRepository.updateProcessProgress(processId, progressPercentage)
        }
        Log.e("GenerateImageWorker", "Task failed: $errorMessage")
        return Result.failure(
            Data.Builder().putString("error", errorMessage).build()
        )
    }
}