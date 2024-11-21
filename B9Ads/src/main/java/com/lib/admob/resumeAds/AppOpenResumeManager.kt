/**
 * remote config ad_id va is_show call function to setting
 * is_show already using for check before load and show function
 */

package com.lib.admob.resumeAds

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.lib.admob.interstitialAds.InterManager
import com.lib.admob.resumeAds.callback.OpenCallback
import com.lib.ump.AdsConsentManager
import com.lib.dialog.LoadingAdsDialog
import com.lib.enums.LoadState
import com.util.FirebaseLogEventUtils
import com.util.SystemSharePreferenceUtils
import java.util.Date

object AppOpenResumeManager {
    private val TAG = "AppOpenResumeManager"

    // Đợi hiển thị dialog 1s thì hiển thị quảng cáo milliseconds
    private val AD_LOADING_TIME_DELAY_SHOW_ADS:Long = 1000L

    //ad timeout
    //sau thoi gian nay thi ad loaded se duoc xem la khong available de show (hours)
    private val AD_LOADED_TIMEOUT_HOURS:Long = 4

    private var appOpenAd: AppOpenAd? = null

    private var loadState = LoadState.NOT_LOADING

    //remote config ad_id
    private var ad_id: String = ""

    //remote config is_show
    private var is_show: Boolean = true

    //bien check trang thai co duoc show hay khong
    //vi du dang hien thi inter thi khong duoc show
    private var isEnable = true

    //check co dang show resume hay khong
    var isShowingAd = false

    // Thời điểm cuối cùng quảng cáo tải xong
    private var timeAdLoaded: Long = 0

    // Id của quảng cáo
    fun setId(id: String) {
        ad_id = id
    }

    // trạng thái có show hay không
    fun setIsShow(isShow: Boolean) {
        is_show = isShow
    }

    fun getIsShow():Boolean {
        return is_show
    }

    // Enable/ disable ads resume
    fun setEnableAdsResume(enable: Boolean) {
        this.isEnable = enable
    }

    // Kiểm tra quảng cáo có khả dụng không
    //neu qua 4 tieng thi khong show nua
    private fun isAdLoadedAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(AD_LOADED_TIMEOUT_HOURS)
    }

    // Kiếm tra quảng cáo có được tải quá lâu rồi không
    private fun wasLoadTimeLessThanNHoursAgo(hour: Long): Boolean {
        val dateDifference: Long = Date().time - timeAdLoaded
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * hour
    }

    // load quảng cáo
    fun loadAdResume(context: Context) {

        //neu khong su dụng
        if (!is_show)
            return

        //neu da load roi
        if (isAdLoadedAvailable())
            return

        if (loadState == LoadState.LOADING || ad_id.isBlank()) {
            return
        }

        loadState = LoadState.LOADING
        val request = AdRequest.Builder().build()
        var adNameLogEvent:String = context.javaClass.simpleName+"_"+"appopen_resume"
        AppOpenAd.load(
            context,
            ad_id,
            request,
            object : AppOpenAdLoadCallback() {

                //load success cong
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadState = LoadState.NOT_LOADING
                    timeAdLoaded = Date().time
                    Log.d(TAG, "onAdLoaded.")
                }
                //load failed
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    loadState = LoadState.NOT_LOADING
                    Log.d(TAG, "onAdFailedToLoad: " + loadAdError.message)
                    FirebaseLogEventUtils.logEventAdFailedToLoad(context, adNameLogEvent, ad_id, loadAdError.message)
                }
            },
        )
    }

    // show quảng cáo nếu khả dụng
    fun showAdResume(activity: Activity, callback: OpenCallback? = null) {

        //neu khong su dụng
        if (!is_show){
            return
        }


        //trang thai khong show
        if (isEnable.not()) {
            Log.d(TAG, "Ads resume disable")
            return
        }

        //neu dang show inter
        if (InterManager.isShowingInter()) {
            Log.d(TAG, "The interstitial ad is already showing.")
            return
        }

        //neu dang show appopen_resume khac
        if (isShowingAd) {
            Log.d(TAG, "The app open ad is already showing.")
            return
        }

        //neu khong available thi load truoc de lan sau show
        if (isAdLoadedAvailable().not()) {
            Log.d(TAG, "The app open ad is not ready yet.")
            if (AdsConsentManager.getInstance(activity.baseContext).canRequestAds) {
                loadAdResume(activity.baseContext)
            }
            return
        }

        //neu OK thì bat dau show
        val loadingAdsDialog = LoadingAdsDialog(activity)
        var adNameLogEvent:String = activity.javaClass.simpleName+"_"+"appopen_resume"
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "onAdClicked.")
                callback?.onAdClicked()
                FirebaseLogEventUtils.logEventAdClicked(activity, adNameLogEvent, ad_id)

            }

            override fun onAdImpression() {
                super.onAdImpression()
                Log.d(TAG, "onAdImpression.")
                callback?.onAdImpression()
                FirebaseLogEventUtils.logEventAdImpression(activity, adNameLogEvent, ad_id)
            }

            // khi tat quang cao
            override fun onAdDismissedFullScreenContent() {
                //next action callback
                //load welcomeback ad before
                callback?.onNextAction()

                //end then other processing
                super.onAdDismissedFullScreenContent()
                appOpenAd = null
                isShowingAd = false
                Log.d(TAG, "onAdDismissedFullScreenContent.")

                //khi tat ad thi load truoc ad
                if (AdsConsentManager.getInstance(activity.baseContext).canRequestAds) {
                    loadAdResume(activity)
                }

                callback?.onAdDismissedFullScreenContent()
            }

            // khi khong show duoc quang cao
            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                appOpenAd = null
                isShowingAd = false
                Log.d(TAG, "onAdFailedToShowFullScreenContent: $p0")
                //khi show fail thì load lai ad
                if (AdsConsentManager.getInstance(activity.baseContext).canRequestAds) {
                    loadAdResume(activity)
                }
                loadingAdsDialog.dismiss()
                callback?.onAdFailedToShowFullScreenContent(p0)

                //event logging
                FirebaseLogEventUtils.logEventAdFailedToShow(activity, adNameLogEvent, ad_id, p0.message)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "onAdShowedFullScreenContent.")
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingAdsDialog.dismiss()
                    callback?.onAdShowedFullScreenContent()
                }, AD_LOADING_TIME_DELAY_SHOW_ADS)
            }
        }

        appOpenAd?.setOnPaidEventListener (object : OnPaidEventListener {
            override fun onPaidEvent(p0: AdValue) {
                Log.d(TAG, "onPaidEvent: $p0")
                callback?.onEarnRevenue(p0)

                //increase user ltv
                SystemSharePreferenceUtils.updateUserLTV(activity,
                    (p0.valueMicros / 1000000.0).toFloat()
                )

                //log firebase event
                FirebaseLogEventUtils.logEventAdPaidRevenue(activity, adNameLogEvent, ad_id, p0)
            }
        })

        loadingAdsDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            isShowingAd = true
            appOpenAd?.show(activity)
        }, 1000)
    }


}
