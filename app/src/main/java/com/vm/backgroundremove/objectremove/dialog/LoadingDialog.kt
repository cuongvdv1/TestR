package com.vm.backgroundremove.objectremove.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.vm.backgroundremove.objectremove.R

class LoadingDialog(context: Context) : Dialog(context, R.style.full_screen_dialog) {

    init {
        // Gắn layout vào dialog
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        setContentView(dialogView)
        hideNavigationBar()
        val attributes = window!!.attributes
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.width = (context.resources.displayMetrics.widthPixels * 0.8).toInt()
        window!!.attributes = attributes
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