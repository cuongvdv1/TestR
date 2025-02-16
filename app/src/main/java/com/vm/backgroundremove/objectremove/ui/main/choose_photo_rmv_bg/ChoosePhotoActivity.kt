package com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vm.backgroundremove.objectremove.BuildConfig
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.Constants
import com.vm.backgroundremove.objectremove.databinding.ActivityChoosePhotoBinding
import com.vm.backgroundremove.objectremove.inteface.OnClickChoosePhoto
import com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg.adapter.ChoosePhotoAdapter
import com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg.model.ChoosePhotoModel
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackgroundActivity
import com.vm.backgroundremove.objectremove.ui.main.remove_object.RemoveObjectActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ChoosePhotoActivity : BaseActivity<ActivityChoosePhotoBinding, BaseViewModel>() {
    private var uriPhoto = ""
    private val REQUEST_CODE_PERMISSION = 1
    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_IMAGE_CAPTURE = 101
    private var imageFile: File? = null
    private var selectedOutputPath: String? = null
    private var selectedImagePath: String? = null
    private var checkRemove: String? = null
    override fun createBinding(): ActivityChoosePhotoBinding {
        return ActivityChoosePhotoBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun onResume() {
        super.onResume()
        if(checkStorePermission()){
            showPhotoView()
            getAllImageInfos()
        }else{
            showNoPhotoView()
        }
    }

    override fun initView() {
        super.initView()
        dialogPermission()
        if(checkStorePermission()){
            showPhotoView()
            getAllImageInfos()
        }else{
            showNoPhotoView(    )
        }

        binding.ivBack.tap {
            finish()
        }

        binding.btnTryNow.tap {
            if(!checkStorePermission()){
                requestPermissionPhoto()
            }
        }

        checkRemove = intent.getStringExtra(Constants.NAME_INTENT_FROM_HOME).toString()
        val checkRemoveFragment =
            intent.getStringExtra(Constants.NAME_INTENT_FORM_FRAGMENT).toString()
        binding.ivSelected.tap {
            Log.d("TAG_CHECK", "TRUE")
            when {
                checkRemove == Constants.INTENT_FROM_HOME_TO_BACKGROUND -> {
                    val intent =
                        Intent(this@ChoosePhotoActivity, RemoveBackgroundActivity::class.java)
                    intent.putExtra(Constants.IMG_GALLERY_PATH, uriPhoto)
                    startActivity(intent)
                    finish()
                }

                checkRemove == Constants.INTENT_FROM_HOME_TO_OBJECT -> {
                    val intent = Intent(this@ChoosePhotoActivity, RemoveObjectActivity::class.java)
                    intent.putExtra(Constants.IMG_GALLERY_PATH, uriPhoto)
                    startActivity(intent)
                    finish()
                }

                checkRemoveFragment == Constants.INTENT_FROM_FRAGMENT_CHOOSE_BG -> {
                    val intent = Intent()
                    intent.putExtra(Constants.IMG_GALLERY_PATH, uriPhoto)
                    Log.d("TAG_URL", "${uriPhoto}")
                    setResult(RESULT_OK, intent)
                    finish()
                }

                checkRemove == Constants.INTENT_FROM_HOME_TO_EDIT ->{
                    val intent = Intent(this@ChoosePhotoActivity, RemoveBackgroundActivity::class.java)
                    intent.putExtra(Constants.IMG_GALLERY_PATH, uriPhoto)
                    startActivity(intent)
                    finish()
                }
            }

        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                showNoPhotoView()
                requestPermission()
            } else {
                getAllImageInfos()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                showNoPhotoView()
                requestPermission()
            } else {
                getAllImageInfos()
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getAllImageInfos()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    REQUEST_CODE_PERMISSION
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getAllImageInfos()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAllImageInfos()
            } else {
                dialogPermission()
                showDialogPermission()
            }
        }
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toOpenCameraNew()
            } else {
                dialogPermission()
                showDialogPermission()
            }
        }
    }


    private fun getAllImageInfos() {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        val imageInfos = mutableListOf<ChoosePhotoModel>()

        contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
            val columnIndexId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val columnIndexName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val imageId = cursor.getLong(columnIndexId)
                val imageName = cursor.getString(columnIndexName)
                val imagePath = cursor.getString(columnIndexData)

                val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    .buildUpon()
                    .appendPath(imageId.toString())
                    .build()

                val imageInfo = ChoosePhotoModel(
                    uri = imageUri.toString(),
                    name = imageName,
                    path = imagePath
                )

                imageInfos.add(imageInfo)
            }
        }
        if (imageInfos.isEmpty()) {
            showNoPhotoView()
        } else {
            binding.ivNoPhoto.visibility = View.GONE
            binding.rvChoosePhoto.visibility = View.VISIBLE
            setupRecyclerView(imageInfos)
        }

    }

    private fun showNoPhotoView() {
        binding.tvStartRemoving.visibility = View.VISIBLE
        binding.btnTryNow.visibility = View.VISIBLE
        binding.tvNoPhoto.visibility = View.VISIBLE
        binding.ivNoPhoto.visibility = View.VISIBLE
        binding.rvChoosePhoto.visibility = View.GONE
    }
    private fun showPhotoView() {
        binding.tvStartRemoving.visibility = View.GONE
        binding.btnTryNow.visibility = View.GONE
        binding.tvNoPhoto.visibility = View.GONE
        binding.ivNoPhoto.visibility = View.GONE
    }



    private fun setupRecyclerView(allImage: List<ChoosePhotoModel>) {
        val mutableListPhoto = allImage.toMutableList()
        val drawableUri =
            Uri.parse("android.resource://${this.packageName}/${R.drawable.img_camera}")

        val cameraPhoto = ChoosePhotoModel(
            uri = drawableUri.toString(),
            name = "Camera",
            path = "drawable/img_camera"
        )
        mutableListPhoto.add(0, cameraPhoto)
        val adapter = ChoosePhotoAdapter(this, mutableListPhoto, object : OnClickChoosePhoto {
            override fun onClickItemCamera() {
                // Kiểm tra quyền truy cập camera
                if (ContextCompat.checkSelfPermission(
                        this@ChoosePhotoActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Nếu đã có quyền, mở camera
                    toOpenCameraNew()
                } else {
                    // Nếu chưa có quyền, yêu cầu quyền
                    ActivityCompat.requestPermissions(
                        this@ChoosePhotoActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION
                    )
                }
            }

            override fun onClickItemPhoto(data: ChoosePhotoModel) {
                uriPhoto = data.uri
                Log.d("TAG_URL", "uriPhoto: $uriPhoto")
                binding.ivSelected.visibility = View.VISIBLE
            }

        })
        binding.rvChoosePhoto.layoutManager = GridLayoutManager(this, 3)
        binding.rvChoosePhoto.adapter = adapter
        binding.rvChoosePhoto.itemAnimator = null
    }


    @SuppressLint("QueryPermissionsNeeded")
    private fun toOpenCameraNew() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(
            "android.intent.extras.CAMERA_FACING",
            CameraCharacteristics.LENS_FACING_FRONT
        )
        val photoURI: Uri = FileProvider.getUriForFile(
            applicationContext,
            "${BuildConfig.APPLICATION_ID}.provider",
            createImageFile()
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            cameraResultLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && !selectedOutputPath.isNullOrEmpty()) {

                when {
                    checkRemove == Constants.INTENT_FROM_HOME_TO_BACKGROUND -> {
                        selectedImagePath = selectedOutputPath
                        val intent =
                            Intent(this@ChoosePhotoActivity, RemoveBackgroundActivity::class.java)
                        intent.putExtra(Constants.IMG_CAMERA_PATH, selectedImagePath)
                        startActivity(intent)
                        finish()
                    }

                    checkRemove == Constants.INTENT_FROM_HOME_TO_OBJECT -> {
                        selectedImagePath = selectedOutputPath
                        val intent =
                            Intent(this@ChoosePhotoActivity, RemoveObjectActivity::class.java)
                        intent.putExtra(Constants.IMG_CAMERA_PATH, selectedImagePath)
                        startActivity(intent)
                        finish()
                    }
                    checkRemove == Constants.INTENT_FROM_HOME_TO_EDIT->{
                        selectedImagePath = selectedOutputPath
                        val intent =
                            Intent(this@ChoosePhotoActivity, RemoveBackgroundActivity::class.java)
                        intent.putExtra(Constants.IMG_CAMERA_PATH, selectedImagePath)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

    private fun createImageFile(): File {
        val storageDir = File(
            getExternalFilesDir(null),
            "CamPic/"
        )
        storageDir.mkdirs()
        val image = File(storageDir, "Temp${System.currentTimeMillis()}.jpg")
        image.createNewFile()
        selectedOutputPath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            // Lưu ảnh vào bộ nhớ tạm
            val imageBitmap = data?.extras?.get(Constants.DATA) as Bitmap
            imageFile = saveBitmapToCache(imageBitmap)

            imageFile?.let {
                // Chuyển sang Activity khác và gửi đường dẫn ảnh
                val intent = Intent(this, RemoveBackgroundActivity::class.java)
                intent.putExtra(Constants.IMG_CAMERA_PATH, it.absolutePath)
                startActivity(intent)
            }


        }
    }

    // Lưu ảnh vào bộ nhớ tạm (Cache)
    private fun saveBitmapToCache(bitmap: Bitmap): File? {
        val cachePath = File(filesDir, Constants.CACHE_PATH)
        if (!cachePath.exists()) {
            cachePath.mkdirs()
        }
        val file = File(cachePath, Constants.CACHE_IMG_NAME)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }


}