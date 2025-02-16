package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.api.ApiResult
import com.vm.backgroundremove.objectremove.api.repository.UpLoadImageRepository
import com.vm.backgroundremove.objectremove.api.response.UpLoadImagesResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class RemoveBackGroundViewModel(private val upLoadImageRepository: UpLoadImageRepository) :
    BaseViewModel() {
    private val _upLoadImage = MutableLiveData<UpLoadImagesResponse>()
    val upLoadImage: LiveData<UpLoadImagesResponse> = _upLoadImage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _color = MutableLiveData<Int>()
    val color: LiveData<Int> get() = _color

    private val _triggerRemove = MutableLiveData<Unit>()
    val triggerRemove: LiveData<Unit> get() = _triggerRemove

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> get() = _text

    private val _triggerRemoveByList = MutableLiveData<Unit>()
    val triggerRemoveByList: LiveData<Unit> get() = _triggerRemoveByList
    private val _triggerRemoveByListSelected = MutableLiveData<Unit>()
    val triggerRemoveByListSelected: LiveData<Unit> get() = _triggerRemoveByListSelected

    private val _textByList = MutableLiveData<String>()
    val textByList: LiveData<String> get() = _textByList

    private val _textByListSelected = MutableLiveData<String>()
    val textByListSelected: LiveData<String> get() = _textByListSelected


    private val _itemListObject = MutableLiveData<List<String>>()
    val itemListObject: LiveData<List<String>> get() = _itemListObject

    private val _itemDisabledState = MutableLiveData<List<String>>()
    val itemDisabledState: LiveData<List<String>> = _itemDisabledState

    fun setItemDisabledState(state: List<String>) {
        _itemDisabledState.value = state
    }
    fun setItemList(items: List<String>) {
        _itemListObject.value = items
    }


    fun setColor(color: Int) {
        _color.value = color
    }

    fun triggerRemoveByListSelected() {
        _triggerRemoveByListSelected.postValue(Unit)
    }

    fun triggerRemoveByList() {
        _triggerRemoveByList.postValue(Unit)
    }

    fun setTextByList(text: String) {
        _textByList.value = text
    }
    fun setTextByListSelected(text: String) {
        _textByListSelected.value = text
    }

    fun triggerRemove() {
        _triggerRemove.postValue(Unit)
    }

    fun setText(text: String) {
        _text.value = text
    }


    private val _backGround = MutableLiveData<Bitmap>()
    val backGround: LiveData<Bitmap> get() = _backGround

    fun setBackGround(backGround: Bitmap) {
        _backGround.value = backGround
    }

    private val _startColor = MutableLiveData<Int?>()
    val startColor: LiveData<Int?> get() = _startColor

    private val _endColor = MutableLiveData<Int?>()
    val endColor: LiveData<Int?> get() = _endColor

    fun setStartColor(startColor: Int?) {
        _startColor.value = startColor
    }
    fun setEndColor(endColor: Int?) {
        _endColor.value = endColor
    }

    fun upLoadImage(
        item_code: RequestBody,
        client_code: RequestBody,
        client_memo: RequestBody,
        ___payload_replace_img_src: MultipartBody.Part,
        ___payload_replace_text_obj: RequestBody
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            when (val result = upLoadImageRepository.upLoadImageRepository(
                item_code,
                client_code,
                client_memo,
                ___payload_replace_img_src,
                ___payload_replace_text_obj
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