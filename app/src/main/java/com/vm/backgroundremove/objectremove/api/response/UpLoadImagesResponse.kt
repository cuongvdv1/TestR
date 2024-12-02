package com.vm.backgroundremove.objectremove.api.response

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class UpLoadImagesResponse(
    val cf_url: String,
    val status: String,
    val success: Boolean,
    val task_id: String
):Parcelable
