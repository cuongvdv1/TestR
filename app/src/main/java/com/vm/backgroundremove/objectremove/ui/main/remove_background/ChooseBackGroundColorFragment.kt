package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.ui.main.dialog.DialogBottomSheetPickColor
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.BackGroundAdapter
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorAdapter
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorSelectorListener
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.backGroundSelectorListener
import com.vm.backgroundremove.objectremove.ui.main.remove_background.model.ColorModel

class ChooseBackGroundColorFragment : Fragment() {
    private lateinit var rcvColor: RecyclerView
    private lateinit var rcvBackGround: RecyclerView
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var backGroundAdapter: BackGroundAdapter
    private lateinit var ll_choose_bg : ConstraintLayout
    private lateinit var ctl_option_change_color_bg : ConstraintLayout
    private lateinit var ctl_picker_color : ConstraintLayout
    private lateinit var ctl_picker_color_gradient : ConstraintLayout
    private lateinit var ctl_choose_bg : ConstraintLayout
    private lateinit var ll_picker_color: ConstraintLayout
    private lateinit var view_color_indicator: View
    private lateinit var view_bg_indicator: View
    private lateinit var view_color: View
    private lateinit var view_bg_gradient: View
    private lateinit var tv_picker_color_single: TextView
    private lateinit var tv_picker_color_gradient: TextView
    private lateinit var tv_choose_bg_color: TextView
    private lateinit var tv_choose_bg_image: TextView
    private lateinit var iv_color_start :ImageView
    private lateinit var iv_color_end :ImageView
    private lateinit var color_bg : String
    private lateinit var viewModel: RemoveBackGroundViewModel



