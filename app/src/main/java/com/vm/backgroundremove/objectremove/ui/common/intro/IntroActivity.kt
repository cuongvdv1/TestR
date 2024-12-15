package com.vm.backgroundremove.objectremove.ui.common.intro

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.lib.admob.interstitialAds.base.InterCallback
import com.lib.admob.nativeAds.advance.manager.NativeAdvanceManager2
import com.util.RemoteConfig
import com.util.appupdate.AppUpdateManager
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.RemoteConfigKey
import com.vm.backgroundremove.objectremove.a1_common_utils.ad.AdCommon
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.ads.RemoteConfigAdNativeModel
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.app.RemoteConfigAppAllModel
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens.RemoteConfigScreenIntroModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.SharePrefUtils
import com.vm.backgroundremove.objectremove.a8_app_utils.SystemUtil
import com.vm.backgroundremove.objectremove.databinding.ActivityIntroBinding
import com.vm.backgroundremove.objectremove.ui.common.update.AppUpdateActivity
import com.vm.backgroundremove.objectremove.ui.main.permission.PermissionActivity


class IntroActivity : BaseActivity<ActivityIntroBinding, BaseViewModel>() {

    private val tag = "IntroActivity"

    //new param
    private var nativeAdvanceManager2: NativeAdvanceManager2? = null

    //native full
    private var nativeFullScreenManager2: NativeAdvanceManager2? = null
    private var remoteConfigNativeFull: RemoteConfigAdNativeModel? = null
    private var remoteConfigScreenIntroModel: RemoteConfigScreenIntroModel? = null
    private var introAdapter: IntroAdapter? = null

    //remoteConfigAppAllModel
    private var remoteConfigAppAllModel: RemoteConfigAppAllModel? = null

    override fun createBinding() = ActivityIntroBinding.inflate(layoutInflater)

    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        super.initView()

        //lấy remote config AppAll
        remoteConfigAppAllModel = RemoteConfig.getConfigObject(
            this,
            RemoteConfigKey.app_config,
            RemoteConfigAppAllModel::class.java
        )

        remoteConfigScreenIntroModel = RemoteConfig.getConfigObject(
            this,
            RemoteConfigKey.screen_intro,
            RemoteConfigScreenIntroModel::class.java
        )

        //list slide
        val listIntroSlide = mutableListOf<IntroModel>()

        //list ad show status
        val listAdShowStatus = mutableListOf<Boolean>()

        //ad slide to manage list by show status
        if (remoteConfigScreenIntroModel?.is_slide1_show == true) {
            listIntroSlide.add(IntroModel(R.drawable.img_intro_1))
            listAdShowStatus.add(remoteConfigScreenIntroModel?.is_slide1_ad_show ?: true)
        }

        if (remoteConfigScreenIntroModel?.is_slide2_show == true) {
            listIntroSlide.add(IntroModel(R.drawable.img_intro_2))
            listAdShowStatus.add(remoteConfigScreenIntroModel?.is_slide2_ad_show ?: true)
        }

        if (remoteConfigScreenIntroModel?.is_slide3_show == true) {
            listIntroSlide.add(IntroModel(R.drawable.img_intro_3))
            listAdShowStatus.add(remoteConfigScreenIntroModel?.is_slide3_ad_show ?: true)
        }

        if (remoteConfigScreenIntroModel?.is_slide4_show == true) {
            listIntroSlide.add(IntroModel(R.drawable.img_intro_4))
            listAdShowStatus.add(remoteConfigScreenIntroModel?.is_slide4_ad_show ?: true)
        }

        //ad native full to list
        val adNativeFullPos1 = (remoteConfigScreenIntroModel?.ad_native_full_pos1 ?: 0) - 1
        if ((adNativeFullPos1 >= 0) and (adNativeFullPos1 <= listIntroSlide.size)) {
            listIntroSlide.add(adNativeFullPos1, IntroModel(0))
            listAdShowStatus.add(adNativeFullPos1, false)
        }

        val adNativeFullPos2 = (remoteConfigScreenIntroModel?.ad_native_full_pos2 ?: 0) - 1
        if ((adNativeFullPos2 >= 0) and (adNativeFullPos2 <= listIntroSlide.size)) {
            listIntroSlide.add(adNativeFullPos2, IntroModel(0))
            listAdShowStatus.add(adNativeFullPos2, false)
        }

        //continue processing
        introAdapter = IntroAdapter(this@IntroActivity, listIntroSlide)
        introAdapter?.list?.addAll(listIntroSlide)
        binding.vpIntro.adapter = introAdapter

        binding.indicator.attachTo(binding.vpIntro)
        binding.vpIntro.getChildAt(0).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER

        binding.vpIntro.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                super.onPageSelected(position)

                //ad processing
                processAdInSlide(listIntroSlide, listAdShowStatus, position)

                //get slide image position
                var imageNumber = 0
                if (remoteConfigNativeFull?.is_show == true) {
                    //ad processing
                    processAdInSlide(listIntroSlide, listAdShowStatus, position)

                    val pos1 = (remoteConfigScreenIntroModel?.ad_native_full_pos1 ?: 0) - 1
                    val pos2 = (remoteConfigScreenIntroModel?.ad_native_full_pos2 ?: 0) - 1


                    //if slide is showing ad native full
                    if ((position == pos1) or (position == pos2))
                        return

                    //convert from slide index 0-5 to slide image only index 0-3

                    if (position < pos1)
                        imageNumber = position
                    else if ((position > pos1) and (position < pos2))
                        imageNumber = position - 1
                    else if (position > pos2)
                        imageNumber = position - 2
                } else {
                    imageNumber = position
                }


