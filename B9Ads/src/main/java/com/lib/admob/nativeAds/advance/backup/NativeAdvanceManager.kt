//
//package com.lib.admob.nativeAds.advance.backup
//
//import android.view.LayoutInflater
//import android.widget.FrameLayout
//import androidx.core.view.isVisible
//import androidx.lifecycle.Lifecycle
//import com.google.android.gms.ads.AdValue
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.nativead.NativeAd
//import com.google.android.gms.ads.nativead.NativeAdView
//import com.lib.admob.nativeAds.advance.base.NativeCallback
//import com.lib.admob.nativeAds.advance.base.NativeLibDefaultLayout
//
//class NativeAdvanceManager(
//    // id quảng cáo
//    id: String,
//
//    // lifecycle dùng để reload quảng cáo
//    lifecycle: Lifecycle? = null
//) : NativeManager(id, lifecycle) {
//
//    private var isShowNativeSpecial = false
//
//    // layout đặc biệt sẽ hiển thị khi quảng trả về các id đặc biệt
//    private var specialLayoutNative: NativeAdView? = null
//
//    // list các id đặc biệt mà khi quảng cáo trả về sẽ đổi layout
//    private var specialIds = mutableListOf<String>()
//
//    //show native with resource in app
//    override fun showNative(
//        container: FrameLayout,
//        idNativeLayout: Int,
//        idShimmer: Int,
//        callback: NativeCallback?
//    ) {
//        val newCallback = getNewCallback(callback)
//        super.showNative(container, idNativeLayout, idShimmer, newCallback)
//    }
//
//    //show native default with resource in lib
//    override fun showNative(
//        container: FrameLayout,
//        defaultStyle: NativeLibDefaultLayout,
//        callback: NativeCallback?
//    ) {
//        val newCallback = getNewCallback(callback)
//        super.showNative(container, defaultStyle, newCallback)
//    }
//
//    // Set dữ liệu về layout và id đặc biệt (layout sẽ được show khi quảng cáo trả về các id được chỉ định)
//    fun setShowOtherLayoutWhenSpecialId(
//        container: FrameLayout,
//        specialLayout: Int,
//        listId: List<String>
//    ) {
//        specialIds.clear()
//        specialIds.addAll(listId)
//        val context = container.context
//        val otherLayout = LayoutInflater.from(context).inflate(specialLayout, null)
//        if (otherLayout is NativeAdView) {
//            specialLayoutNative = otherLayout
//            container.addView(otherLayout)
//            specialLayoutNative?.isVisible = false
//        }
//    }
//
//    override fun getLayoutToBindAds(nativeAd: NativeAd): NativeAdView? {
//        val adsId = nativeAd.responseInfo?.loadedAdapterResponseInfo?.adSourceId
//        return if (adsId in specialIds && specialLayoutNative != null) {
//            isShowNativeSpecial = true
//            specialLayoutNative
//        } else {
//            isShowNativeSpecial = false
//            layoutNative
//        }
//    }
//
//    private fun onlyClickAtButton(adView: NativeAdView, onlyClickBtn: Boolean = true) {
//        val elements = listOf(
//            adView.mediaView,
//            adView.headlineView,
//            adView.bodyView,
//            adView.iconView,
//            adView.priceView,
//            adView.starRatingView,
//            adView.storeView,
//            adView.advertiserView
//        )
//        elements.forEach {
//            it?.isClickable = onlyClickBtn.not()
//        }
//    }
//
//    private fun getNewCallback(callback: NativeCallback?): NativeCallback {
//        return object : NativeCallback {
//            override fun onShowNative(view: NativeAdView) {
//                if (isShowNativeSpecial) {
//                    onlyClickAtButton(view)
//                } else {
//                    onlyClickAtButton(view, false)
//                }
//                callback?.onShowNative(view)
//            }
//
//            override fun onEarnRevenue(revenue: AdValue) {
//                callback?.onEarnRevenue(revenue)
//            }
//
//            override fun onAdClicked() {
//                callback?.onAdClicked()
//            }
//
//            override fun onAdFailedToLoad(p0: LoadAdError) {
//                callback?.onAdFailedToLoad(p0)
//            }
//
//            override fun onAdLoaded() {
//                callback?.onAdLoaded()
//            }
//        }
//    }
//}
