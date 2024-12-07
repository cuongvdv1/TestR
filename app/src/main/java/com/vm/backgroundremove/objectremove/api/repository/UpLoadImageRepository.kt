package com.vm.backgroundremove.objectremove.api.repository

import com.vm.backgroundremove.objectremove.api.ApiResult
import com.vm.backgroundremove.objectremove.api.RemoveBackGroundApi
import com.vm.backgroundremove.objectremove.api.response.ResultImageResponse
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import com.vm.backgroundremove.objectremove.api.safeApiCall
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UpLoadImageRepository(private val api: RemoveBackGroundApi)  {
    suspend fun upLoadImageRepository(
        itemCode: RequestBody,
        client_code: RequestBody,
        client_memo: RequestBody,
        ___payload_replace_img_src: MultipartBody.Part,
        ___payload_replace_text_obj : RequestBody
    ) : ApiResult<UpLoadImagesResponse> {
        return safeApiCall {
            api.upLoadImage(itemCode,client_code, client_memo, ___payload_replace_img_src,___payload_replace_text_obj)
        }
    }

    suspend fun getImageResultRepository(
        task_id: String,
        cf_url: String

    ):ApiResult<ResultImageResponse>{
        return safeApiCall {
            api.getImageResult(task_id, cf_url)
        }
    }

}