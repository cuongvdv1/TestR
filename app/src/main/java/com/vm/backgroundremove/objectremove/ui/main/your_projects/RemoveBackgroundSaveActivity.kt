package com.vm.backgroundremove.objectremove.ui.main.your_projects

import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.databinding.ActivityYourProjectsResultBinding
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import java.io.File

class RemoveBackgroundSaveActivity :
    BaseActivity<ActivityYourProjectsResultBinding, BaseViewModel>() {
    private var imageUrl = ""
    override fun createBinding() = ActivityYourProjectsResultBinding.inflate(layoutInflater)
    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        super.initView()
        try {
            // Gắn link ảnh cố định
            val fixedImageUrl =
                "https://aphoto.vn/wp-content/uploads/2020/04/anh-dep-jpg-fujifilm-2.jpg" // Thay bằng URL ảnh cứng
            Glide.with(this@RemoveBackgroundSaveActivity)
                .load(fixedImageUrl)
                .into(binding.ivHistoryResult)

            imageUrl = fixedImageUrl // Gắn giá trị cho biến imageUrl
        } catch (_: Exception) {
        }


    }

    override fun bindView() {
        super.bindView()
        binding.llBtnExport.visibility = View.GONE
        binding.llBtnShare.visibility = View.GONE
        binding.tvTitleName.visibility =View.GONE
        binding.tvSaved.visibility = View.VISIBLE
        binding.icHome.visibility = View.VISIBLE
        binding.tvTitle.visibility = View.VISIBLE
        binding.llBtnShareWith.visibility = View.VISIBLE
        binding.llBtnShareWith.setOnClickListener {
            shareImage(imageUrl)
        }

        binding.icHome.tap {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        binding.ivBack.tap {
            finish()
        }


    }


    private fun shareImage(url: String) {
        // Tải hình ảnh từ URL và lưu tạm vào cache
        Glide.with(this)
            .asFile()
            .load(url)
            .into(object : CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    // Chia sẻ file ảnh sau khi tải về thành công
                    shareFile(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Xử lý nếu tải hình thất bại
                }
            })
    }


    private fun shareFile(imageFile: File) {
        // Tạo URI từ File thông qua FileProvider
        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.provider", // Thay đổi theo package name
            imageFile
        )

        // Tạo Intent chia sẻ
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Cho phép quyền đọc file
        }

        // Hiển thị dialog chọn ứng dụng
        startActivity(Intent.createChooser(shareIntent, "Share image via"))
    }


}