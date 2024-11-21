
/**
 * native manager in reload by timer class
 * pending ANH CUONG spec (priority)
 *  khi gap video huy timer va setting auto reload khi het video
 *  khi khong phai video thi setting timer theo trang thai enable/disable va thoi gian interval trong firebase
 *  khi activity pause, stop -> huy handle timer
 */

package com.lib.admob.nativeAds.advance.manager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import com.util.FirebaseLogEventUtils

open class NativeAdvanceManager1(

    // id quảng cáo
    private val ad_id: String,

    // lifecycle dùng để reload quảng cáo
    lifecycle: Lifecycle? = null

) : NativeAdvanceManager0(ad_id, lifecycle) {

    //remote config - is_auto_reload_timer
    private var isAutoReloadByTimer = true

    //remote config reload time interval - ad_reload_timer_interval
    //millisecond
    private var timerReloadInterval = 0L

    // Handler phục vụ reload quảng cáo by time
    private val handler = Handler(Looper.getMainLooper())

    public fun setReloadTimerInfo(isAuto: Boolean, timeInterval: Long) {
        isAutoReloadByTimer = isAuto

        //time interval in millisecond
        //config in firebase is second
        timerReloadInterval = timeInterval*1000
    }

    // Đặt thời gian để reload quảng cáo
    private fun setTimerHandleReloadAd() {
        handler.removeCallbacksAndMessages(null)
        if ((timerReloadInterval > 0) && isAutoReloadByTimer) {
            handler.postDelayed({
                reloadNow()

                //log event
                var context = nativeAdView?.context
                var bundle = Bundle()
                bundle.putString("v1_ad_id", ad_id)
                bundle.putString("v1_ad_name", adName)
                bundle.putString("v1_des", "no video so reload timer")
                FirebaseLogEventUtils.logEventCommonWithName(context,"ad_native_reload_timer", bundle)

            }, timerReloadInterval)
        }
    }

    //when show ad check
    //neu video khong show thi reload by time
    override fun onCheckNativeNotMediaVideo() {
        super.onCheckNativeNotMediaVideo()
        setTimerHandleReloadAd()

    }

}