package com.vm.backgroundremove.objectremove.ui.main.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.ui.main.color_picker.HSView
import com.vm.backgroundremove.objectremove.ui.main.color_picker.VView

class DialogBottomSheetPickColor: BottomSheetDialogFragment() {
    private lateinit var iv_cancel: ImageView
    private lateinit var iv_selected: ImageView
    private lateinit var pickColor: HSView
    private lateinit var vView: VView
    var customColor: Int? = null

    private var onDone: (Int) -> Unit = {}

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
        iv_selected = view.findViewById(R.id.iv_selected_dialog_color)
        pickColor = view.findViewById(R.id.picker_color)
        vView = view.findViewById(R.id.vView_dialog)
        iv_cancel.setOnClickListener {
            dismiss()
        }
        iv_selected.setOnClickListener {
            dismiss()
        }
        pickColor.setOnColorChange {
            customColor = it
        }

        pickColor.setupWith(vView)
        vView.setOnValueChange {
            pickColor.setValue(it)
        }

        iv_selected.tap {
            customColor?.let(onDone)
            dismiss()
        }




        return  view



    }

    fun setOnDone(onDone: (Int) -> Unit) {
        this.onDone = onDone
    }
}