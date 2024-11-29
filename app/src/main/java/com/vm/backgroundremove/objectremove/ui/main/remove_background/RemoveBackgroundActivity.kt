package com.vm.backgroundremove.objectremove.ui.main.remove_background
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveBackgroundBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorAdapter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream


class RemoveBackgroundActivity : BaseActivity<ActivityRemoveBackgroundBinding,RemoveBackGroundViewModel>() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var colorAdapter: ColorAdapter

    override fun createBinding(): ActivityRemoveBackgroundBinding {
        return ActivityRemoveBackgroundBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): RemoveBackGroundViewModel =
        viewModel<RemoveBackGroundViewModel>().value

    override fun initView() {
        super.initView()
        val fragment = ChooseBackGroundColorFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()
        // Chuyển đổi drawable sang MultipartBody.Part
        val drawable = resources.getDrawable(R.drawable.img_intro_4)
        val bitmap = (drawable as BitmapDrawable).bitmap

        // Chuyển Bitmap thành mảng byte
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        // Tạo RequestBody từ mảng byte
        val requestFile = RequestBody.create("image/png".toMediaTypeOrNull(), byteArray)

        // Tạo MultipartBody.Part
        val imagePart = MultipartBody.Part.createFormData("___payload_replace_img_src", "img_intro_4.png", requestFile)
        viewModel.upLoadImage(
            Constants.ITEM_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
            Constants.CLIENT_CODE.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
            Constants.CLIENT_MEMO.toRequestBody(Constants.TEXT_PLAIN.toMediaTypeOrNull()),
            imagePart
        )

        viewModel.upLoadImage.observe(this){response ->
            Log.d("tag12340","response $response")
        }
        binding.tvChooseBgOp1.tap {
            binding.tvChooseBgOp1.setTextColor(ContextCompat.getColor(this, R.color.color_FF0000))
            binding.tvChooseBgOp2.setTextColor(ContextCompat.getColor(this, R.color.color_37414B))
            binding.ivRedo.visibility = View.VISIBLE
            binding.ivUndo.setImageResource(R.drawable.ic_undo_off)
            binding.ivBeforeAfter.setImageResource(R.drawable.ic_before_after)
            binding.tvEdit.setText(R.string.edit)
            fragment.showColorList()
        }
        binding.tvChooseBgOp2.tap {
            binding.tvChooseBgOp2.setTextColor(ContextCompat.getColor(this, R.color.color_FF0000))
            binding.tvChooseBgOp1.setTextColor(ContextCompat.getColor(this, R.color.color_37414B))
            fragment.showBackgroundList()

        }
    }
    fun setNewImage(){
        binding.ivBeforeAfter.setImageResource(R.drawable.ic_selected)
        binding.ivRedo.visibility = View.GONE
        binding.ivUndo.setImageResource(R.drawable.ic_back)
        binding.tvChooseBgOp2.setText(R.string.gradient)
        binding.tvEdit.setText(R.string.color_picker)

    }

    override fun viewModel() {
        super.viewModel()
    }
}