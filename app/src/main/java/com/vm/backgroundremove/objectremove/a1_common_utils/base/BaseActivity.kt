package com.vm.backgroundremove.objectremove.a1_common_utils.base

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.util.FirebaseLogEventUtils
import com.vm.backgroundremove.objectremove.a8_app_utils.SystemUtil
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.lib.admob.resumeAds.AppOpenResumeManager
import com.vm.backgroundremove.objectremove.R

abstract class BaseActivity<VB : ViewBinding, V: ViewModel> : AppCompatActivity() {
    protected lateinit var binding: VB
    protected lateinit var viewModel: V

    abstract fun createBinding(): VB
    abstract fun setViewModel(): V

    protected open fun initView() {}
    protected open fun bindView() {}

    open fun viewModel() {}
    open fun initData() {}

    //init ad
    protected open fun initAd() {}

    //adjust layout
    protected open fun adjustLayout() {}

    override fun hasWindowFocus(): Boolean {
        hideNavigationBar()
        return super.hasWindowFocus()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideNavigationBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SystemUtil.setLocale(this)
        binding = createBinding()
        setContentView(binding.root)
        viewModel = setViewModel()
        viewModel()
        initView()
        bindView()

        //AD implement
        initAd()

        //hide or show navigation bar
        hideNavigationBar()

        //adjust layout
        adjustLayout()

        //log event
        FirebaseLogEventUtils.logEventScreenView(this,"onCreat" )
    }


    //confirm xu ly source code outdated
    protected open fun hideNavigationBar() {

        val decorView = window.decorView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API level 30) and above
            decorView.windowInsetsController?.let { controller ->
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Below Android 11
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )

            // Listener để ẩn lại thanh điều hướng khi người dùng tương tác
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    Handler().postDelayed({
                        decorView.systemUiVisibility = (
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                )
                    }, 3000)
                }
            }
        }
    }

    fun checkNotiPermissionNew():Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED)

        }else{
            true
        }
    }
    // Nhung quyen cap cho version API <33
    var permissionsApiTo33 = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.CAMERA
    )
    // Nhung quyen cap cho version API >=5 && API API < 12
    var permissionsApiFrom5To12 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
    )

    // Ham cap quyen

    private fun arrayPermission():Array<String>{
        val permission : Array<String> = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permissionsApiTo33
        }else{
            permissionsApiFrom5To12

        }
        return permission
    }

    // Ham check quyen permissions
    fun checkPermissions() : Boolean{
        return checkCameraPermission() && checkStorePermission()
    }

    // Ham check quyen cap quyen cua CAMERA
    fun checkCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

// Ham check da duoc cap quyen hay chua tra ve true neu READ_MEDIA_IMAGES da duoc cap quyen (voi thiet bi Android API33)
// va READ_EXTERNAL_STORAGE + WRITE_EXTERNAL_STORAGE da duoc cap quyen (voi thiet bi Android voi API thap hon)
    fun checkStorePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED)
        } else {
            (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        }
    }

    private var alertDialog : AlertDialog ? = null

    fun dialogPermission(){
        alertDialog = AlertDialog.Builder(this, R.style.AlertDialogCustom).create()
        alertDialog?.setTitle(getString(R.string.Grant_Permission))
        alertDialog?.setMessage(getString(R.string.Please_grant_all_permissions))
        alertDialog?.setButton(-1,getString(R.string.Go_to_setting) as CharSequence){ _, _ ->
            alertDialog?.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", applicationContext.packageName, null)
            intent.data = uri
            startActivity(intent)
            AppOpenResumeManager.setEnableAdsResume(false)
        }
    }
    fun showDialogPermission() {
        alertDialog?.setOnShowListener {
            alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(resources.getColor(R.color.black))
        }
        alertDialog?.show()
    }
    private var permissionsResult: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.entries.all { it.value }) {

            } else {
                showDialogPermission()
            }
        }



    fun requestPermission() {
        permissionsResult.launch(arrayPermission())
    }

}