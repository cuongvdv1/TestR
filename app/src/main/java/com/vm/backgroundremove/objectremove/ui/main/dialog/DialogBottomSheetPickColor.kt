package com.vm.backgroundremove.objectremove.ui.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vm.backgroundremove.objectremove.R

class DialogBottomSheetPickColor: BottomSheetDialogFragment() {
    private lateinit var iv_cancel: ImageView
    private lateinit var iv_selected: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutParams = dialog?.window?.attributes
        layoutParams?.flags = (layoutParams?.flags ?: 0) or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        dialog?.window?.attributes = layoutParams
        dialog?.window?.decorView?.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)


        val view = inflater.inflate(R.layout.bottomsheet_dialog_pick_color, container, false)
        iv_cancel = view.findViewById(R.id.iv_cancel)
        iv_selected = view.findViewById(R.id.iv_selected)
        iv_cancel.setOnClickListener {
            dismiss()
        }
        iv_selected.setOnClickListener {
            dismiss()
        }
        return  view

    }
}