package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.vm.backgroundremove.objectremove.MainActivity
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityRemoveObjectBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.ChooseBackGroundColorFragment
import com.vm.backgroundremove.objectremove.util.getBitmapFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class RemoveObjectActivity : BaseActivity<ActivityRemoveObjectBinding, BaseViewModel>(){
    override fun createBinding() = ActivityRemoveObjectBinding.inflate(layoutInflater)
    override fun setViewModel() = BaseViewModel()


    override fun initView() {
        super.initView()

        val imgPathGallery = intent.getStringExtra(Constants.IMG_GALLERY_PATH)
        val imagePathCamera = intent.getStringExtra(Constants.IMG_CAMERA_PATH)

        val filePath = intent.getStringExtra(Constants.IMG_CATEGORY_PATH)
        Log.d("TAG123", "filePath: $filePath")
        if (!imagePathCamera.isNullOrEmpty()) {
//            Glide.with(this)
//                .load(File(imagePathCamera))
//                .skipMemoryCache(true)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into(binding.ivRmvBg)
            getBitmapFrom(this, imagePathCamera) {
                binding.ivRmvBg.setBitmap(it)
            }

        } else if (!imgPathGallery.isNullOrEmpty()) {
//            Glide.with(this)
//                .load(imgPathGallery)
//                .into(binding.ivRmvBg)
            getBitmapFrom(this, imgPathGallery) {
                binding.ivRmvBg.setBitmap(it)
            }
        }

        val fragment = RemoveObjectFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()


        binding.ivExport.tap {

        }

        binding.ivBack.tap {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }



}