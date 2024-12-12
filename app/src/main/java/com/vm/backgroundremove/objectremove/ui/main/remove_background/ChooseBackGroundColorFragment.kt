package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseFragment
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.FragmentColorBackgroundBinding
import com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg.ChoosePhotoActivity
import com.vm.backgroundremove.objectremove.ui.main.color_picker.HSView
import com.vm.backgroundremove.objectremove.ui.main.color_picker.VView
import com.vm.backgroundremove.objectremove.ui.main.dialog.DialogBottomSheetPickColor
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.BackGroundAdapter
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.BackGroundSelectorListener
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorAdapter
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorSelectorListener
import com.vm.backgroundremove.objectremove.ui.main.remove_background.model.BackGroundModel
import com.vm.backgroundremove.objectremove.ui.main.remove_background.model.ColorModel
import com.vm.backgroundremove.objectremove.util.Utils
import com.vm.backgroundremove.objectremove.util.getBitmapFrom
import org.koin.android.ext.android.get

class ChooseBackGroundColorFragment : BaseFragment<FragmentColorBackgroundBinding>() {
    private lateinit var rcvColor: RecyclerView
    private lateinit var rcvBackGround: RecyclerView
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var backGroundAdapter: BackGroundAdapter
    private lateinit var ll_choose_bg: ConstraintLayout
    private lateinit var ctl_option_change_color_bg: ConstraintLayout
    private lateinit var ctl_picker_color: ConstraintLayout
    private lateinit var ctl_picker_color_gradient: ConstraintLayout
    private lateinit var ctl_choose_bg: ConstraintLayout
    private lateinit var ll_picker_color: ConstraintLayout
    private lateinit var view_color_indicator: View
    private lateinit var view_bg_indicator: View
    private lateinit var view_color: View
    private lateinit var view_bg_gradient: View
    private lateinit var tv_picker_color_single: TextView
    private lateinit var tv_picker_color_gradient: TextView
    private lateinit var tv_choose_bg_color: TextView
    private lateinit var tv_choose_bg_image: TextView
    private lateinit var iv_color_start: ImageView
    private lateinit var iv_color_end: ImageView
    private lateinit var viewModel: RemoveBackGroundViewModel
    private lateinit var colorList: List<ColorModel>
    private lateinit var backGroundList: List<BackGroundModel>
    private lateinit var pickColor : HSView
    private lateinit var vView : VView
    private lateinit var iv_gradient_color :ImageView
    var color_start : Int? = Color.parseColor("#FE23BE")
    var color_end : Int? = Color.parseColor("#A69CFC")
    var check_gradient : Boolean = false
    var check_single_color :Boolean = false


    var customColor: Int? = null

