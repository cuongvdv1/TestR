package com.vm.backgroundremove.objectremove.ui.main.your_projects.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.LAYOUT_INFLATER_SERVICE
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.HOUR_FORMAT
import com.vm.backgroundremove.objectremove.a8_app_utils.convertTime
import com.vm.backgroundremove.objectremove.a8_app_utils.toDp
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.database.HistoryRepository
import com.vm.backgroundremove.objectremove.databinding.ItemProjectBinding
import com.vm.backgroundremove.objectremove.databinding.PopupOptionHistoryBinding
import com.vm.backgroundremove.objectremove.ui.main.progress.ProcessViewModel
import com.vm.backgroundremove.objectremove.ui.main.your_projects.viewModel.ProjectViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.Executors


class ProjectAdapter(private val context: Context) :
    ListAdapter<HistoryModel, ProjectAdapter.ProjectViewHolder>(


        AsyncDifferConfig.Builder(diffCallback)
            .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
            .build(),


        ) {
    private var onViewMoreClick: (HistoryModel) -> Unit = {}

    private var onDeleteClick: (HistoryModel) -> Unit = {}

    private var isClickable = true
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProjectViewHolder(ItemProjectBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bindData(getItem(position))

    }

    fun setOonDeleteClick(onDeleteClick: (HistoryModel) -> Unit) {
        this.onDeleteClick = onDeleteClick
    }

    fun setOnViewMoreClick(onViewMoreClick: (HistoryModel) -> Unit) {
        this.onViewMoreClick = onViewMoreClick
    }

    inner class ProjectViewHolder(private val binding: ItemProjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var data: HistoryModel? = null

        init {
            binding.root.tap {
                if (isClickable) {
                    isClickable = false // Vô hiệu hóa click tiếp theo
                    data?.let(onViewMoreClick)
                    // Khôi phục trạng thái sau một khoảng thời gian (nếu cần)
                    binding.root.postDelayed({ isClickable = true }, 300)
                }
            }
        }

        fun bindData(data: HistoryModel) {
            this.data = data
            binding.tvItemName.text = data.name
            binding.tvTimeProcess.text = data.time.convertTime(HOUR_FORMAT)
            binding.tvDayProcess.text = data.time.convertTime()
            binding.progress.progress = data.process
            binding.tvProgress.text = "${data.process}%"

            if (data.isSuccess()) {
//                updateDatabase(data)
                Glide.with(binding.root.context).load(data.imageResult)
                    .placeholder(R.drawable.ic_image_processing).into(binding.imgProcess)
                binding.ivMenuPopup.setImageResource(R.drawable.ic_menu_on)
                binding.ivMenuPopup.tap {
                    if (isClickable) {
                        isClickable = false
                        showCustomMenu(binding.root, data)
                        binding.root.postDelayed({ isClickable = true }, 300)
                    }

                }

            } else if (data.isProcessing()) {
                Glide.with(binding.root.context).load(R.drawable.ic_image_processing)
                    .into(binding.imgProcess)
                binding.ivMenuPopup.setImageResource(R.drawable.ic_menu)
            }
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<HistoryModel>() {
            override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean {
                return oldItem == newItem
            }

        }
    }

    @SuppressLint("InflateParams")
    private fun showCustomMenu(view: View, data: HistoryModel?) {
        val bindingPopup = PopupOptionHistoryBinding.inflate(LayoutInflater.from(view.context))
        val inflater = view.context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.popup_option_history, null)
        val popupWindow = PopupWindow(
            bindingPopup.root,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f.toDp()

        bindingPopup.clReport.tap {
            data?.let(onViewMoreClick)
            popupWindow.dismiss()
        }
        // Thiết lập sự kiện cho từng item
        bindingPopup.clDownload.tap {
            CoroutineScope(Dispatchers.IO).launch {
                data?.imageResult?.let { imageUrl ->
                    Glide.with(context).asBitmap()
                        .load(imageUrl)
                        .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap, transition: Transition<in Bitmap>?
                            ) {
                                saveFileToDownload(context, resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                }
            }

            popupWindow.dismiss()

        }


        bindingPopup.clShare.tap {
            shareImage(data?.imageResult ?: "", view.context)
            popupWindow.dismiss()
        }

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = false

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth
        val popupHeight = popupView.measuredHeight

        popupWindow.width = popupWidth
        popupWindow.height = popupHeight

        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0] + view.width - (popupWidth * 1.2)
        val y = location[1] + 20f.toDp()
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x.toInt(), y.toInt())
    }


    private fun saveFileToDownload(context: Context, bitmap: Bitmap) {
        val resolver = context.contentResolver
        var name = System.currentTimeMillis().toString()
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/removebg")
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        Log.v("tag111", "gen success: $name")
        Toast.makeText(context, R.string.image_saved_successfully, Toast.LENGTH_SHORT).show()
    }


    private fun downloadImageFromUrl(context: Context, imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Tải bitmap từ URL
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .submit()
                    .get()

                val outputStream: OutputStream?

                // Tạo tên tệp ngẫu nhiên
                val randomFileName = "Image_${System.currentTimeMillis()}.jpg"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10 trở lên: Lưu vào MediaStore
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, randomFileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    if (uri == null) {
                        throw Exception("Failed to create URI for saving the image")
                    }
                    outputStream = context.contentResolver.openOutputStream(uri)
                } else {
                    // Android 9 trở xuống: Lưu vào thư mục Pictures
                    val downloadDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs()
                    }
                    val file = File(downloadDir, randomFileName)
                    outputStream = FileOutputStream(file)

                    // Thêm vào MediaStore để hiển thị trong thư viện
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, randomFileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.DATA, file.absolutePath)
                    }
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                }

                // Lưu bitmap vào file
                outputStream?.let {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    it.close()
                }

                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Image downloaded successfully", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {

                }
            }
        }
    }

    private fun shareImage(url: String, context: Context) {
        // Tải hình ảnh từ URL và lưu tạm vào cache
        Glide.with(context)
            .asFile()
            .load(url)
            .into(object : CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    // Chia sẻ file ảnh sau khi tải về thành công
                    shareFile(resource, context)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Xử lý nếu tải hình thất bại
                }
            })
    }


    private fun shareFile(imageFile: File, context: Context) {
        // Tạo URI từ File thông qua FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Thay đổi theo package name
            imageFile
        )

        // Tạo Intent chia sẻ
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Cho phép quyền đọc file
        }

        // Hiển thị dialog chọn ứng dụng
        (context as? Activity)?.startActivity(Intent.createChooser(shareIntent, "Share image via"))
    }
}