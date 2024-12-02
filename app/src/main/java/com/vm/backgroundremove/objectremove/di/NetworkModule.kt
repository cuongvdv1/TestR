package com.vm.backgroundremove.objectremove.di


import android.util.Log
import com.vm.backgroundremove.objectremove.BuildConfig
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.api.RemoveBackGroundApi
import com.vm.backgroundremove.objectremove.api.repository.UpLoadImageRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

//dao add
val networkModule = module {
    single(named("GenerateOkHttpClient")) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val errorInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            if (!response.isSuccessful) {
                val errorBody = response.peekBody(Long.MAX_VALUE).string()
                Log.d("NetworkError", "Mã lỗi Generate: ${response.code}, Nội dung: $errorBody")
            }
            response
        }
        val headerInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val modifiedRequest = originalRequest.newBuilder()
                .addHeader(Constants.API_KEY, Constants.API_KEY_TOKEN)
                .build()
            chain.proceed(modifiedRequest)
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(errorInterceptor)
            .addInterceptor(headerInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // retrofit call api
    single(named("Generate")) {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(get(named("GenerateOkHttpClient"))) //okhttp GenerateOkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single {
        get<Retrofit>(named("Generate")).create(RemoveBackGroundApi::class.java)
    }
    single { UpLoadImageRepository(get<RemoveBackGroundApi>()) }

}