    private val register =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val path_gallery = result.data?.getStringExtra(Constants.IMG_GALLERY_PATH)
                Log.d("TAG_URL", "path_gallery: $path_gallery")
                try {
                    if (path_gallery != null) {
                        val bitmap = getBitmapFromUri(requireActivity(), Uri.parse(path_gallery))
                        if (bitmap != null) {
                            viewModel.setBackGround(bitmap)
                            Log.e("TAG_URL", "bitmap: $bitmap")
                        } else {
                            Log.e("TAG_URL", "bitmap is null")

                        }
                    } else {
                        Log.e("TAG_URL", "path_gallery is null")
                    }
                } catch (e: Exception) {
                    Log.e("TAG_URL", "path_gallery  $e")
                    e.printStackTrace()
                }
            }

        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun inflateViewBinding(): FragmentColorBackgroundBinding {
        return FragmentColorBackgroundBinding.inflate(layoutInflater)
    }

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
        pickColor = view.findViewById(R.id.picker_color_fragment)
        vView = view.findViewById(R.id.vView)
        iv_gradient_color = view.findViewById(R.id.iv_gradient_color)

        viewModel = ViewModelProvider(requireActivity())[RemoveBackGroundViewModel::class.java]

        // hien thi list color
        colorList = listOf<ColorModel>(
            ColorModel(R.drawable.ic_none_color, "#FFFFFF"),
            ColorModel(R.drawable.ic_circle_color1, "#5A9CFF"),
            ColorModel(R.drawable.ic_circle_color, ""),
            ColorModel(R.drawable.ic_circle_color2, "#76FF94"),
            ColorModel(R.drawable.ic_circle_color3, "#8ECE40"),
            ColorModel(R.drawable.ic_circle_color4, "#FFBF49"),
            ColorModel(R.drawable.ic_circle_color5, "#FF7755"),
            ColorModel(R.drawable.ic_circle_color6, "#F578B9"),
            ColorModel(R.drawable.ic_circle_color7, "#939393"),
            ColorModel(R.drawable.ic_circle_color8, "#ADADAD"),
            ColorModel(R.drawable.ic_circle_color9, "#FF7D7D"),
            ColorModel(R.drawable.ic_circle_color10, "#EB67BF"),
        )
        colorAdapter = ColorAdapter(requireContext(), colorList, object : ColorSelectorListener {
            override fun onColorClicked(position: Int, color: String) {
                viewModel.setColor(Color.parseColor(color))
                Log.d("TAG_COLOR", "onColorClicked: $color")
            }
        })
        rcvColor.adapter = colorAdapter

        // hien thi list background
        backGroundList = listOf<BackGroundModel>(
            BackGroundModel(R.drawable.ic_choose_bg,),
            BackGroundModel(R.drawable.bg1,false),
            BackGroundModel(R.drawable.bg2,false),
            BackGroundModel(R.drawable.bg3,false),
            BackGroundModel(R.drawable.bg4,false),
            BackGroundModel(R.drawable.bg5,false),
            BackGroundModel(R.drawable.bg6,false),
            BackGroundModel(R.drawable.bg7,false),
            BackGroundModel(R.drawable.bg8,false),
            BackGroundModel(R.drawable.bg9,false),
            BackGroundModel(R.drawable.bg10,false),
            BackGroundModel(R.drawable.bg11,false),
            BackGroundModel(R.drawable.bg12,false),
            BackGroundModel(R.drawable.bg13,false),
            BackGroundModel(R.drawable.bg14,false),
            BackGroundModel(R.drawable.bg15,false),
            BackGroundModel(R.drawable.bg16,false),
            BackGroundModel(R.drawable.bg17,false),
            BackGroundModel(R.drawable.bg18,false),
            BackGroundModel(R.drawable.bg19,false),
            BackGroundModel(R.drawable.bg20,false),
            BackGroundModel(R.drawable.bg21,false),
            BackGroundModel(R.drawable.bg22,false),
            BackGroundModel(R.drawable.bg23,false),
            BackGroundModel(R.drawable.bg24,false),
            BackGroundModel(R.drawable.bg25,false)
        )
        backGroundAdapter = BackGroundAdapter(requireContext(), backGroundList)
        rcvBackGround.adapter = backGroundAdapter

        // Chon mau cho BackGround
        colorAdapter.setActionListener(object : ColorSelectorListener {
            override fun onColorClicked(position: Int, color: String) {
                if (position == 2) {
                    showPickerColor()
                    check_single_color = true
                    check_gradient = false
                    if (activity is ResultRemoveBackGroundActivity) {
                        (activity as ResultRemoveBackGroundActivity).setNewImage()
                    }
                } else if (position == 0) {
                    if (activity is ResultRemoveBackGroundActivity) {
                        (activity as ResultRemoveBackGroundActivity).clearBackground()
                    }
                } else {
                    check_single_color = false
                    check_gradient = true
                    viewModel.setColor(Color.parseColor(color))
                    Log.d("TAG_COLOR", "onColorClicked: $color")
                }
            }
        })

        // Chon anh BackGround cho BackGround
        backGroundAdapter.setActionListener(object : BackGroundSelectorListener {
            override fun onBackGroundClicked(position: Int) {
                Log.d("TAG_POSITION", "backGroundClicked: $position")
                if (position == 0) {
                    val intent = Intent(requireContext(), ChoosePhotoActivity::class.java)
                    intent.putExtra(
                        Constants.NAME_INTENT_FORM_FRAGMENT,
                        Constants.INTENT_FROM_FRAGMENT_CHOOSE_BG
                    )
                    register.launch(intent)
                } else {
                    val bitmap =
                        BitmapFactory.decodeResource(context?.resources, backGroundList[position].image)
                    viewModel.setBackGround(bitmap)
                }
            }
        })

        pickColor.setOnColorChange {
            Log.v("tag111", "color change: $it")
            customColor = it
        }

        pickColor.setupWith(vView)
        vView.setOnValueChange {
            pickColor.setValue(it)
        }

        tv_choose_bg_color.tap {
            showColorList()
        }
        tv_choose_bg_image.tap {
            showBackgroundList()
        }
        tv_picker_color_gradient.tap {
            check_gradient = true
            check_single_color = false
            color_start?.let { it1 -> viewModel.setStartColor(it1) }
            color_end?.let { it1 -> viewModel.setEndColor(it1) }
            showChooseColorGradient()
        }
        tv_picker_color_single.tap {
            check_gradient = false
            check_single_color = true
            showPickerColor()
        }
        iv_color_start.tap {
            val colorPickerDialog = DialogBottomSheetPickColor()
            colorPickerDialog.setOnDone {
                color_start = it
                iv_color_start.backgroundTintList = ColorStateList.valueOf(it)
                Log.d("TAG_COLOR", "onColorClicked: $it")
                viewModel.setStartColor(it)
                viewModel.endColor.value?.let { endColor ->
                    var gradient = GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(it, endColor)
                    )
                    iv_gradient_color.setImageDrawable(gradient)
                }
            }
            colorPickerDialog.show(parentFragmentManager, "ColorPickerBottomSheetDialog")
        }
        iv_color_end.tap {
            val colorPickerDialog = DialogBottomSheetPickColor()
            colorPickerDialog.setOnDone {
                color_end = it
                iv_color_end.backgroundTintList = ColorStateList.valueOf(it)
                viewModel.setEndColor(it)
                viewModel.startColor.value?.let { startColor ->
                    var gradient = GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(startColor, it)
                    )
                    iv_gradient_color.setImageDrawable(gradient)
                }
            }
            colorPickerDialog.show(parentFragmentManager, "ColorPickerBottomSheetDialog")
        }
        return view
    }

    fun showColorList() {
        rcvColor.visibility = View.VISIBLE
        ll_choose_bg.visibility = View.VISIBLE
        tv_choose_bg_color.visibility = View.VISIBLE
        tv_choose_bg_image.visibility = View.VISIBLE
        rcvBackGround.visibility = View.GONE
        ctl_picker_color.visibility = View.GONE
        ctl_picker_color_gradient.visibility = View.GONE
        ctl_option_change_color_bg.visibility = View.GONE
        tv_choose_bg_image.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_8F9DAA
            )
        )
        tv_choose_bg_color.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_FF6846
            )
        )
        tv_choose_bg_color.visibility = View.VISIBLE
        tv_choose_bg_image.visibility = View.VISIBLE
        // Thay đổi trạng thái View nếu cần
        view_color_indicator.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_FF6846
            )
        )
        view_bg_indicator.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_8F9DAA
            )
        )


    }

    fun showPickerColor() {
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.figtree_light)
        rcvColor.visibility = View.GONE
        ll_choose_bg.visibility = View.GONE
        ctl_option_change_color_bg.visibility = View.VISIBLE
        ctl_picker_color.visibility = View.VISIBLE
        ctl_picker_color_gradient.visibility = View.GONE
        tv_picker_color_gradient.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_8F9DAA
            )
        )
        tv_picker_color_single.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_FF6846
            )
        )
        tv_picker_color_gradient.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_8F9DAA
            )
        )
        tv_picker_color_gradient.textSize = 14f
        tv_picker_color_gradient.typeface = typeface
        tv_picker_color_single.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_FF6846
            )
        )
        tv_picker_color_single.textSize = 14f
        tv_picker_color_single.typeface = typeface
        // Thay đổi trạng thái View nếu cần
        view_color.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_FF6846
            )
        )
        view_bg_gradient.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_8F9DAA
            )
        )

    }

    private fun showBackgroundList() {
        rcvColor.visibility = View.GONE
        rcvBackGround.visibility = View.VISIBLE
        ctl_picker_color.visibility = View.GONE
        ctl_picker_color_gradient.visibility = View.GONE
        ctl_option_change_color_bg.visibility = View.GONE
        tv_choose_bg_image.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_FF6846
            )
        )
        tv_choose_bg_color.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_8F9DAA
            )
        )
        tv_choose_bg_image.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_FF6846
            )
        )
        tv_choose_bg_color.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_8F9DAA
            )
        )
        // Thay đổi trạng thái View nếu cần
        view_color_indicator.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_8F9DAA
            )
        )
        view_bg_indicator.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_FF6846
            )
        )

    }

    private fun showChooseColorGradient() {
        ctl_option_change_color_bg.visibility = View.VISIBLE
        ctl_picker_color_gradient.visibility = View.VISIBLE
        tv_picker_color_gradient.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_FF6846
            )
        )
        tv_picker_color_single.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_8F9DAA
            )
        )
        ctl_picker_color.visibility = View.GONE
        // Thay đổi trạng thái View nếu cần
        view_color.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_8F9DAA
            )
        )
        view_bg_gradient.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_FF6846
            )
        )
    }

    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e("TAG_URL", "Error decoding URI: $e")
            null
        }
    }

}