/**
 * ANH: xem lai khi man hinh bi khoa xong build, mo khoa ra khong chay duoc
 *
 */
package com.lib.admob.splashAds

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.lib.admob.splashAds.callback.SplashCallback
import com.lib.admob.splashAds.type.SplashType
import com.lib.ump.AdsConsentManager
import com.lib.dialog.LoadingAdsDialog
import com.lib.enums.LoadState
import com.util.CheckInternet
import com.util.FirebaseLogEventUtils
import com.util.SystemSharePreferenceUtils
import kotlin.random.Random

class SplashAdManager(

    //ANH không muốn đưa activity vào class
    //dua qua loi gọi hàm loadAndShowAdSplash
    private val activity: Activity,

    // id quảng cáo app open
    private val idAppOpen: String,

    // id quảng cáo inter
    private val idInter: String,

    // isShow App open
    private val isShowAppOpen: Boolean = true,

    // isShow Inter
    private val isShowInter: Boolean = true,

    //remote config splash - max_loading_time
    private val AdMaxLoadingTime: Long = 25000L,

    //rate of app_open, sum 100
    private val rate: Int = 30,

    // lifecycle dùng để reload quảng cáo
    private val lifecycle: Lifecycle,

    // callback: thực hiện các hành động tại các thời điểm khác nhau của ads
    private val splashCallback: SplashCallback? = null,

    ) {
    // Log cat
    val TAG = "SplashManager"

    //adName for log event inter
    var adNameInterLogEvent:String = "splash_inter"

    //adName for log event appopen
    var adNameAppopenLogEvent:String = "splash_appopen"

    //giu trang thai hiển thị dialog loading 1 giây thì hiển thị quảng cáo
    private val TIME_DELAY_SHOW_ADS = 1000L

    //giu trang thai hien tai, sau 2s thi tat diaglog loading
    private val TIME_DELAY_CANCEL_DIALOG_LOADING = 2000L

    private var loadingAdsDialog: LoadingAdsDialog? = null // Dialog loading

    // Có đang tải quảng cáo không
    private var loadState = LoadState.NOT_LOADING

    // Quảng cáo inter
    private var interstitialAd: InterstitialAd? = null

    // Quảng cáo app open
    private var appOpenAd: AppOpenAd? = null

    // Hiển thị quảng cáo nào appopen or inter no no
    private var splashType = SplashType.NO_ADS

    //status quan ly trang thai load va show
    //khi load nhamh qua ma chua den trang thai on_resume thi chua show
    //co the hieu ten khac la isON_RESUME_STATUS
    private var isAdReadyToShow = false

    /*pending isAdCancelToShow va isInitHandlerShowAds dung de quan ly trang thai
    chuan bi show thi thoat ra home xong vao lai app
    cho nay kho hieu, viet lai sau
    */
    private var isAdCancelToShow = false

    /*truoc khi goi handle1s show ad thi set = true,
    chay xong set =false de biet handle da duoc chay hay chua
     */
    private var isInitHandlerShowAds = false

    // Handle 1s delay hiển thị quảng cáo
    private var handlerShowAds: Handler? = null

    // Handle time out dung de dem max properties timeOut(s)
    //het timeOut(s) set isTimeOut = true
    private var handlerTimeOut: Handler? = null

    //max load ad timeOut(s)
    //neu den timeOut(s) ma chua load duoc thi trang thai timeout -> chuyen man ma khong show ad
    private var isTimeOut = false

    init {
        // Đăng ký observer vào lifecycle
        lifecycle.addObserver( object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                // Lắng nghe các sự kiện lifecycle
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {

                        //set trang thai ready show ad
                        isAdReadyToShow = true

                        //from another app back to splash, case da chay qua handle show_ad
                        if (isAdCancelToShow) {
                            isAdCancelToShow = false

                            // Hiển thị lại quảng cáo ở đây
                            checkShowAdsFromBackground()
                            Log.d(TAG, "isAdCancelToShow")

                        //from another app back to splash, case chua chay qua handle show
                        } else if (isInitHandlerShowAds) {
                            isInitHandlerShowAds = false

                            // Hiển thị lại quảng cáo ở đây
                            checkShowAdsFromBackground()
                            Log.d(TAG, "isInitHandler")
                        }
                        Log.d(TAG, "ON_RESUME")
                    }

                    Lifecycle.Event.ON_STOP -> {

                        //cancel handle show ad
                        handlerShowAds?.removeCallbacksAndMessages(null)
                        isAdReadyToShow = false
                        Log.d(TAG, "ON_STOP")
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        removeAll(source, this)
                        Log.d(TAG, "ON_DESTROY")
                    }

                    else -> {}
                }
            }
        })
    }

    fun loadAndShowAdSplash() {

        // Kiểm tra nếu một trong 2 để false thì show cái còn lại
        if (CheckInternet.haveNetworkConnection(activity).not()
            || AdsConsentManager.getInstance(activity).canRequestAds.not()
            || (!isShowAppOpen && !isShowInter)
        ) {
            splashType = SplashType.NO_ADS
        } else if (!isShowAppOpen || !isShowInter) {
            if (isShowAppOpen) {
                Log.d(TAG, "Hiển thị app open")
                splashType = SplashType.APP_OPEN
            } else if (isShowInter) {
                Log.d(TAG, "Hiển thị inter")
                splashType = SplashType.INTER
            }
        } else {
            Log.d(TAG, "rate: $rate")
            val random = Random.nextInt(1, 101)
            Log.d(TAG, "random: $random")
            splashType = if (random <= rate) {
                Log.d(TAG, "Hiển thị app open")
                SplashType.APP_OPEN
            } else {
                Log.d(TAG, "Hiển thị inter")
                SplashType.INTER
            }
        }

        // Load quảng cáo
        when (splashType) {
            SplashType.APP_OPEN -> {
                // Khởi tạo time out
                initTimeOut()
                loadAppOpen()
            }

            SplashType.INTER -> {
                // Khởi tạo time out
                initTimeOut()
                loadInter()
            }

            else -> {
                //default de la loai appopen
                splashCallback?.onNextAction(true)
            }
        }

    }

    private fun initTimeOut() {
        handlerTimeOut = Handler(Looper.getMainLooper())
        handlerTimeOut?.postDelayed({
            //het timeout ma van null thi set isTimeOut = true
            if (interstitialAd == null && appOpenAd == null) {
                isTimeOut = true
                splashCallback?.onNextAction(true)
            }
        }, AdMaxLoadingTime)
    }

    fun cancelHandlerTimeOut() {
        try {
            handlerTimeOut?.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Load inter
    private fun loadInter() {

        // Đặt cờ trạng thái là LOADING
        loadState = LoadState.LOADING

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            idInter,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {

                    // Nếu timeout không hiển thị quảng cáo
                    if (isTimeOut) {
                        Log.d(TAG, "Đã hết thời gian đợi")
                        return
                    }
                    //neu chua het timeout
                    //Hủy time out
                    cancelHandlerTimeOut()

                    //xu ly quảng cáo đã được tải
                    this@SplashAdManager.interstitialAd = interstitialAd
                    loadState = LoadState.NOT_LOADING // Đặt lại trạng thái khi tải xong

                    //callback
                    splashCallback?.onAdLoaded(null, interstitialAd, false)
                    Log.d(TAG, "Quảng cáo $idInter tải thành công!")

                    // Trả Revenue
                    this@SplashAdManager.interstitialAd?.onPaidEventListener =
                        OnPaidEventListener { adValue ->
                            splashCallback?.onEarnRevenue(adValue)
                            FirebaseLogEventUtils.logEventAdPaidRevenue(activity, adNameInterLogEvent, idInter, adValue)
                        }

                    //show ad
                    showInter()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {

                    // Nếu timeout
                    if (isTimeOut) {
                        Log.d(TAG, "Ad loaded but process was cancelled.")
                        return
                    }

                    //neu không timeout
                    this@SplashAdManager.interstitialAd = null
                    loadState = LoadState.NOT_LOADING // Đặt lại trạng thái khi tải xong
                    splashCallback?.onAdFailedToLoad(loadAdError, false)
                    Log.d(TAG, "Quảng cáo $idInter tải thất bại!")
                    //log event
                    FirebaseLogEventUtils.logEventAdFailedToLoad(activity, adNameInterLogEvent, idInter, loadAdError.message)
                }
            })
    }

    private fun loadAppOpen() {
        loadState = LoadState.LOADING  // Đặt cờ trạng thái là LOADING
        val adRequest = AdRequest.Builder().build()

        AppOpenAd.load(activity, idAppOpen, adRequest, object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                super.onAdLoaded(appOpenAd)

                // Nếu timeout không hiển thị quảng cáo
                if (isTimeOut) {
                    Log.d(TAG, "Ad loaded but process was cancelled.")
                    return
                }

                // Hủy time out
                cancelHandlerTimeOut()

                // xử lý quảng cáo đã được tải
                this@SplashAdManager.appOpenAd = appOpenAd
                loadState = LoadState.NOT_LOADING // Đặt lại trạng thái khi tải xong
                splashCallback?.onAdLoaded(appOpenAd, null, true)
                Log.d(TAG, "Quảng cáo $idAppOpen tải thành công!")

                // Trả Revenue
                this@SplashAdManager.appOpenAd?.onPaidEventListener = OnPaidEventListener { adValue ->
                    splashCallback?.onEarnRevenue(adValue)

                    //increase user ltv
                    SystemSharePreferenceUtils.updateUserLTV(activity,
                        (adValue.valueMicros / 1000000.0).toFloat()
                    )

                    //logevent
                    FirebaseLogEventUtils.logEventAdPaidRevenue(activity, adNameAppopenLogEvent, idAppOpen, adValue)
                }

                //show ads
                showAppOpen()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)

                // Nếu timeout
                if (isTimeOut) {
                    Log.d(TAG, "Đã hết thời gian đợi")
                    return
                }

                this@SplashAdManager.appOpenAd = null
                loadState = LoadState.NOT_LOADING // Đặt lại trạng thái khi tải xong
                splashCallback?.onAdFailedToLoad(loadAdError, true)
                Log.d(TAG, "Quảng cáo $idAppOpen tải thành công!")

                //log event
                FirebaseLogEventUtils.logEventAdFailedToLoad(activity, adNameAppopenLogEvent, idAppOpen, loadAdError.message)
            }
        })
    }

    private fun showInter() {

        // Thiết lập các callback cho quảng cáo
        interstitialAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    splashCallback?.onAdDismissedFullScreenContent(false)
                    splashCallback?.onNextAction(false)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadState = LoadState.NOT_LOADING
                    loadAndShowAdSplash()
                    splashCallback?.onAdFailedToShowFullScreenContent(adError, false)
                    FirebaseLogEventUtils.logEventAdFailedToShow(activity, adNameInterLogEvent, idInter, adError.message)
                }

                override fun onAdShowedFullScreenContent() {
                    splashCallback?.onAdShowedFullScreenContent(false)
                }

                override fun onAdImpression() {
                    splashCallback?.onAdImpression(false)
                    FirebaseLogEventUtils.logEventAdImpression(activity, adNameInterLogEvent, idInter)
                }

                override fun onAdClicked() {
                    splashCallback?.onAdClicked(false)
                    FirebaseLogEventUtils.logEventAdClicked(activity, adNameInterLogEvent, idInter)
                }
            }

        //show ads
        showAds()
    }

    private fun showAppOpen() {

        appOpenAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    splashCallback?.onAdDismissedFullScreenContent(true)
                    splashCallback?.onNextAction(true)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    loadState = LoadState.NOT_LOADING
                    loadAndShowAdSplash()
                    splashCallback?.onAdFailedToShowFullScreenContent(adError, true)
                    FirebaseLogEventUtils.logEventAdFailedToShow(activity, adNameAppopenLogEvent, idAppOpen, adError.message)
                }

                override fun onAdShowedFullScreenContent() {
                    splashCallback?.onAdShowedFullScreenContent(true)
                }

                override fun onAdImpression() {
                    splashCallback?.onAdImpression(true)
                    FirebaseLogEventUtils.logEventAdImpression(activity, adNameAppopenLogEvent, idAppOpen)
                }

                override fun onAdClicked() {
                    splashCallback?.onAdClicked(true)
                    FirebaseLogEventUtils.logEventAdClicked(activity, adNameAppopenLogEvent, idAppOpen)
                }
            }

        showAds()
    }

    // show ads inter or splash
    private fun showAds() {

        isInitHandlerShowAds = true

        // Hiển thị dialog loading
        showDialogLoadingAds()

        handlerShowAds = Handler(Looper.getMainLooper())

        handlerShowAds?.postDelayed({

            if (isAdReadyToShow) {
                when (splashType) {
                    // Hiển thị quảng cáo app open
                    SplashType.APP_OPEN -> appOpenAd?.show(activity)
                    // Hiển thị quảng cáo app open
                    SplashType.INTER -> interstitialAd?.show(activity)
                    //no ad
                    else -> splashCallback?.onNextAction(true)
                }
            //trang thai ko the show, vi du bam nut home->on_stop
            } else {
                cancelDialogLoadingAds()
                isAdCancelToShow = true
            }
            isInitHandlerShowAds = false
        }, TIME_DELAY_SHOW_ADS)
        cancelDialogLoadingAds()
    }

    //Goi trong on_resume, vi du case dang hien thi activity splash, nhan nut home
    //khi quay lai thi goi trong on_resume de show ad
    private fun checkShowAdsFromBackground() {
        when (splashType) {
            SplashType.APP_OPEN -> showAppOpen()
            SplashType.INTER -> showInter()
            else -> splashCallback?.onNextAction(true)
        }

    }

    // Hiển thị dialog loading
    private fun showDialogLoadingAds() {
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
    private fun cancelDialogLoadingAds() {
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
            }, TIME_DELAY_CANCEL_DIALOG_LOADING)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeAll(source: LifecycleOwner, lifecycleEventObserver:LifecycleEventObserver) {
        try {
            source.lifecycle.removeObserver(lifecycleEventObserver)
            handlerShowAds?.removeCallbacksAndMessages(null)
            handlerTimeOut?.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}