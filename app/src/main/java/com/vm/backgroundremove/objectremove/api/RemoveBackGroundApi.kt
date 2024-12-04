package com.vm.backgroundremove.objectremove.api

import com.vm.backgroundremove.objectremove.api.response.ResultImageResponse
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RemoveBackGroundApi {
    @Multipart
    @POST("/c/prompt")
    suspend fun upLoadImage(
        @Part("item_code") item_code: RequestBody,
        @Part("client_code") client_code: RequestBody,
        @Part("client_memo") client_memo: RequestBody,
        @Part ___payload_replace_img_src: MultipartBody.Part
    ):UpLoadImagesResponse

    @GET("/c/result/{task_id}")
    suspend fun getImageResult(
        @Path ("task_id") task_id: String,
        @Query("cf_url") cf_url: String
    ): ResultImageResponse
}