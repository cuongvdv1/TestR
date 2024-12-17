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
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseFragment
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.FragmentColorBackgroundBinding
import com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg.ChoosePhotoActivity
import com.vm.backgroundremove.objectremove.ui.main.dialog.DialogBottomSheetPickColor
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.BackGroundAdapter
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.BackGroundSelectorListener
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorAdapter
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorSelectorListener
import com.vm.backgroundremove.objectremove.ui.main.remove_background.model.BackGroundModel
import com.vm.backgroundremove.objectremove.ui.main.remove_background.model.ColorModel
class ChooseBackGroundColorFragment : BaseFragment<FragmentColorBackgroundBinding>() {
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var backGroundAdapter: BackGroundAdapter
    private lateinit var viewModel: RemoveBackGroundViewModel
    private lateinit var colorList: List<ColorModel>
    private lateinit var backGroundList: List<BackGroundModel>
    var color_start : Int? = Color.parseColor("#FE23BE")
    var color_end : Int? = Color.parseColor("#A69CFC")
    var check_gradient : Boolean = false
    var check_single_color :Boolean = false
    private var _isClicked: Boolean = false
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

    override fun initView() {
        super.initView()

        colorList = listOf<ColorModel>(
            ColorModel(R.drawable.ic_none_color, "#FFFFFF",true),
            ColorModel(R.drawable.ic_circle_color, ""),
            ColorModel(R.drawable.ic_circle_color1, "#5A9CFF"),
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
                if(!_isClicked){
                    _isClicked = true
                    viewModel.setColor(Color.parseColor(color))
                    Log.d("TAG_COLOR", "onColorClicked: $color")
                }
                Handler().postDelayed({ _isClicked = false}, 300)
            }
        })

        viewBinding.rvListColor.adapter = colorAdapter

        // hien thi list background
        backGroundList = listOf<BackGroundModel>(
            BackGroundModel(R.drawable.ic_choose_bg),
            BackGroundModel(R.drawable.bg5,false),
            BackGroundModel(R.drawable.bg1,false),
            BackGroundModel(R.drawable.bg6,false),
            BackGroundModel(R.drawable.bg2,false),
            BackGroundModel(R.drawable.bg7,false),
            BackGroundModel(R.drawable.bg3,false),
            BackGroundModel(R.drawable.bg8,false),
            BackGroundModel(R.drawable.bg4,false),
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
        viewBinding.rvListBackground.adapter = backGroundAdapter


        // Chon mau cho BackGround
        colorAdapter.setActionListener(object : ColorSelectorListener {
            override fun onColorClicked(position: Int, color: String) {
                if (position == 1) {
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
                if(!_isClicked){
                    _isClicked = true
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
                    Handler().postDelayed({ _isClicked = false}, 300)
                }
            }
        })


        viewBinding.pickerColorFragment.setOnColorChange {
            if(!_isClicked) {
                Log.v("tag111", "color change: $it")
                customColor = it
                viewBinding.ivColorPicker.backgroundTintList = ColorStateList.valueOf(it)
            }
            Handler().postDelayed({_isClicked= false}, 100)
        }

        viewBinding.pickerColorFragment.setupWith(viewBinding.vView)
        viewBinding.vView.setOnValueChange {
            viewBinding.pickerColorFragment.setValue(it)
        }


        viewBinding.tvChooseBgColor.tap {
            if(!_isClicked) {
                _isClicked = true
                showColorList()
            }
            Handler().postDelayed({ _isClicked = false}, 500)
        }
        viewBinding.tvChooseBgImage.tap {
            if(!_isClicked) {
                _isClicked = true
                showBackgroundList()
            }
            Handler().postDelayed({ _isClicked = false}, 300)
        }
        viewBinding.tvPickerColorGradient.tap {
            if(!_isClicked) {
                _isClicked = true
                check_gradient = true
                check_single_color = false
                color_start?.let { it1 -> viewModel.setStartColor(it1) }
                color_end?.let { it1 -> viewModel.setEndColor(it1) }
                showChooseColorGradient()
            }
            Handler().postDelayed({ _isClicked = false}, 300)
        }
        viewBinding.tvPickerColorSingle.tap {
            if(!_isClicked) {
                _isClicked = true
                check_gradient = false
                check_single_color = true
                showPickerColor()
            }
            Handler().postDelayed({ _isClicked = false}, 300)
        }

        viewBinding.ivColorStart.tap {
            if(!_isClicked){
                _isClicked = true
            val colorPickerDialog = DialogBottomSheetPickColor()
            colorPickerDialog.setOnDone {
                color_start = it
                viewBinding.ivColorStart.backgroundTintList = ColorStateList.valueOf(it)
                Log.d("TAG_COLOR", "onColorClicked: $it")
                viewModel.setStartColor(it)
                viewModel.endColor.value?.let { endColor ->
                    var gradient = GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(it, endColor)
                    )
                    viewBinding.ivGradientColor.setImageDrawable(gradient)
                }
            }
            colorPickerDialog.show(parentFragmentManager, "ColorPickerBottomSheetDialog")
            }
            Handler().postDelayed({ _isClicked = false}, 300)
        }
        viewBinding.ivColorEnd.tap {
            if(!_isClicked){
                _isClicked = true
                val colorPickerDialog = DialogBottomSheetPickColor()
                colorPickerDialog.setOnDone {
                    color_end = it
                    viewBinding.ivColorEnd.backgroundTintList = ColorStateList.valueOf(it)
                    viewModel.setEndColor(it)
                    viewModel.startColor.value?.let { startColor ->
                        var gradient = GradientDrawable(
                            GradientDrawable.Orientation.LEFT_RIGHT,
                            intArrayOf(startColor, it)
                        )
                        viewBinding.ivGradientColor.setImageDrawable(gradient)
                    }
                }
                colorPickerDialog.show(parentFragmentManager, "ColorPickerBottomSheetDialog")
            }
            Handler().postDelayed({ _isClicked = false}, 300)
        }
        viewModel = ViewModelProvider(requireActivity())[RemoveBackGroundViewModel::class.java]
    }
    fun showColorList() {
        viewBinding.rvListColor.visibility = View.VISIBLE
        viewBinding.llChooseBackground.visibility = View.VISIBLE
        viewBinding.tvChooseBgColor.visibility = View.VISIBLE
        viewBinding.tvChooseBgImage.visibility = View.VISIBLE
        viewBinding.rvListBackground.visibility = View.GONE
        viewBinding.ctlPickerColor.visibility = View.GONE
        viewBinding.ctlPickerColorGradient.visibility = View.GONE
        viewBinding.ctlOptionChangeColorBg.visibility = View.GONE
        viewBinding.tvChooseBgImage.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_8F9DAA
            )
        )
        viewBinding.tvChooseBgColor.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_FF6846
            )
        )
        viewBinding.tvChooseBgColor.visibility = View.VISIBLE
        viewBinding.tvChooseBgImage.visibility = View.VISIBLE
        // Thay đổi trạng thái View nếu cần
        viewBinding.viewColorIndicator.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_FF6846
            )
        )
        viewBinding.viewBgIndicator.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_8F9DAA
            )
        )
    }

    fun showPickerColor() {
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.figtree_light)
        viewBinding.rvListColor.visibility = View.GONE
        viewBinding.llChooseBackground.visibility = View.GONE
        viewBinding.ctlOptionChangeColorBg.visibility = View.VISIBLE
        viewBinding.ctlPickerColor.visibility = View.VISIBLE
        viewBinding.ctlPickerColorGradient.visibility = View.GONE
        viewBinding.tvPickerColorGradient.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_8F9DAA
            )
        )
        viewBinding.tvPickerColorSingle.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_FF6846
            )
        )
        viewBinding.tvPickerColorGradient.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_8F9DAA
            )
        )
        viewBinding.tvPickerColorGradient.textSize = 14f
        viewBinding.tvPickerColorGradient.typeface = typeface
        viewBinding.tvPickerColorSingle.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_FF6846
            )
        )
        viewBinding.tvPickerColorSingle.textSize = 14f
        viewBinding.tvPickerColorSingle.typeface = typeface
        // Thay đổi trạng thái View nếu cần
        viewBinding.viewColor.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_FF6846
            )
        )
        viewBinding.viewBgGradient.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_8F9DAA
            )
        )

    }

    private fun showBackgroundList() {
        viewBinding.rvListColor.visibility = View.GONE
        viewBinding.rvListBackground.visibility = View.VISIBLE
        viewBinding.ctlPickerColor.visibility = View.GONE
        viewBinding.ctlPickerColorGradient.visibility = View.GONE
        viewBinding.ctlOptionChangeColorBg.visibility = View.GONE
        viewBinding.tvChooseBgImage.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_FF6846
            )
        )
        viewBinding.tvChooseBgColor.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_8F9DAA
            )
        )
        // Thay đổi trạng thái View nếu cần
        viewBinding.viewColorIndicator.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_8F9DAA
            )
        )
        viewBinding.viewBgIndicator.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_FF6846
            )
        )
    }

    private fun showChooseColorGradient() {
        viewBinding.ctlOptionChangeColorBg.visibility = View.VISIBLE
        viewBinding.ctlPickerColorGradient.visibility = View.VISIBLE
        viewBinding.tvPickerColorGradient.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_FF6846
            )
        )
        viewBinding.tvPickerColorSingle.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_8F9DAA
            )
        )
        viewBinding.ctlPickerColor.visibility = View.GONE
        // Thay đổi trạng thái View nếu cần
        viewBinding.viewColor.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_8F9DAA
            )
        )
        viewBinding.viewBgGradient.setBackgroundColor(
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

    private fun handleSingleClick(action: () -> Unit) {
        if (!_isClicked) {
            _isClicked = true
            action()
            Handler().postDelayed({ _isClicked = false }, 300)  // Đặt lại sau một khoảng thời gian
        }
    }

}