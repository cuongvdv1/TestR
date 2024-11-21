package com.lib.admob.resumeAds.callback

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError

interface OpenCallback {

    //AppOpenAdLoadCallback
    // Tải thành công ads
    fun onAdLoaded(){}

    // Tải quảng cáo thất bại
    fun onAdFailedToLoad(loadAdError: LoadAdError){}

    //fullScreenContentCallback
    fun onAdClicked() {}

    fun onAdDismissedFullScreenContent() {}

    fun onAdFailedToShowFullScreenContent(error: AdError) {}

    fun onAdImpression() {}

    fun onAdShowedFullScreenContent() {}

    //revenue
    fun onEarnRevenue(revenue: AdValue) {}

    //custom
    //action chuyển màn sau khi show quảng cáo
    fun onNextAction(){}
}
