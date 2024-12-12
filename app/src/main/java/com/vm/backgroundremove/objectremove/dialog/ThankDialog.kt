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
import android.widget.TextView
import com.vm.backgroundremove.objectremove.R


class ThankDialog @SuppressLint("NonConstantResourceId") constructor(context2: Context) :
    Dialog(context2, R.style.CustomDialogTheme) {

    private var onPress: OnPress? = null
    private val tvTitle: TextView
    private val tvContent: TextView
    private val context: Context
    private val tvGot: TextView


    init {

        this.context = context2
        setContentView(R.layout.thank_you_dialog)

        val attributes = window!!.attributes
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        window!!.attributes = attributes
        window!!.setSoftInputMode(16)
        hideNavigationBar()
        tvTitle = findViewById<TextView>(R.id.tv_title_ty)
        tvContent = findViewById<TextView>(R.id.tv_content_01)
        tvGot = findViewById<TextView>(R.id.tv_submit_got_it)


        tvGot.setOnClickListener {
            onPress?.gotIt()
            dismiss()
        }

    }


    interface OnPress {
        fun send(s: Int)

        fun rating(s: Int)

        fun cancel()

        fun later()

        fun gotIt()
    }

    fun init(context: Context?, onPress: OnPress?) {
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