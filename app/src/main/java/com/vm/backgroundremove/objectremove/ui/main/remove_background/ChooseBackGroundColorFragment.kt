package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.ui.main.dialog.DialogBottomSheetPickColor
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.BackGroundAdapter
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorAdapter
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorSelectorListener

class ChooseBackGroundColorFragment : Fragment() {
    private lateinit var rcvColor: RecyclerView
    private lateinit var rcvBackGround: RecyclerView
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var backGroundAdapter: BackGroundAdapter
    private lateinit var ctl_picker_color: ConstraintLayout
    private lateinit var colorList: List<Int>
    private lateinit var backGroundList: List<Int>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_color_background, container, false)
        rcvColor = view.findViewById(R.id.rv_list_color)
        rcvBackGround = view.findViewById(R.id.rv_list_background)
        ctl_picker_color = view.findViewById(R.id.ctl_picker_color)


        // hien thi list color
        colorList = listOf<Int>(
            R.drawable.ic_none_color,
            R.drawable.ic_circle_color1,
            R.drawable.ic_circle_color,
            R.drawable.ic_circle_color2,
            R.drawable.ic_circle_color3,
            R.drawable.ic_circle_color4,
            R.drawable.ic_circle_color5,
            R.drawable.ic_circle_color6,
            R.drawable.ic_circle_color7,
            R.drawable.ic_circle_color8,
            R.drawable.ic_circle_color9,
            R.drawable.ic_circle_color10,
        )
        colorAdapter = ColorAdapter(requireContext(), colorList)
        rcvColor.adapter = colorAdapter

        // hien thi list background
        backGroundList = listOf<Int>(
            R.drawable.bg1,
            R.drawable.bg2,
            R.drawable.bg3,
            R.drawable.bg4,
            R.drawable.bg5,
            R.drawable.bg6,
            R.drawable.bg7,
            R.drawable.bg8,
            R.drawable.bg9,
            R.drawable.bg10,
            R.drawable.bg11,
            R.drawable.bg12,
            R.drawable.bg13,
            R.drawable.bg14,
            R.drawable.bg15,
            R.drawable.bg16,
            R.drawable.bg17,
            R.drawable.bg18,
            R.drawable.bg19,
            R.drawable.bg20,
            R.drawable.bg21,
            R.drawable.bg22,
            R.drawable.bg23,
            R.drawable.bg24,
            R.drawable.bg25,
        )
        backGroundAdapter = BackGroundAdapter(requireContext(), backGroundList)
        rcvBackGround.adapter = backGroundAdapter

        colorAdapter.setActionListener(object : ColorSelectorListener {
            override fun onColorClicked(position: Int) {
//                val bottomSheet = DialogBottomSheetPickColor()
//               bottomSheet.show(childFragmentManager, bottomSheet.tag)
                rcvColor.visibility = View.GONE
                ctl_picker_color.visibility = View.VISIBLE
                (activity as RemoveBackgroundActivity)?.setNewImage()

            }

        })

        return view
    }
    fun showColorList() {
        rcvColor.visibility = View.VISIBLE
        rcvBackGround.visibility = View.GONE
        ctl_picker_color.visibility = View.GONE
    }

    fun showBackgroundList() {
        rcvColor.visibility = View.GONE
        rcvBackGround.visibility = View.VISIBLE
        ctl_picker_color.visibility = View.GONE
    }

    fun showGradientColorPicker(){
        ctl_picker_color.visibility = View.VISIBLE
    }

}