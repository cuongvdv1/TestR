package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.a8_app_utils.parcelable
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResultRemoveBackGroundActivity :
    BaseActivity<ActivityRemoveBackgroundBinding, RemoveBackGroundViewModel>() {
    private var historyModel: HistoryModel? = null
    private var type = ""
    private var color =""
    override fun createBinding(): ActivityRemoveBackgroundBinding {
        return ActivityRemoveBackgroundBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value

    override fun initView() {
        super.initView()
        type = intent.getStringExtra(Constants.TYPE_HISTORY).toString()
        viewModel.color.observe(this){color->
            binding.ivRmvBg.setBackgroundWithColor(color)

        }
        try {
            historyModel = intent.parcelable<HistoryModel>(Constants.INTENT_RESULT)
            historyModel?.let {
                if (!it.imageResult.isNullOrEmpty()) {
                    Glide.with(this).asBitmap() // Chỉ định rằng bạn muốn tải Bitmap
                        .load(it.imageResult) // Tải hình ảnh từ file
                        .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap, transition: Transition<in Bitmap>?
                            ) {
                                // Đây là nơi bạn nhận được Bitmap
                                val bitmap: Bitmap = resource
                                binding.ivRmvBg.setBitmap(bitmap)

                            }
                            override fun onLoadCleared(placeholder: Drawable?) {
                                // Xử lý khi cần thiết, không có gì đặc biệt trong trường hợp này
                            }
                        })
                    Glide.with(this).asBitmap().load(it.imageResult)
                }
            }
        } catch (_: Exception) {
        }
        // xet cac su kien click
        // xet su kien back man


        binding.ivBack.tap {
            finish()
        }
        // Tạo fragment
        val fragment = ChooseBackGroundColorFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()
    }

    fun setNewImage() {
        binding.ivBeforeAfter.setImageResource(R.drawable.ic_selected)
        binding.ivRedo.visibility = View.GONE
        binding.ivUndo.setImageResource(R.drawable.ic_cancel)
    }
}