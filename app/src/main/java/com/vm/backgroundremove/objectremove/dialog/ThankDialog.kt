package com.vm.backgroundremove.objectremove.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
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


}