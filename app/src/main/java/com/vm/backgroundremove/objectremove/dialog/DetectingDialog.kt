package com.vm.backgroundremove.objectremove.dialog

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import com.vm.backgroundremove.objectremove.R

class DetectingDialog(context: Context) : Dialog(context, R.style.CustomDialogTheme) {

    init {
        // Gắn layout vào dialog
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_detecting, null)
        setContentView(dialogView)

        // Không cho phép đóng dialog khi nhấn bên ngoài
        setCancelable(false)
    }

    // Hàm hiển thị dialog với thời gian tự động đóng
    fun showWithTimeout(timeout: Long = 5000) {
        show()

        // Đóng dialog sau thời gian chờ
        Handler(Looper.getMainLooper()).postDelayed({
            dismiss()
        }, timeout)
    }


}