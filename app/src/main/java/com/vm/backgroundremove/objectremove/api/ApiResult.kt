package com.vm.backgroundremove.objectremove.api

//dao add
sealed class ApiResult<out T> {
    // Một lớp con cho kết quả thành công
    data class Success<out T>(val data: T) : ApiResult<T>()

    // Một lớp con cho kết quả lỗi
    data class Error(val code: Int, val message: String?) : ApiResult<Nothing>()

    // Một lớp con cho các ngoại lệ không phải lỗi HTTP
    data class Exception(val exception: Throwable) : ApiResult<Nothing>()
}

// Hàm an toàn call api trả về ApiResult
suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        // Thực hiện cuộc gọi API và lưu kết quả
        val response = apiCall()
        // Nếu thành công, trả về kết quả Success với dữ liệu
        ApiResult.Success(response)
    } catch (throwable: Throwable) {
        when (throwable) {
            // Nếu lỗi là HttpException do Retrofit gây ra
            is retrofit2.HttpException -> {
                ApiResult.Error(throwable.code(), throwable.message())
            }
            // Nếu lỗi không phải là HttpException
            else -> ApiResult.Exception(throwable)
        }
    }
}
