package com.vm.backgroundremove.objectremove.inteface
import com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg.model.ChoosePhotoModel

interface OnClickChoosePhoto {
    fun onClickItemCamera()
    fun onClickItemPhoto(data : ChoosePhotoModel)
}