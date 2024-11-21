package com.lib.admob.interstitialAds

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.lib.admob.interstitialAds.base.InterCallback
import com.lib.admob.interstitialAds.base.InterSingle
import com.lib.ump.AdsConsentManager
import com.lib.dialog.LoadingAdsDialog
import com.util.CheckInternet
import com.lib.enums.LoadState
import com.util.FirebaseLogEventUtils
import com.util.SystemSharePreferenceUtils

object InterManager {
    private const val TAG = "InterManager"

    // Đợi hiển thị dialog 1s thì hiển thị quảng cáo
    private const val AD_LOADING_TIME_DELAY_SHOW_ADS = 1000L

    // hiển thị dialog loading ad 2s thì off
    private const val AD_LOADING_TIME_DELAY_FINISH = 2000L

    // Quản lý các inter
    private val interHashMap = HashMap<String, InterSingle>()

    // Dialog loading
    private var loadingAdsDialog: LoadingAdsDialog? = null

    // Inter có đang được hiện thị hay không
    private var isInterShowing = false

    // Lưu thời gian vào app
    private var timeStart: Long = System.currentTimeMillis()

    // Lưu lần cuối đóng inter
    private var timeBetween: Long = 0L

    // time interval config trên firebase
    private var interStartInterval = 5000L

    private var interBetweenInterval = 15000L

    // Lưu thời gian bat dau vào app
    fun init(timeStart: Long) {
        this.timeStart = timeStart
        timeBetween = 0L
        isInterShowing = false
    }

    fun setTimeInterval(interInterval:Long = 15000L, interStartInterval:Long = 5000L){
        this.interBetweenInterval = interInterval
        this.interStartInterval = interStartInterval
    }

    // Thêm một inter config để thêm vào interHashMap
    //them 1 single inter vao hashmap cua inter manager
    fun addInterSingle(keyRemoteConfig: String, interModel: InterSingle) {
        if (!interHashMap.containsKey(keyRemoteConfig)) {
            interHashMap[keyRemoteConfig] = interModel
        } else {
            Log.d(TAG, "Key $keyRemoteConfig đã tồn tại trong interHashMap")
        }
    }

    // get inter single
    fun getInterSingle(keyRemoteConfig: String): InterSingle? {
        return interHashMap[keyRemoteConfig]
    }

    // Kiểm tra xem inter có đang được hiển thị hay không
    //using for ad_resume check only
    fun isShowingInter(): Boolean {
        return isInterShowing
    }

