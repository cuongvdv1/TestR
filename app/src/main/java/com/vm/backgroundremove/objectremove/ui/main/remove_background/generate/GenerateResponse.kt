package com.vm.backgroundremove.objectremove.ui.main.remove_background.generate

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class  GenerateResponse(
    var task_id: String = "",
    var cf_url: String? = "",
    var imageCreate: String = ""
) : Parcelable
