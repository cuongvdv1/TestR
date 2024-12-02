package com.vm.backgroundremove.objectremove.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.databinding.DialogDiscardChangesBinding

class DiscardChangesDialog @SuppressLint("NonConstantResourceId") constructor(context2: Context) :
    Dialog(context2, R.style.CustomDialogTheme) {

    val binding = DialogDiscardChangesBinding.inflate(layoutInflater)

    private var onPress: OnPress? = null


    init {
        // Sử dụng View Binding
        setContentView(binding.root)

        val attributes = window!!.attributes
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        window!!.attributes = attributes
        window!!.setSoftInputMode(16)



        binding.tvCancel.setOnClickListener {
            dismiss() // Đóng dialog
        }

        binding.tvYes.setOnClickListener {
            onPress?.send(1) // Gọi hàm xử lý quay lại màn hình
            dismiss() // Đóng dialog
        }

    }


    interface OnPress {
        fun send(s: Int) // Xử lý khi nhấn Yes
    }

    fun init(onPress: OnPress?) {
        this.onPress = onPress
    }


}