    // Tải 1 quảng cáo inter
    fun loadInter(
        // Truyền context
        context: Context,

        // Key trên remote
        keyRemoteConfig: String,

        // id quảng cáo
        adUnitId: String = "",

        //is show
        isShow: Boolean = true,

        // có load hoặc show quảng cáo hay không
        isPreloadAfterShowAd: Boolean = false,

        // neu la true thi goi man B, roi show ad va nguoc lai
        isShowNextActivityBeforeAd: Boolean = true,

        //callback
        interCallback: InterCallback? = null
    ) {
        // Kiểm tra xem key đã tồn tại trong interHashMap hay chưa
        // Nếu chưa tồn tại, thêm key mới vào HashMap với trạng thái ban đầu
        val interModel = interHashMap[keyRemoteConfig] ?: run {
            val interSingle = InterSingle()
            interSingle.adUnitId = adUnitId
            interSingle.isShow = isShow
            interSingle.isPreloadAfterShowAd=isPreloadAfterShowAd
            interSingle.isShowNextActivityBeforeAd=isShowNextActivityBeforeAd
            interSingle.interCallback = interCallback
            interHashMap[keyRemoteConfig] = interSingle

            // Trả về trạng thái quảng cáo mới để tiếp tục quá trình tải
            interSingle
        }

        // Kiểm tra xem co san sang load quảng cáo hay không
        if (checkAvailableLoadInter(context, interModel)) {

            // Đặt cờ trạng thái là LOADING
            interModel.loadState = LoadState.LOADING

            //ad name for logging event firebase
            var adNameLogEvent:String = context.javaClass.simpleName+"_"+keyRemoteConfig

            //load ad
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        // Quảng cáo đã được tải
                        interModel.interstitialAd = interstitialAd
                        // Đặt lại trạng thái khi tải xong
                        interModel.loadState = LoadState.NOT_LOADING
                        Log.d(TAG, "Quảng cáo $adUnitId tải thành công!")

                        // Trả Revenue
                        interModel.interstitialAd?.onPaidEventListener =
                            OnPaidEventListener { adValue ->
                                interModel.interCallback?.onEarnRevenue(adValue)

                                //increase user ltv
                                SystemSharePreferenceUtils.updateUserLTV(context,
                                    (adValue.valueMicros / 1000000.0).toFloat()
                                )

                                //log event
                                FirebaseLogEventUtils.logEventAdPaidRevenue(context, adNameLogEvent, adUnitId, adValue)
                            }
                        interModel.interCallback?.onAdLoaded(interstitialAd)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Quảng cáo tải thất bại
                        interModel.interstitialAd = null

                        // Đặt lại trạng thái nếu tải thất bại
                        interModel.loadState = LoadState.NOT_LOADING
                        Log.d(TAG, "Quảng cáo $adUnitId tải thất bại!")
                        interModel.interCallback?.onAdFailedToLoad(loadAdError)
                        //event
                        FirebaseLogEventUtils.logEventAdFailedToLoad(context, adNameLogEvent, adUnitId, loadAdError.message)
                    }
                })

        } else {
            Log.d(TAG, "Quảng cáo $adUnitId này đang được tải!")
        }
    }

    // Hiển thị 1 quảng cáo inter
    fun showInter(
        activity: Activity, // Activity của inter cần show
        keyRemoteConfig: String, // Key trên remote
        interCallback: InterCallback? = null// call back
    ) {

        // Tìm kiếm InterModel dựa trên keyRemoteConfig
        val interSingle = interHashMap[keyRemoteConfig] ?: run {
            Log.d(TAG, "Không tìm thấy InterModel cho keyRemoteConfig: $keyRemoteConfig")
            interCallback?.onNextAction()
            return
        }

        // Kiểm tra nếu quảng cáo đã được tải và sẵn sàng để hiển thị
        if (checkAvailableShowInter(activity, interSingle)) {

            // set isInterShowing = true để thông báo là quảng cáo inter đang được hiển thị
            isInterShowing = true
            var adNameLogEvent:String = activity.javaClass.simpleName+"_"+keyRemoteConfig
            // Thiết lập các fullScreenContentCallback cho quảng cáo
            interSingle.interstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {

                    override fun onAdClicked() {
                        interCallback?.onAdClicked()
                        Log.d(TAG, "Quảng cáo $keyRemoteConfig đã được nhấn.")
                        FirebaseLogEventUtils.logEventAdClicked(activity, adNameLogEvent, interSingle.adUnitId)
                    }

                    override fun onAdDismissedFullScreenContent() {
                        isInterShowing =
                            false //set isShowInter = false để thông báo là quảng cáo inter đã không còn hiển thị
                        interCallback?.onAdDismissedFullScreenContent()
                        // Đặt lại thời khi tắt quảng cáo
                        timeBetween = System.currentTimeMillis()

                        processPreloadAndNextActivity(activity, interSingle, keyRemoteConfig, interCallback)
                        Log.d(TAG, "Quảng cáo $keyRemoteConfig đã bị đóng.")
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        isInterShowing =
                            false //set isShowInter = false để thông báo là quảng cáo inter đã không còn hiển thị
                        interCallback?.onAdFailedToShowFullScreenContent(adError)
                        // Đặt lại thời khi tắt quảng cáo
                        timeBetween = System.currentTimeMillis()

                        processPreloadAndNextActivity(activity, interSingle, keyRemoteConfig, interCallback)
                        Log.d(
                            TAG,
                            "Quảng cáo $keyRemoteConfig không thể hiển thị: ${adError.message}"
                        )

                        //event logging
                        FirebaseLogEventUtils.logEventAdFailedToShow(activity, adNameLogEvent, interSingle.adUnitId, adError.message)
                    }

                    override fun onAdImpression() {
                        interCallback?.onAdImpression()
                        Log.d(TAG, "Quảng cáo $keyRemoteConfig đã ghi nhận impression.")
                        FirebaseLogEventUtils.logEventAdImpression(activity, adNameLogEvent, interSingle.adUnitId)
                    }

                    override fun onAdShowedFullScreenContent() {
                        interCallback?.onAdShowedFullScreenContent()
                        Log.d(TAG, "Quảng cáo $keyRemoteConfig đang hiển thị.")
                    }
                }
            // Hiển thị dialog loading
            showDialogLoadingAds(activity)

            Handler(Looper.getMainLooper()).postDelayed({

                //load man hinh next trước
                if (interSingle.isShowNextActivityBeforeAd) {
                    interCallback?.onNextAction()
                }

                //roi hien thị quảng cáo
                interSingle.interstitialAd?.show(activity)

            }, AD_LOADING_TIME_DELAY_SHOW_ADS)
            cancelDialogLoadingAds(activity)
        } else {
            Log.d(TAG, "Quảng cáo $keyRemoteConfig chưa được tải hoặc không sẵn sàng để hiển thị.")
            interCallback?.onNextAction()
        }
    }

    // Xử lý khi hoàn tất show quảng cáo
    //dung de preload khi back lai man hinh va chuyen sang man tiep theo
    private fun processPreloadAndNextActivity(
        activity: Activity,
        interSingle: InterSingle,
        keyRemoteConfig: String,
        interCallback: InterCallback? = null
    ) {
        // Tải lại quảng cáo
        // Đặt quảng cáo thành null sau khi bị đóng
        interSingle.interstitialAd = null
        interSingle.loadState = LoadState.NOT_LOADING
        if (interSingle.isPreloadAfterShowAd) {
            loadInter(
                activity,
                keyRemoteConfig,
                interSingle.adUnitId,
                interSingle.isShow,
                interSingle.isPreloadAfterShowAd,
                interSingle.isShowNextActivityBeforeAd,
                interSingle.interCallback,
            )
        }

        if (!interSingle.isShowNextActivityBeforeAd) {
            interCallback?.onNextAction()
        }
    }

    // Hiển thị dialog loading
    private fun showDialogLoadingAds(activity: Activity) {
        try {
            activity.runOnUiThread {
                loadingAdsDialog = LoadingAdsDialog(activity)
                loadingAdsDialog?.show()
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Tắt dialog loading
    private fun cancelDialogLoadingAds(activity: Activity) {
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    loadingAdsDialog = if ((loadingAdsDialog != null
                                && loadingAdsDialog?.window != null
                                && loadingAdsDialog?.window?.decorView?.isAttachedToWindow == true)
                        && !activity.isDestroyed
                        && loadingAdsDialog?.isShowing == true
                    ) {
                        loadingAdsDialog?.dismiss()
                        null
                    } else {
                        null
                    }
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }
            }, AD_LOADING_TIME_DELAY_FINISH)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkAvailableShowInter(context: Context, interModel: InterSingle): Boolean {

        if (
            // check remote
            !interModel.isShow
            //pending: Nếu interstitialAd == null trả về false
            //campare voi app_resume, neu null thi load lai
            ||interModel.interstitialAd == null
            //network not connected
            || CheckInternet.haveNetworkConnection(context).not()
            //UPM not allow
            || AdsConsentManager.getInstance(context).canRequestAds.not()
            // Nếu vào app chưa lớn hơn interstitialFromStart trả về false
            || interStartInterval > (System.currentTimeMillis() - timeStart)
            // Nếu thời gian hiển thị quảng cáo giua 2 inter chưa du trả về false
            || interBetweenInterval > (System.currentTimeMillis() - timeBetween)
        ) {
            return false
        }

        return true
    }

    private fun checkAvailableLoadInter(context: Context, interSingle: InterSingle): Boolean {
        if (interSingle.loadState == LoadState.LOADING
            || interSingle.interstitialAd != null
            || !interSingle.isShow
            || CheckInternet.haveNetworkConnection(context).not()
            || AdsConsentManager.getInstance(context).canRequestAds.not()
        ) {
            return false
        }
        return true
    }
}