                //other data
                when (imageNumber) {
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

            }
        })

        binding.tvNext.setOnClickListener {
            val currentPosition = binding.vpIntro.currentItem
            binding.vpIntro.setCurrentItem(currentPosition + 1, true)
            //last slide processing
            lastSlideProcess(currentPosition, listIntroSlide)

        }

        binding.ivCloseNativeFull.tap {
            processShowIntroImage()
            binding.vpIntro.currentItem += 1
            lastSlideProcess(binding.vpIntro.currentItem, listIntroSlide)
        }

    }

    override fun initAd() {
        //native
        nativeAdvanceManager2 = AdCommon.loadAndShowAdNativeAdvance(
            lifecycle,
            RemoteConfigKey.ad_native_intro,
            binding.frAdBottom,
            null
        )

        //inter load
        AdCommon.loadInterAd(
            this,
            RemoteConfigKey.ad_inter_intro,
            null
        )

        //ad native fullscreen
        nativeFullScreenManager2 = AdCommon.loadAndShowAdNativeAdvance(
            lifecycle,
            RemoteConfigKey.ad_native_fullscreen_intro,
            binding.frAdFull,
            null
        )
    }


    override fun hideNavigationBar() {
        if (remoteConfigScreenIntroModel?.is_hide_navi_menu == true)
            return super.hideNavigationBar()
        else
            return
    }

    //hidden native bottom and image, show native full
    private fun processShowAdNativeFull() {
        binding.frAdFull.visibility = View.VISIBLE
        binding.ivCloseNativeFull.visibility = View.VISIBLE
        binding.frAdBottom.visibility = View.GONE
        binding.layoutIndicator.visibility = View.GONE
        binding.nestedScrollView.visibility = View.GONE
    }

    //hidden native full, show image and native bottom
    private fun processShowIntroImage() {
        binding.frAdFull.visibility = View.GONE
        binding.ivCloseNativeFull.visibility = View.GONE
        binding.frAdBottom.visibility = View.VISIBLE
        binding.layoutIndicator.visibility = View.VISIBLE
        binding.nestedScrollView.visibility = View.VISIBLE
    }

    private fun processAdInSlide(
        listIntroSlide: MutableList<IntroModel>,
        listAdShowStatus: MutableList<Boolean>,
        position: Int
    ) {

        val isShowNativeFull = (listIntroSlide[position].image == 0)

        //show native full
        if (isShowNativeFull) {
            processShowAdNativeFull()

            //reload if not slide 0
            if (position != 0)
                nativeFullScreenManager2?.reloadNow()
        }
        //show native bottom if show_ad = true
        else if (listAdShowStatus[position]) {
            processShowIntroImage()
            binding.frAdBottom.visibility = View.VISIBLE

            //reload ad if not first slide because ad loaded and reload ok in remote config
            if ((position != 0) && (remoteConfigScreenIntroModel?.is_reload_ad_when_change_slide == true))
                nativeAdvanceManager2?.reloadNow()

            //do not show native bottom
        } else {
            processShowIntroImage()
            binding.frAdBottom.visibility = View.GONE
        }


//        //show ad frame if show_ad = true
//        if (listAdShowStatus[position]) {
//            binding.frAdBottom.visibility = View.VISIBLE
//            //reload ad if not first slide because ad loaded
//            //if first slide no reload
//            if (position != 0)
//                nativeAdvanceManager2?.reloadNow()
//        } else
//            binding.frAdBottom.visibility = View.GONE
    }

    //adjust distance button vs ad by firebase config
    override fun adjustLayout() {
        val bottomMargin: Int = remoteConfigScreenIntroModel?.distance_button_next_vs_ad ?: 0

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

        //font size
        var textSize: Float = remoteConfigScreenIntroModel?.font_size ?: 20F
        binding.tvNext.textSize = textSize
    }


    private fun lastSlideProcess(currentPosition: Int, listIntroSlide: MutableList<IntroModel>) {
        //last slide
        if (currentPosition + 1 == listIntroSlide.size) {
            //show inter and next screen
            AdCommon.showInterAd(this,
                RemoteConfigKey.ad_inter_intro,
                object : InterCallback {
                    override fun onNextAction() {
                        nextAction()
                    }
                }
            )
        }

    }


    private fun nextAction() {

        Log.d("aaabbb", AppUpdateManager.checkUpdate.toString())
        Log.d("aaabbb", remoteConfigAppAllModel?.app_update_diaglog_or_activity.toString())

        //check version code va version newest firebase
        // = 1 la need update, 2 la should update
        //app_update_dialog_or_activity= activity la show screen activity

        //check next screen
        val nextScreen = SystemUtil.getActivityClass(
            remoteConfigScreenIntroModel?.next_screen_default,
            PermissionActivity::class.java
        )

        val launchCount = SharePrefUtils.getAppLaunchCountPermisstion(this)
        // Kiểm tra lần đầu mở ứng dụng
        if (launchCount == 0) {
            // Chuyển đến màn Permission lần đầu tiên
            startActivity(Intent(this@IntroActivity, PermissionActivity::class.java))
            finish()
        } else {
            // Kiểm tra cập nhật trong các lần tiếp theo
            if ((AppUpdateManager.checkUpdate == 1 || AppUpdateManager.checkUpdate == 2) &&
                remoteConfigAppAllModel?.app_update_diaglog_or_activity.equals("activity")
            ) {
                // Chuyển đến màn hình cập nhật
                startActivity(Intent(this@IntroActivity, AppUpdateActivity::class.java))
                finishAffinity()
            } else {
                // Chuyển đến màn hình kế tiếp
                startActivity(Intent(this@IntroActivity, nextScreen))
                finishAffinity()
            }
        }

    }


}