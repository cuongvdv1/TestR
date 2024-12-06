package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.api.ApiResult
import com.vm.backgroundremove.objectremove.api.repository.UpLoadImageRepository
import com.vm.backgroundremove.objectremove.api.response.ResultImageResponse
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class RemoveBackGroundViewModel (private val upLoadImageRepository: UpLoadImageRepository): BaseViewModel() {
    private val _upLoadImage = MutableLiveData<UpLoadImagesResponse>()
    val upLoadImage: LiveData<UpLoadImagesResponse> = _upLoadImage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading :  LiveData<Boolean> = _isLoading

    private val _color = MutableLiveData<String>()
    val color : LiveData<String> get() = _color

    fun setColor(color: String) {
        _color.value = color
    }

    private val _backGround = MutableLiveData<Bitmap>()
    val backGround : LiveData<Bitmap> get() = _backGround

    fun setBackGround(backGround: Bitmap) {
        _backGround.value = backGround
    }

    private val _startColor = MutableLiveData<String>()
    val startColor : LiveData<String> get() = _startColor

    private val _endColor = MutableLiveData<String>()
    val endColor : LiveData<String> get() = _endColor

    fun setGradientBackGroundColor(startColor: String, endColor: String) {
        _startColor.value = startColor
        _endColor.value = endColor
    }


    fun upLoadImage(
        item_code: RequestBody,
        client_code: RequestBody,
        client_memo: RequestBody,
        ___payload_replace_img_src: MultipartBody.Part
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            when (val result = upLoadImageRepository.upLoadImageRepository(
                item_code,client_code,client_memo,___payload_replace_img_src
            )) {
                is ApiResult.Success -> {
                    Log.d("TAG123", "Success " + result.data.toString())
                    _upLoadImage.postValue(result.data)
                    _isLoading.postValue(false)
                }

                is ApiResult.Error -> {
                    _isLoading.postValue(false)
                    Log.d("TAG123", "Error " + result.message.toString())
                }

                is ApiResult.Exception -> {
                    _isLoading.postValue(false)
                    Log.d("TAG123", "Exception " + result.exception.toString())
                }
            }
        }

    }


}