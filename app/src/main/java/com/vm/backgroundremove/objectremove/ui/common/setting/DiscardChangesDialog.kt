package com.vm.backgroundremove.objectremove.ui.common.setting

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.TextView
import com.vm.backgroundremove.objectremove.R

class DiscardChangesDialog @SuppressLint("NonConstantResourceId") constructor(context2: Context) :
    Dialog(context2, R.style.CustomDialogTheme) {

    private var onPress: OnPress? = null
    private val tvTitle: TextView
    private val tvContent: TextView
    private val context: Context
    private val tvCancel: TextView
    private val tvYes: TextView

    init {

        this.context = context2
        setContentView(R.layout.dialog_discard_changes)

        val attributes = window!!.attributes
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        window!!.attributes = attributes
        window!!.setSoftInputMode(16)

        tvTitle = findViewById<TextView>(R.id.tv_title_ty)
        tvContent = findViewById<TextView>(R.id.tv_content_01)
        tvCancel = findViewById<TextView>(R.id.tv_cancel)
        tvYes = findViewById<TextView>(R.id.tv_yes)

        tvCancel.setOnClickListener {
            onPress?.gotIt()
            dismiss()
        }

        tvYes.setOnClickListener {
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