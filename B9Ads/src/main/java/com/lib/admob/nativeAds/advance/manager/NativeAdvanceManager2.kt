/**
 * native manager special defend on mediation adsource
 * adsource: Fan
 */

package com.lib.admob.nativeAds.advance.manager

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.util.FirebaseLogEventUtils

class NativeAdvanceManager2(
    private var ad_id: String,
    lifecycle: Lifecycle? = null
) : NativeAdvanceManager1(ad_id, lifecycle) {

    //for Fan sourceID list
    //for test ad "7068401028668408324" (admob test)
    private var listMediationFanSourceID = mutableListOf<String>("10568273599589928883","11198165126854996598")

    //if source is FAN, is disable click in ad?
    private var isChangeButtonClickOnlyWhenFan = true

    private fun isMediationFanSource(): Boolean{
        var strAdSourceID = this.nativeAd?.responseInfo?.loadedAdapterResponseInfo?.adSourceId?:"0"
        return strAdSourceID in listMediationFanSourceID
    }

    //disable click in adNativeView
    private fun changeClickButtonOnlyWhenFan(){

        if(!isChangeButtonClickOnlyWhenFan)
            return

        if (isMediationFanSource()){
            this.nativeAdView?.let {
                setClickButtonOnly(it,true)
                var context = nativeAdView?.context
                FirebaseLogEventUtils.logEventAdNativeChangeButtonClickOnly(context, adName, ad_id)
            }
        }
    }

    private fun setClickButtonOnly(adView: NativeAdView, onlyClickBtn: Boolean = true) {
        val elements = listOf(
            adView.mediaView,
            adView.headlineView,
            adView.bodyView,
            adView.iconView,
            adView.priceView,
            adView.starRatingView,
            adView.storeView,
            adView.advertiserView
        )
        elements.forEach {
            it?.isClickable = onlyClickBtn.not()
        }
    }

    fun setChangeClickButtonOnlyWhenFan(isChange: Boolean) {
        isChangeButtonClickOnlyWhenFan = isChange
    }

    /**
     * override here to change click enable status in adview
     */
    override fun showAndPreloadAd() {
        super.showAndPreloadAd()
        changeClickButtonOnlyWhenFan()
    }


}