    private lateinit var colorList: List<ColorModel>
    private lateinit var backGroundList: List<Int>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_color_background, container, false)
        rcvColor = view.findViewById(R.id.rv_list_color)
        rcvBackGround = view.findViewById(R.id.rv_list_background)
        ctl_picker_color = view.findViewById(R.id.ctl_picker_color)
        ctl_choose_bg = view.findViewById(R.id.ctl_choose_bg)
        ctl_picker_color_gradient = view.findViewById(R.id.ctl_picker_color_gradient)
        ll_choose_bg = view.findViewById(R.id.ll_choose_background)
        ctl_option_change_color_bg = view.findViewById(R.id.ctl_option_change_color_bg)
        ll_picker_color = view.findViewById(R.id.ll_picker_color)
        tv_picker_color_single = view.findViewById(R.id.tv_picker_color_single)
        tv_picker_color_gradient = view.findViewById(R.id.tv_picker_color_gradient)
        tv_choose_bg_color = view.findViewById(R.id.tv_choose_bg_color)
        tv_choose_bg_image = view.findViewById(R.id.tv_choose_bg_image)
        iv_color_start = view.findViewById(R.id.iv_color_start)
        iv_color_end = view.findViewById(R.id.iv_color_end)
        view_color_indicator = view.findViewById(R.id.view_color_indicator)
        view_bg_indicator = view.findViewById(R.id.view_bg_indicator)
        view_color = view.findViewById(R.id.view_color)
        view_bg_gradient = view.findViewById(R.id.view_bg_gradient)

        viewModel = ViewModelProvider(requireActivity()).get(RemoveBackGroundViewModel::class.java)


        // hien thi list color
        colorList = listOf<ColorModel>(
            ColorModel(R.drawable.ic_none_color, "None"),
            ColorModel(R.drawable.ic_circle_color1,"#5A9CFF"),
            ColorModel( R.drawable.ic_circle_color,"None"),
            ColorModel( R.drawable.ic_circle_color2,"#76FF94"),
            ColorModel( R.drawable.ic_circle_color3,"#8ECE40"),
            ColorModel( R.drawable.ic_circle_color4,"#FFBF49"),
            ColorModel( R.drawable.ic_circle_color5,"#FF7755"),
            ColorModel( R.drawable.ic_circle_color6,"#F578B9"),
            ColorModel( R.drawable.ic_circle_color7,"#939393"),
            ColorModel( R.drawable.ic_circle_color8,"#ADADAD"),
            ColorModel( R.drawable.ic_circle_color9,"#FF7D7D"),
            ColorModel( R.drawable.ic_circle_color10,"#EB67BF"),
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
            override fun onColorClicked(position: Int, color:String) {
                if(position == 2){
                    showPickerColor()
                    (activity as ResultRemoveBackGroundActivity)?.setNewImage()
                }else{
                    viewModel.setColor(color)
                    Log.d("TAG_COLOR", "onColorClicked: $color")
                }
            }
        })
        backGroundAdapter.setActionListener(object : backGroundSelectorListener{
            override fun backGroundClicked(position: Int, background: String) {

            }
        })



        tv_choose_bg_color.tap {
            showColorList()
        }
        tv_choose_bg_image.tap {
            showBackgroundList()
        }
        tv_picker_color_gradient.tap {
            showChooseColorGradient()
        }
        tv_picker_color_single.tap {
            showPickerColor()
        }
        iv_color_start.tap {
            val colorPickerDialog = DialogBottomSheetPickColor()
            colorPickerDialog.show(parentFragmentManager, "ColorPickerBottomSheetDialog")
        }
        iv_color_end.tap {
            val colorPickerDialog = DialogBottomSheetPickColor()
            colorPickerDialog.show(parentFragmentManager, "ColorPickerBottomSheetDialog")
        }
        return view
    }
    fun showColorList() {
        rcvColor.visibility = View.VISIBLE
        rcvBackGround.visibility = View.GONE
        ctl_picker_color.visibility = View.GONE
        ctl_picker_color_gradient.visibility = View.GONE
        ctl_option_change_color_bg.visibility = View.GONE
        tv_choose_bg_color.visibility = View.VISIBLE
        tv_choose_bg_image.visibility = View.VISIBLE
        // Thay đổi trạng thái View nếu cần
        view_color_indicator.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_FF6846))
        view_bg_indicator.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_8F9DAA))


    }

    fun showPickerColor(){
        rcvColor.visibility = View.GONE
        ll_choose_bg.visibility = View.GONE
        ctl_option_change_color_bg.visibility = View.VISIBLE
        ctl_picker_color.visibility = View.VISIBLE
        ctl_picker_color_gradient.visibility = View.GONE
        tv_picker_color_gradient.setTextColor(ContextCompat.getColor(requireContext(),R.color.color_8F9DAA))
        tv_picker_color_single.setTextColor(ContextCompat.getColor(requireContext(),R.color.color_FF6846))
        // Thay đổi trạng thái View nếu cần
        view_color.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_FF6846))
        view_bg_gradient.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_8F9DAA))

    }
    fun showBackgroundList() {
        rcvColor.visibility = View.GONE
        rcvBackGround.visibility = View.VISIBLE
        ctl_picker_color.visibility = View.GONE
        ctl_picker_color_gradient.visibility = View.GONE
        ctl_option_change_color_bg.visibility = View.GONE
        tv_choose_bg_image.setTextColor(ContextCompat.getColor(requireContext(),R.color.color_8F9DAA))
        tv_choose_bg_color.setTextColor(ContextCompat.getColor(requireContext(),R.color.color_FF6846))
        // Thay đổi trạng thái View nếu cần
        view_color_indicator.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_8F9DAA))
        view_bg_indicator.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_FF6846))

    }

    fun showChooseColorGradient(){
        ctl_option_change_color_bg.visibility = View.VISIBLE
        ctl_picker_color_gradient.visibility = View.VISIBLE
        tv_picker_color_gradient.setTextColor(ContextCompat.getColor(requireContext(),R.color.color_FF6846))
        tv_picker_color_single.setTextColor(ContextCompat.getColor(requireContext(),R.color.color_8F9DAA))
        ctl_picker_color.visibility = View.GONE
        // Thay đổi trạng thái View nếu cần
        view_color.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_8F9DAA))
        view_bg_gradient.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_FF6846))
    }


}