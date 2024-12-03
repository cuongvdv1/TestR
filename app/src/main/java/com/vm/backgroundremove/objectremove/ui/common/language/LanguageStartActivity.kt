package com.vm.backgroundremove.objectremove.ui.common.language

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.nativead.NativeAdView
import com.lib.admob.bannerAds.adaptive.AdaptiveBannerManager
import com.lib.admob.bannerAds.collapsible.BannerGravity
import com.lib.admob.bannerAds.collapsible.CollapseBannerManager
import com.lib.admob.nativeAds.advance.base.NativeCallback
import com.lib.admob.nativeAds.advance.manager.NativeAdvanceManager2
import com.util.RemoteConfig
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.RemoteConfigKey
import com.vm.backgroundremove.objectremove.a1_common_utils.ad.AdCommon
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens.RemoteConfigScreenLanguageStartModel
import com.vm.backgroundremove.objectremove.a8_app_utils.SystemUtil
import com.vm.backgroundremove.objectremove.databinding.ActivityLanguageStartBinding
import com.vm.backgroundremove.objectremove.ui.common.intro.IntroActivity
import com.vm.backgroundremove.objectremove.ui.main.user_perpose.UserPerposeActivity


class LanguageStartActivity : BaseActivity<ActivityLanguageStartBinding, BaseViewModel>() {
    private val tag:String = "LanguageStartActivity"
    private  var languageAdapter: LanguageAdapter? = null
    private var lang : String = ""

    //ad variable
    private var remoteConfigScreenLanguageStartModel : RemoteConfigScreenLanguageStartModel? =null

    //banner manager
    private var nativeAdvanceManager: NativeAdvanceManager2? = null
    private var adaptiveBannerManager: AdaptiveBannerManager? = null
    private var collapseBannerManager: CollapseBannerManager? = null



    override fun createBinding() = ActivityLanguageStartBinding.inflate(layoutInflater)

    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        super.initView()

        //ad
        remoteConfigScreenLanguageStartModel = RemoteConfig.getConfigObject(
            this,
            RemoteConfigKey.screen_language_start,
            RemoteConfigScreenLanguageStartModel::class.java
        )

        binding.ivCheck.setOnClickListener {
            //ANH edit 20241126
            //move to function

//            SystemUtil.saveLocale(this, lang)
//
//            //check next screen
//            val nextScreen = SystemUtil.getActivityClass(
//                remoteConfigScreenLanguageStartModel?.next_screen_default,
//                IntroActivity::class.java
//            )
//
//            val intent = Intent(this@LanguageStartActivity, nextScreen)
//            intent.putExtra("from_language_to_customization","from_language_to_customization")
//            startActivity(intent)
//            finishAffinity()
            saveLanguageAndNextScreen()
        }

        binding.btnFanSelectedLanguage.setOnClickListener {
            //ANH add 20241126 for change UI for FAN
            saveLanguageAndNextScreen()
        }


        val listLang = mutableListOf(
            LanguageModel("Hindi", "hi", false, R.drawable.img_logo_hindi),
            LanguageModel("French", "fr", false, R.drawable.img_logo_french),
            LanguageModel("Spanish", "es", false, R.drawable.img_logo_spanish),
            LanguageModel("Portuguese", "pt", false, R.drawable.img_logo_portuguese),
            LanguageModel("Indonesian", "in", false, R.drawable.img_logo_indo),
            LanguageModel("German", "de", false, R.drawable.img_logo_german),
            LanguageModel("English", "en", true, R.drawable.img_logo_english),
        )

        //swap position
        val n = remoteConfigScreenLanguageStartModel?.lang_c_pos?:0
        val m = remoteConfigScreenLanguageStartModel?.lang_p_pos?:0
        if (n in listLang.indices && m in listLang.indices) {
            val temp = listLang[n]
            listLang[n] = listLang[m]
            listLang[m] = temp
        }

        //continue processing
        val linearLayoutManager = LinearLayoutManager(this)
        languageAdapter =
            LanguageAdapter(this, listLang, object : LanguageAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {

                    //reload native ad
                    //anh edit 20241117 add condition reload
                    if ((remoteConfigScreenLanguageStartModel?.is_reload_after_select_language == true) &&
                        (nativeAdvanceManager != null)){
                        nativeAdvanceManager?.reloadNow()

                    }


                    //other process
                    binding.ivCheck.visibility = View.VISIBLE
                    lang = listLang[position].code
                }

            })
        binding.rvLanguage.layoutManager = linearLayoutManager
        binding.rvLanguage.adapter = languageAdapter
    }

    override fun initAd() {
//        nativeAdvanceManagerBottom = AdCommon.loadAndShowAdNativeAdvance(lifecycle,
//            RemoteConfigKey.ad_native_language_bottom,
//            binding.frAdsLangBottom,
//            null)
//

        val adNativeCallback:NativeCallback = object :NativeCallback{
            override fun onChangeCtrWhenFan(view: NativeAdView) {
                super.onChangeCtrWhenFan(view)
                Log.v(tag,"onChangeCtrWhenFan")
                binding.ivCheck.visibility=View.GONE
                binding.btnFanSelectedLanguage.visibility=View.VISIBLE
            }
        }

        //ad type
        val adType= remoteConfigScreenLanguageStartModel?.ad_type

        //ad adPlacement
        var frAd: FrameLayout?=null
        var bannerGravity :BannerGravity?=null
        val adPlacement= remoteConfigScreenLanguageStartModel?.ad_placement

        if (adPlacement.equals("top")) {
            frAd = binding.frAdsLangTop
            bannerGravity=BannerGravity.TOP
        } else if (adPlacement.equals("bottom")) {
            frAd = binding.frAdsLangBottom
            bannerGravity=BannerGravity.BOTTOM
        }


        val adManager =
            AdCommon.loadAndShowNativeOrBanner(
                lifecycle,
                adType=adType,
                frAd = frAd,
                rmcfKeyNativeAdvance = RemoteConfigKey.ad_native_language_bottom,
                rmcfKeyBannerCollap = RemoteConfigKey.ad_banner_collapsible_all,
                rmcfKeyBannerAdaptive = RemoteConfigKey.ad_banner_adaptive_all,
                bannerGravity = bannerGravity,
                nativeCallback = adNativeCallback,
                bannerCallback = null
            )

        //return
        if (adManager is NativeAdvanceManager2)
            nativeAdvanceManager = adManager
        else if (adManager is CollapseBannerManager)
            collapseBannerManager = adManager
        else if (adManager is AdaptiveBannerManager)
            adaptiveBannerManager = adManager

    }



    override fun hideNavigationBar() {
        if (remoteConfigScreenLanguageStartModel?.is_hide_navi_menu == true)
            return super.hideNavigationBar()
        else
            return
    }
    private fun saveLanguageAndNextScreen(){
        SystemUtil.saveLocale(this, lang)

        //check next screen
        val nextScreen = SystemUtil.getActivityClass(
            remoteConfigScreenLanguageStartModel?.next_screen_default,
            UserPerposeActivity::class.java
        )

        val intent = Intent(this@LanguageStartActivity, nextScreen)
        intent.putExtra("from_language_to_customization","from_language_to_customization")
        startActivity(intent)
        finishAffinity()
    }
}