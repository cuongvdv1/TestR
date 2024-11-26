package com.vm.backgroundremove.objectremove.ui.common.intro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.lib.admob.nativeAds.advance.manager.NativeAdvanceManager2
import com.util.appupdate.AppUpdateManager
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.app.RemoteConfigAppAllModel
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens.RemoteConfigScreenIntroModel
import com.vm.backgroundremove.objectremove.a8_app_utils.SharePrefUtils
import com.vm.backgroundremove.objectremove.a8_app_utils.SystemUtil
import com.vm.backgroundremove.objectremove.databinding.ActivityIntroBinding
import com.vm.backgroundremove.objectremove.ui.common.update.AppUpdateActivity
import com.vm.backgroundremove.objectremove.ui.main.permission.PermissionActivity


class IntroActivity : BaseActivity<ActivityIntroBinding, BaseViewModel>() {

    //new param
    private var nativeAdvanceManager2: NativeAdvanceManager2? = null
    //native full
    private var nativeFullScreenManager2: NativeAdvanceManager2? = null

    private var remoteConfigIntroModel: RemoteConfigScreenIntroModel? = null
    private var introAdapter: IntroAdapter? = null

    //remoteConfigAppAllModel
    private var remoteConfigAppAllModel: RemoteConfigAppAllModel? = null

    override fun createBinding() = ActivityIntroBinding.inflate(layoutInflater)

    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        super.initView()

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        //list slide
        val list = mutableListOf<IntroModel>()

        //list ad show status
        val listAdShowStatus = mutableListOf<Boolean>()

        if (true) {
            list.add(IntroModel(R.drawable.img_intro_1))
            listAdShowStatus.add(remoteConfigIntroModel?.is_slide1_ad_show ?: true)
        }

        if (true) {
            list.add(IntroModel(R.drawable.img_intro_2))
            listAdShowStatus.add(remoteConfigIntroModel?.is_slide2_ad_show ?: true)
        }

        if (true) {
            list.add(IntroModel(R.drawable.img_intro_3))
            listAdShowStatus.add(remoteConfigIntroModel?.is_slide3_ad_show ?: true)
        }

        if (true) {
            list.add(IntroModel(R.drawable.img_intro_4))
            listAdShowStatus.add(remoteConfigIntroModel?.is_slide4_ad_show ?: true)
        }

        introAdapter = IntroAdapter(this@IntroActivity, list)
        introAdapter?.list?.addAll(list)
        binding.vpIntro.adapter = introAdapter

        binding.indicator.attachTo(binding.vpIntro)
        binding.vpIntro.getChildAt(0).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER

        binding.vpIntro.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                //ad processing
                processAdInSlide(listAdShowStatus, position)


                //other data
                when (position) {
                    0 -> {
                        binding.tvTitle.text = getString(R.string.title_1)
                        binding.tvContent.text = getString(R.string.content_1)

                    }

                    1 -> {
                        binding.tvTitle.text = getString(R.string.title_2)
                        binding.tvContent.text = getString(R.string.content_2)
                    }

                    2 -> {
                        binding.tvTitle.text = getString(R.string.title_3)
                        binding.tvContent.text = getString(R.string.content_3)
                    }

                    3 -> {
                        binding.tvTitle.text = getString(R.string.title_4)
                        binding.tvContent.text = getString(R.string.content_4)
                    }
                }
                super.onPageSelected(position)
            }
        })

        binding.tvNext.setOnClickListener {
            val currentPosition = binding.vpIntro.currentItem
            binding.vpIntro.setCurrentItem(currentPosition + 1, true)
            //last slide
            if (currentPosition + 1 == list.size) {
                //show inter and next screen
//                AdCommon.showInterAd(this,
//                    RemoteConfigKey.ad_inter_intro,
//                    object : InterCallback {
//                        override fun onNextAction() {
//                            nextAction()
//                        }
//                    }
//                )
                nextAction()
            }
        }
    }
    override fun hideNavigationBar() {
        if (remoteConfigIntroModel?.is_hide_navi_menu == false)
            return super.hideNavigationBar()
        else
            return
    }

    private fun processAdInSlide(listAdShowStatus: MutableList<Boolean>, position: Int) {
        //show ad frame if show_ad = true
        if (listAdShowStatus[position]) {
            binding.frAdBottom.visibility = View.VISIBLE
            //reload ad if not first slide because ad loaded
            //if first slide no reload
            if (position != 0)
                nativeAdvanceManager2?.reloadNow()
        } else
            binding.frAdBottom.visibility = View.GONE
    }

    //adjust distance button vs ad by firebase config
    override fun adjustLayout() {
        val bottomMargin: Int = remoteConfigIntroModel?.distance_button_next_vs_ad ?: 0

        val paramsTextView = binding.tvNext.layoutParams as MarginLayoutParams
        val paramsDotsIndicator = binding.indicator.layoutParams as MarginLayoutParams

        //textview button
        paramsTextView.setMargins(
            paramsTextView.leftMargin,
            paramsTextView.topMargin,
            paramsTextView.rightMargin,
            bottomMargin
        )

        //dot indicator
        paramsDotsIndicator.setMargins(
            paramsDotsIndicator.leftMargin,
            paramsDotsIndicator.topMargin,
            paramsDotsIndicator.rightMargin,
            bottomMargin
        )
    }


    private fun nextAction() {

        Log.d("aaabbb", AppUpdateManager.checkUpdate.toString())
        Log.d("aaabbb", remoteConfigAppAllModel?.app_update_diaglog_or_activity.toString())

        val nextScreen = SystemUtil.getActivityClass(
            remoteConfigIntroModel?.next_screen_default,
            PermissionActivity::class.java
        )

        val launchCount = SharePrefUtils.getAppLaunchCountPermisstion(this)
        // Kiểm tra lần đầu mở ứng dụng
        if (launchCount == 0) {
//            // Chuyển đến màn Permission lần đầu tiên
            startActivity(Intent(this@IntroActivity, PermissionActivity::class.java))
            SharePrefUtils.increaseAppLaunchCountPermisstion(this) // Cập nhật lại launchCount
            finish()
        } else {
            // Kiểm tra cập nhật trong các lần tiếp theo
            if ((AppUpdateManager.checkUpdate == 1 || AppUpdateManager.checkUpdate == 2) &&
                remoteConfigAppAllModel?.app_update_diaglog_or_activity.equals("activity")
            ) {
//                // Chuyển đến màn hình cập nhật
                startActivity(Intent(this@IntroActivity, AppUpdateActivity::class.java))
                finishAffinity()
            } else {
//                // Chuyển đến màn hình kế tiếp
                startActivity(Intent(this@IntroActivity, nextScreen))
                finishAffinity()
            }
        }

        val intent = Intent(this@IntroActivity, nextScreen)
        intent.putExtra("from_intro_to_customization","from_intro_to_customization")
        startActivity(intent)
        finishAffinity()
    }

}