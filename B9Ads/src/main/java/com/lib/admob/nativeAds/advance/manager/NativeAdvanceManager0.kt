/**
 * load and show ads by common
 * Reload in case: resume, media play end, trigger by user
 * preload in other ads
 */

package com.lib.admob.nativeAds.advance.manager
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.lib.R
import com.lib.admob.nativeAds.advance.base.NativeCallback
import com.lib.admob.nativeAds.advance.base.NativeLayoutInlib
import com.lib.enums.LoadState
import com.lib.ump.AdsConsentManager
import com.util.CheckInternet
import com.util.FirebaseLogEventUtils
import com.util.SystemSharePreferenceUtils

open class NativeAdvanceManager0(

    // id quảng cáo
    //remote config
    private val ad_id: String,

    // lifecycle dùng để reload quảng cáo
    lifecycle: Lifecycle? = null

) {
    //log tag
    private val TAG = "NativeAdvanceAdManager0"

    //properties for log event
    protected var adName: String ="native"

    // layout của Native, co the lay trong lib hoạc trong app
    protected var nativeAdView: NativeAdView? = null

    // layout loading
    private var shimmerLayout: View? = null

    //native containter in app
    private var containerLayout: FrameLayout? = null

    // quảng cáo native hiện đang show
    protected var nativeAd: NativeAd? = null

    // quảng cáo native reload
    private var nativeAdPreload: NativeAd? = null

    // Có đang tải quảng cáo không
    private var loadAdState = LoadState.NOT_LOADING

    //dang co 2 chuc nang la phan biet ad goi on_resume lan1,2 va show ad luon khi vao app
    //pending neu load truoc resume thi co the can them 1 parameter status nua de luu status
    private var isShowAdImmediately = false

    private var callback: NativeCallback? = null

    //log event bundle
    private var logBundleRevenueDetail = Bundle()

    //ad loaded time
    //for management preload ad
    private var loadedNTimes = 0

    //preload quota
    //if not have max time, show rate will low
    private var preloadedMaxTimes = 2


    init {
        lifecycle?.addObserver(object : LifecycleEventObserver{
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {

                    Lifecycle.Event.ON_RESUME -> {
                        if(!isShowAdImmediately){
                            //confirm neu co ad resume thi 2 lan goi vao day
                            Log.v(TAG, "Lifecycle.Event.ON_RESUME reloadNow")
                            reloadNow()
                        }
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        nativeAd?.destroy()
                        nativeAdPreload?.destroy()
                        //handler.removeCallbacksAndMessages(null)
                        source.lifecycle.removeObserver(this)
                    }

                    else -> {}
                }
            }
        })
    }

    //set adName for log event
    fun setNativeInfo(adName: String, preloadMaxTime: Int =1){
        this.adName=adName
        this.preloadedMaxTimes = preloadMaxTime
    }

    protected open fun getAdLayout(nativeAd: NativeAd) = nativeAdView

    /**
     * call to load and show native by using layout in app
     */
    open fun loadAndShowNative(container: FrameLayout, idNativeLayout: Int, idShimmer: Int,
                               drawableBackground: GradientDrawable? = null,
                               callback: NativeCallback? = null) {
        this.callback = callback
        containerLayout = container
        val context = container.context
        val nativeView = LayoutInflater.from(context).inflate(idNativeLayout, null)

        if (nativeView is NativeAdView) {
            nativeAdView = nativeView
        }

        //adjust layout
        if (drawableBackground != null)
            nativeAdView?.findViewById<ConstraintLayout>(R.id.ad_unit_content)?.background = drawableBackground

        //shimmer
        shimmerLayout = LayoutInflater.from(context).inflate(idShimmer, null)
        shimmerLayout?.isVisible = true
        containerLayout?.addView(shimmerLayout)

        //show ad luc dau ngay sau khi load ad
        isShowAdImmediately = true

        //load and show
        loadAndShowNative(context)
    }

    /**
     * call to load and show native by using layout in lib
     */
    open fun loadAndShowNative(container: FrameLayout, defaultStyle: NativeLayoutInlib,
                               drawableBackground: GradientDrawable? = null,
                               callback: NativeCallback? = null) {
        loadAndShowNative(container, defaultStyle.layoutNativeId, defaultStyle.shimmerLayoutId, drawableBackground, callback)
    }

    // Reload ngay lập tức
    fun reloadNow() {

        //if always preload ad
        //showAndPreloadAd()

        //if quota in preload ad, bellow code
        Log.v(TAG, "Function reloadNow")
        Log.v(TAG, "check already loaded ad: ${nativeAdPreload != null}")

        //fix bug crash when using for native fullscreen ( call native when clicked button, not init screen)
        if (containerLayout == null)
            return

        if (nativeAdPreload != null)
            showAndPreloadAd()
        else {
            isShowAdImmediately=true
            loadAndShowNative(containerLayout!!.context)
        }
    }

    /**
     * Tải native về, neu OK goi hien thi
     */
    private fun loadAndShowNative(context: Context) {
        Log.v(TAG, "Function loadAndShowNative")

        if (loadAdState == LoadState.LOADING
            || CheckInternet.haveNetworkConnection(context).not()
            || AdsConsentManager.getInstance(context).canRequestAds.not()
        ) {
            Log.v(TAG, "Function loadAndShowNative-load condition NG, exit function")
            return
        }

        //condition load native OK
        Log.v(TAG, "Function loadAndShowNative-load condition OK, load ad...")

        loadAdState = LoadState.LOADING
        val builder = AdLoader.Builder(context, ad_id)
            .forNativeAd {
                Log.v(TAG, "onNativeAdLoaded")
                onLoadComplete(context, it)
            }
            .withAdListener(object : AdListener() {

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    //Log.v(TAG, "onAdLoaded")
                    callback?.onAdLoaded()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    loadAdState = LoadState.NOT_LOADING
                    //Log.v(TAG, "onAdFailedToLoad")
                    callback?.onAdFailedToLoad(p0)
                    FirebaseLogEventUtils.logEventAdFailedToLoad(context, adName, ad_id, p0.message)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    //Log.v(TAG, "onAdClicked")
                    callback?.onAdClicked()

                    FirebaseLogEventUtils.logEventAdClicked(context, adName, ad_id)

                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    //Log.v(TAG, "onAdClosed")
                    callback?.onAdClosed()
                }

                override fun onAdImpression() {
                    super.onAdImpression()

                    //Log.v(TAG, "onAdImpression")
                    callback?.onAdImpression()
                    FirebaseLogEventUtils.logEventAdImpression(context, adName, ad_id)

                }

                override fun onAdOpened() {
                    super.onAdOpened()

                    //Log.v(TAG, "onAdOpened")
                    callback?.onAdOpened()
                }

                override fun onAdSwipeGestureClicked() {
                    super.onAdSwipeGestureClicked()
                    //Log.v(TAG, "onAdSwipeGestureClicked")
                    callback?.onAdSwipeGestureClicked()
                }

            })

        //confirm xem lai cho nay de lam gi truoc khi load?
        val videoOption =
            VideoOptions.Builder().setStartMuted(false).setCustomControlsRequested(true).build()
        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOption).build()
        builder.withNativeAdOptions(adOptions)
        val adLoader = builder.build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    /**
     * callback khi load ad success
     * assign loaded ads to preload ads
     * if 1st isShowAdImmediately =true then show ad
     * if not do nothing (wait to call showAndPreloadAd)
     */
    protected open fun onLoadComplete(context: Context, loadedNativeAd: NativeAd) {
        Log.v(TAG, "Function onLoadComplete")

        //increase loadedNtimes
        loadedNTimes++
        Log.v(TAG, "increased loadNtime, loadedNTime = $loadedNTimes")

        //reset load state
        loadAdState = LoadState.NOT_LOADING

        loadedNativeAd.setOnPaidEventListener {
            callback?.onEarnRevenue(it)

            //increase user ltv
            SystemSharePreferenceUtils.updateUserLTV(context,
                (it.valueMicros / 1000000.0).toFloat()
            )

            //Log.v(TAG, "onEarnRevenue")
            //log event
            FirebaseLogEventUtils.logEventAdPaidRevenue(context, adName, ad_id, it)
            FirebaseLogEventUtils.logEventAdPaidRevenueDetail(context,"native_advance", logBundleRevenueDetail, adName, ad_id, it)
        }

        nativeAdPreload = loadedNativeAd

        if (isShowAdImmediately) {
            isShowAdImmediately = false
            showAndPreloadAd()
        }

        callback?.OnNativeAdLoaded(loadedNativeAd)

        //log firebase mediation ad source
        //moi ad_source 1 event
        val bundleMediation = Bundle()
        bundleMediation.putString("v1_ad_source_name", loadedNativeAd.responseInfo?.loadedAdapterResponseInfo?.adSourceName)
        bundleMediation.putString("v1_ad_source_id", loadedNativeAd.responseInfo?.loadedAdapterResponseInfo?.adSourceId)
        FirebaseLogEventUtils.logEventAdMediationWin(context, adName, ad_id, bundleMediation)
    }

    // Đẩy dữ liệu vào view
    private fun pushNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        Log.v(TAG, "pushNativeAdView")
        val context = adView!!.context

        adView.mediaView = adView.findViewById<View>(R.id.ad_media) as? MediaView

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        (adView.headlineView as? TextView)?.text = nativeAd.headline
        adView.mediaView?.mediaContent = nativeAd.mediaContent

        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as? TextView)?.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as? Button)?.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as? ImageView)?.setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView?.visibility = View.INVISIBLE
        } else {
            adView.priceView?.visibility = View.VISIBLE
            (adView.priceView as? TextView)?.text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView?.visibility = View.INVISIBLE
        } else {
            adView.storeView?.visibility = View.VISIBLE
            (adView.storeView as? TextView)?.text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as? RatingBar)?.rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView?.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as? TextView)?.text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)


        val vc = nativeAd.mediaContent?.videoController

        if (nativeAd.mediaContent != null && nativeAd.mediaContent!!.hasVideoContent()) {

            //has video
            var bundle = Bundle()
            bundle.putString("v1_ad_id", ad_id)
            bundle.putString("v1_ad_name", adName)
            FirebaseLogEventUtils.logEventCommonWithName(context, "ad_native_has_vd", bundle)

            vc?.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {

                override fun onVideoEnd() {
                    super.onVideoEnd()
                    //Log.v(TAG, "onVideoEnd")
                    onNativeMediaVideoEnd()
                    //log event
                    FirebaseLogEventUtils.logEventCommonWithName(context, "ad_native_vd_e", bundle)
                }

                override fun onVideoStart() {
                    super.onVideoStart()
                    //Log.v(TAG, "onVideoStart")

                    //log event
                    FirebaseLogEventUtils.logEventCommonWithName(context, "ad_native_vd_s", bundle)

                }

                override fun onVideoPlay() {
                    super.onVideoPlay()
                    //Log.v(TAG, "onVideoPlay")
                    //log event
                    FirebaseLogEventUtils.logEventCommonWithName(context, "ad_native_vd_p", bundle)
                }

                override fun onVideoPause() {
                    super.onVideoPause()
                    //Log.v(TAG, "onVideoPause")
                }

                override fun onVideoMute(p0: Boolean) {
                    super.onVideoMute(p0)
                    //Log.v(TAG, "onVideoMute")
                }

            }
            if (vc?.isCustomControlsEnabled == true) {
                vc.play()
            }
        } else {
            Log.v(TAG, "Function onCheckNativeNotMediaVideo")
            onCheckNativeNotMediaVideo()
        }
    }

    /**
     * callback khi native ad khong co video
     */
    protected open fun onCheckNativeNotMediaVideo(){
        return
    }

    /**
     * callback khi end video media
     */
    protected open fun onNativeMediaVideoEnd(){
        //showAndPreloadAd()
        reloadNow()
    }

    /**
     * show ad and preload other native
     */

    open fun showAndPreloadAd() {
        Log.v(TAG, "Function showAndPreloadAd")
        Log.v(TAG, "Function showAndPreloadAd condition  ${(nativeAdView != null && nativeAdPreload != null)}")

        //show ad and preload ad process
        if (nativeAdView != null && nativeAdPreload != null) {

            //show ad
            //destroy current native and assign preloaded native
            nativeAd?.destroy()
            nativeAd = nativeAdPreload
            nativeAdPreload = null

            //reset bundle log event
            resetBundle()
            setLogBundleDataMedia()

            //processing UI
            val layout = getAdLayout(nativeAd!!)
            layout?.let {
                pushNativeAdView(nativeAd!!, it)
                containerLayout?.removeAllViews()
                containerLayout?.addView(it)
                it.isVisible = true

                //callback
                callback?.onShowNative(it)
            }

            //if no quota management preload ad
            //loadAndShowNative(nativeAdView!!.context)

            //continue preload ad if have preload quota
            Log.v(TAG, "Function showAndPreloadAd check preload: ${loadedNTimes<preloadedMaxTimes}")
            if (loadedNTimes < preloadedMaxTimes) {
                loadAndShowNative(nativeAdView!!.context)
            }

        }
    }

    //reset bundle
    private fun resetBundle() {
        //Log.v(TAG, "resetBundle")

        //source
        logBundleRevenueDetail.putString("v1_source_name", "")
        logBundleRevenueDetail.putString("v1_source_id", "")

        //ad info
        logBundleRevenueDetail.putString("v1_ad_advertiser", "")
        logBundleRevenueDetail.putString("v1_ad_store", "")
        logBundleRevenueDetail.putString("v1_ad_head", "")
        //logBundle.putString("v1_ad_body", "")

        //media
        logBundleRevenueDetail.putString("v1_media_info", "")
    }

    //set bundle media data
    private fun setLogBundleDataMedia() {
        //Log.v(TAG, "setLogBundleDataMedia")

        nativeAd?.let {

            //ad source info
            val adapterResponseInfo = it.responseInfo?.loadedAdapterResponseInfo
            logBundleRevenueDetail.putString("v1_source_name", adapterResponseInfo?.adSourceName)
            logBundleRevenueDetail.putString("v1_source_id", adapterResponseInfo?.adSourceId)

            //ad info
            logBundleRevenueDetail.putString("v1_ad_advertiser", it.advertiser)
            logBundleRevenueDetail.putString("v1_ad_store", it.store)
            logBundleRevenueDetail.putString("v1_ad_head", it.headline)
            //logBundle.putString("v1_ad_body", it.body)

            //media content info
            val media = it.mediaContent
            var hasMedia =  (media != null).toString()
            var hasVideo = (media?.hasVideoContent()?:false).toString()
            var duration = (media?.duration?:0F).toString()
            var aspectRatio = (media?.aspectRatio?:0F).toString()

            var mediaInfo = "media:${hasMedia}_video:${hasVideo}_duration:${duration}_ratio:${aspectRatio}"
            logBundleRevenueDetail.putString("v1_media_info", mediaInfo)
        }
    }
}