package com.lib.admob.nativeAds.advance.base

import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

interface NativeCallback {

    //ad listener callback
    fun onAdLoaded() {}

    fun onAdFailedToLoad(p0: LoadAdError) {}

    fun onAdClicked() {}

    fun onAdClosed() {}

    fun onAdImpression() {}

    fun onAdOpened() {}

    fun onAdSwipeGestureClicked() {}

    //OnNativeAdLoadedListener
    fun OnNativeAdLoaded(nativeAd: NativeAd){}

    //revenue
    fun onEarnRevenue(revenue: AdValue){}

    //custom event
    fun onShowNative(view: NativeAdView) {}
}
