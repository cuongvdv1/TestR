package com.vm.backgroundremove.objectremove.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Handler
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.databinding.DialogChooseRemoveObjectBinding
import com.vm.backgroundremove.objectremove.databinding.DialogDiscardChangesBinding

class ChooseOptionDialog(context2: Context) :
    Dialog(context2, R.style.CustomDialogTheme) {

    val binding = DialogChooseRemoveObjectBinding.inflate(layoutInflater)

    private var onPress: OnOptionObject? = null
    private var selectedOption: Int = 0 // Biến lưu trạng thái đã chọn: 0 = chưa chọn, 1 = Text, 2 = Object List

    init {
        setContentView(binding.root)

        val attributes = window!!.attributes
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
        window!!.attributes = attributes
        window!!.setSoftInputMode(16)

        hideNavigationBar()
        setCancelable(false)
        binding.tvYes.alpha = 0.5f
        binding.tvYes.isEnabled = false
        // Lắng nghe các tùy chọn được chọn
        binding.clOptionText.setOnClickListener {
            selectedOption = 1 // Text
            updateSelectionUI()
            binding.tvYes.alpha = 1.0f
            binding.tvYes.isEnabled = true
        }

        binding.clOptionObjectList.setOnClickListener {
            selectedOption = 2 // Object List
            updateSelectionUI()
            binding.tvYes.alpha = 1.0f
            binding.tvYes.isEnabled = true
        }

        // Gửi trạng thái khi nhấn OK
        binding.tvYes.setOnClickListener {
            onPress?.send(selectedOption)
            dismiss()
        }
    }

    private fun updateSelectionUI() {
        // Đổi trạng thái giao diện theo lựa chọn
        binding.ivChooseObjText.setImageResource(
            if (selectedOption == 1) R.drawable.ic_choose_obj_selected else R.drawable.ic_choose_obj_unselected
        )
        binding.ivChooseObjObjectList.setImageResource(
            if (selectedOption == 2) R.drawable.ic_choose_obj_selected else R.drawable.ic_choose_obj_unselected
        )
    }

    interface OnOptionObject {
        fun send(selectedOption: Int)
    }

    fun init(onPress: OnOptionObject?) {
        this.onPress = onPress
    }

    private fun hideNavigationBar() {
        // Ẩn thanh điều hướng (giữ nguyên như code ban đầu)
        val decorView = window?.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            decorView?.windowInsetsController?.let { controller ->
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            decorView?.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}
