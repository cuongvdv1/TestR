package com.vm.backgroundremove.objectremove.api.repository


import com.vm.backgroundremove.objectremove.api.ApiResult
import com.vm.backgroundremove.objectremove.api.RemoveBackGroundApi
import com.vm.backgroundremove.objectremove.api.response.ResultImageResponse
import com.vm.backgroundremove.objectremove.api.safeApiCall


class ResultImageRepository(private val api: RemoveBackGroundApi) {
    suspend fun getImageResult(task: String, result: String): ApiResult<ResultImageResponse> {
        return safeApiCall {
            api.getImageResult(task, result)
        }
    }
}