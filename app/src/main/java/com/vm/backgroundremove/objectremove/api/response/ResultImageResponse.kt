package com.vm.backgroundremove.objectremove.api.response

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.io.Serializable

data class ResultImageResponse (
    val img_url : List<ImgUrlResponse>,
    val other: List<String>,
    val success: Boolean,
    val status: String
): Serializable


@Parcelize
@Keep
data class ImgUrlResponse(
    val file_name: String,
    val image_url:String,
    val type : String
): Parcelable