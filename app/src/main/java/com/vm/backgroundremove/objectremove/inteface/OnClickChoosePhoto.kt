package com.vm.backgroundremove.objectremove.inteface
import com.v1.photo.enhance.ui.main.ai_portraits.choose_photo.model.ChoosePhotoModel

interface OnClickChoosePhoto {
    fun onClickItemCamera()
    fun onClickItemPhoto(data : ChoosePhotoModel)
}