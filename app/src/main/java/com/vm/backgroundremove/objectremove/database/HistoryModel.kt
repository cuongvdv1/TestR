package com.vm.backgroundremove.objectremove.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize

const val FAILED_STATE = "not_found"
const val PROCESSING_STATE = "pending"
const val DONE_STATE = "done"

@Parcelize
@Entity
data class HistoryModel(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    var idWorker: String = "",
    var name: String = "",
    var imageResult: String? = null,
    var imageCreate: String? = null,
    var cf_url: String? = null,
    var task_id: String? = null,
    var time: Long = -1L,
    var process: Int = 0,
    var status: String? = null,
    var type: String = "",
    var other: String? = null,
) : Parcelable {

    fun convertToString(): String = Gson().toJson(this)

    fun isSuccess() =
        imageResult?.isNotEmpty() == true && imageResult != FAILED_STATE

    fun isFailed() = imageResult.isNullOrEmpty()  && status == FAILED_STATE

    fun isProcessing() =
        idWorker.isNotEmpty() && imageResult.isNullOrEmpty()


}
