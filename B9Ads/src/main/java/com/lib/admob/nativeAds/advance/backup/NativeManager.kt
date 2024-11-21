//
//package com.lib.admob.nativeAds.advance.backup
//import android.content.Context
//import android.os.Handler
//import android.os.Looper
//import android.view.LayoutInflater
//import android.view.View
//import android.widget.Button
//import android.widget.FrameLayout
//import android.widget.ImageView
//import android.widget.RatingBar
//import android.widget.TextView
//import androidx.core.view.isVisible
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.lifecycle.LifecycleOwner
//import com.google.android.gms.ads.AdListener
//import com.google.android.gms.ads.AdLoader
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks
//import com.google.android.gms.ads.VideoOptions
//import com.google.android.gms.ads.nativead.MediaView
//import com.google.android.gms.ads.nativead.NativeAd
//import com.google.android.gms.ads.nativead.NativeAdOptions
//import com.google.android.gms.ads.nativead.NativeAdView
//import com.lib.R
//import com.lib.admob.nativeAds.advance.base.NativeCallback
//import com.lib.admob.nativeAds.advance.base.NativeLibDefaultLayout
//import com.lib.ump.AdsConsentManager
//import com.lib.enums.LoadState
//import com.util.CheckInternet
//
//open class NativeManager(
//    // id quảng cáo
//    private val id: String,
//
//    // lifecycle dùng để reload quảng cáo
//    lifecycle: Lifecycle? = null
//) {
//
//    // layout của Native, co the lay trong lib hoạc trong app
//    protected var layoutNative: NativeAdView? = null
//
//    // layout loading
//    private var shimmerLayout: View? = null
//
//    //native containter in app
//    private var containerLayout: FrameLayout? = null
//
//    // quảng cáo native hiện đang show
//    private var nativeAd: NativeAd? = null
//
//    // quảng cáo native reload
//    private var nextNativeAd: NativeAd? = null
//
//    // thời gian reload quảng cáo
//    private var reloadAfter: Long = -1
//
//    // Có reload hay không
//    private var isReload = true
//
//    // Có đang tải quảng cáo không
//    private var loadState = LoadState.NOT_LOADING
//
//    /**
//         sau khi load xong co show luon hay khong?
//        luc dau set needShowImmediately= true de goi showAds()
//        sau khi show thì set =false de khong show luon
//    */
//    private var needShowImmediately = false
//
//    // Handler phục vụ reload quảng cáo by time
//    private val handler = Handler(Looper.getMainLooper())
//
//
//    private var needReloadAtResume = true
//
//    private var callback: NativeCallback? = null
//
//
//    init {
//        lifecycle?.addObserver(object : LifecycleEventObserver{
//            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//                when (event) {
//                    Lifecycle.Event.ON_RESUME -> {
//                        if (needReloadAtResume) {
//                            isReload = true
//                            showAds()
//                        }
//                        needReloadAtResume = true
//                    }
//
//                    Lifecycle.Event.ON_STOP -> {
//                        isReload = false
//                    }
//
//                    Lifecycle.Event.ON_DESTROY -> {
//                        nativeAd?.destroy()
//                        nextNativeAd?.destroy()
//                        handler.removeCallbacksAndMessages(null)
//                        source.lifecycle.removeObserver(this)
//                    }
//
//                    else -> {}
//                }
//            }
//        })
//    }
//
//    /**
//     * call to load and show native by using layout in app
//     * process: - prepare UI
//     *          - load native
//     */
//    open fun showNative(container: FrameLayout, idNativeLayout: Int, idShimmer: Int, callback: NativeCallback? = null) {
//        this.callback = callback
//        containerLayout = container
//        val context = container.context
//        val nativeView = LayoutInflater.from(context).inflate(idNativeLayout, null)
//        if (nativeView is NativeAdView) {
//            layoutNative = nativeView
//        }
//        shimmerLayout = LayoutInflater.from(context).inflate(idShimmer, null)
//        shimmerLayout?.isVisible = true
//        containerLayout?.addView(shimmerLayout)
//        needShowImmediately = true
//        needReloadAtResume = false
//
//        //load and show
//        loadNative(context)
//    }
//
//    /**
//     * call to load and show native by using layout in lib
//     * process: same above
//     */
//
//    open fun showNative(container: FrameLayout, defaultStyle: NativeLibDefaultLayout, callback: NativeCallback? = null) {
//        showNative(container, defaultStyle.layoutNativeId, defaultStyle.shimmerLayoutId, callback)
//    }
//
//    // Phải gọi khi muốn reload sau một khoảng thời gian
//    fun setReloadAfter(milliseconds: Long) {
//        reloadAfter = milliseconds
//    }
//
//    // Reload ngay lập tức
//    fun reloadNow() {
//        showAds()
//    }
//
//    // Đặt thời gian để reload quảng cáo
//    private fun setupReloadAds() {
//        handler.removeCallbacksAndMessages(null)
//        if (reloadAfter > 0) {
//            handler.postDelayed({
//                showAds()
//            }, reloadAfter)
//        }
//    }
//
//    /**
//     * Tải native về, neu successes goi hien thi
//     */
//    private fun loadNative(context: Context) {
//        if (loadState == LoadState.LOADING || CheckInternet.haveNetworkConnection(context).not()
//            || AdsConsentManager.getInstance(context).canRequestAds.not()
//        ) {
//            return
//        }
//        loadState = LoadState.LOADING
//        val builder = AdLoader.Builder(context, id)
//            .forNativeAd { onLoadComplete(it) }
//            .withAdListener(object : AdListener() {
//
//                override fun onAdFailedToLoad(p0: LoadAdError) {
//                    super.onAdFailedToLoad(p0)
//                    callback?.onAdFailedToLoad(p0)
//                    loadState = LoadState.NOT_LOADING
//                }
//
//                override fun onAdClicked() {
//                    super.onAdClicked()
//                    callback?.onAdClicked()
//                }
//            })
//
//        val videoOption =
//            VideoOptions.Builder().setStartMuted(false).setCustomControlsRequested(true).build()
//        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOption).build()
//        builder.withNativeAdOptions(adOptions)
//        val adLoader = builder.build()
//        adLoader.loadAd(AdRequest.Builder().build())
//    }
//
//    private fun onLoadComplete(newNativeAd: NativeAd) {
//        loadState = LoadState.NOT_LOADING
//        nextNativeAd = newNativeAd
//        callback?.OnNativeAdLoaded(newNativeAd)
//        if (needShowImmediately) {
//            needShowImmediately = false
//            showAds()
//        }
//        newNativeAd.setOnPaidEventListener {
//            callback?.onEarnRevenue(it)
//        }
//    }
//
//    // Đẩy dữ liệu vào view
//    private fun pushNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
//        adView.mediaView = adView.findViewById<View>(R.id.ad_media) as? MediaView
//
//        adView.headlineView = adView.findViewById(R.id.ad_headline)
//        adView.bodyView = adView.findViewById(R.id.ad_body)
//        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
//        adView.iconView = adView.findViewById(R.id.ad_app_icon)
//        adView.priceView = adView.findViewById(R.id.ad_price)
//        adView.starRatingView = adView.findViewById(R.id.ad_stars)
//        adView.storeView = adView.findViewById(R.id.ad_store)
//        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
//
//        (adView.headlineView as? TextView)?.text = nativeAd.headline
//        adView.mediaView?.mediaContent = nativeAd.mediaContent
//
//        if (nativeAd.body == null) {
//            adView.bodyView?.visibility = View.INVISIBLE
//        } else {
//            adView.bodyView?.visibility = View.VISIBLE
//            (adView.bodyView as? TextView)?.text = nativeAd.body
//        }
//
//        if (nativeAd.callToAction == null) {
//            adView.callToActionView?.visibility = View.INVISIBLE
//        } else {
//            adView.callToActionView?.visibility = View.VISIBLE
//            (adView.callToActionView as? Button)?.text = nativeAd.callToAction
//        }
//
//        if (nativeAd.icon == null) {
//            adView.iconView?.visibility = View.GONE
//        } else {
//            (adView.iconView as? ImageView)?.setImageDrawable(
//                nativeAd.icon?.drawable
//            )
//            adView.iconView?.visibility = View.VISIBLE
//        }
//
//        if (nativeAd.price == null) {
//            adView.priceView?.visibility = View.INVISIBLE
//        } else {
//            adView.priceView?.visibility = View.VISIBLE
//            (adView.priceView as? TextView)?.text = nativeAd.price
//        }
//
//        if (nativeAd.store == null) {
//            adView.storeView?.visibility = View.INVISIBLE
//        } else {
//            adView.storeView?.visibility = View.VISIBLE
//            (adView.storeView as? TextView)?.text = nativeAd.store
//        }
//
//        if (nativeAd.starRating == null) {
//            adView.starRatingView?.visibility = View.INVISIBLE
//        } else {
//            (adView.starRatingView as? RatingBar)?.rating = nativeAd.starRating!!.toFloat()
//            adView.starRatingView?.visibility = View.VISIBLE
//        }
//
//        if (nativeAd.advertiser == null) {
//            adView.advertiserView?.visibility = View.INVISIBLE
//        } else {
//            (adView.advertiserView as? TextView)?.text = nativeAd.advertiser
//            adView.advertiserView?.visibility = View.VISIBLE
//        }
//        adView.setNativeAd(nativeAd)
//
//        val vc = nativeAd.mediaContent?.videoController
//
//        if (nativeAd.mediaContent != null && nativeAd.mediaContent!!.hasVideoContent()) {
//            vc?.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {
//                override fun onVideoEnd() {
//                    super.onVideoEnd()
//                    showAds()
//                }
//            }
//            if (vc?.isCustomControlsEnabled == true) {
//                vc.play()
//            }
//        } else {
//            setupReloadAds()
//        }
//    }
//
//    protected open fun getLayoutToBindAds(nativeAd: NativeAd) = layoutNative
//
//    /**
//     * assign native next cho native show
//     * Hiển thị native show
//     * load luon native next
//     */
//    private fun showAds() {
//
//        //show ad
//        if (layoutNative != null && nextNativeAd != null && isReload) {
//            nativeAd?.destroy()
//            nativeAd = nextNativeAd
//            nextNativeAd = null
//            val layout = getLayoutToBindAds(nativeAd!!)
//            layout?.let {
//                pushNativeAdView(nativeAd!!, it)
//                callback?.onShowNative(it)
//                containerLayout?.removeAllViews()
//                containerLayout?.addView(it)
//                it.isVisible = true
//            }
//
//
//            loadNative(layoutNative!!.context)
//        }
//    }
//}