package com.vm.backgroundremove.objectremove.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Handler
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.databinding.DialogDiscardChangesBinding

class DiscardChangesDialog(context2: Context) :
    Dialog(context2, R.style.CustomDialogTheme) {

    val binding = DialogDiscardChangesBinding.inflate(layoutInflater)

    private var onPress: OnPress? = null


    init {
        // Sử dụng View Binding
        setContentView(binding.root)

        val attributes = window!!.attributes
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.width = (context.resources.displayMetrics.widthPixels * 0.8).toInt()
        window!!.attributes = attributes
        window!!.setSoftInputMode(16)

        hideNavigationBar()
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
    private fun hideNavigationBar() {

        val decorView = window?.decorView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API level 30) and above
            decorView?.windowInsetsController?.let { controller ->
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Below Android 11
            decorView?.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )

            // Listener để ẩn lại thanh điều hướng khi người dùng tương tác
            decorView?.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    Handler().postDelayed({
                        decorView?.systemUiVisibility = (
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                )
                    }, 3000)
                }
            }
        }
    }



}