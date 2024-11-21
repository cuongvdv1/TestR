//package com.lib.ump
//
//import android.app.Activity
//import android.content.Context
//import android.util.Log
//import com.google.android.ump.ConsentDebugSettings
//import com.google.android.ump.ConsentRequestParameters
//import com.google.android.ump.UserMessagingPlatform
//
//class AdsConsentManager2 private constructor(context: Context) {
//
//    private val TAG = "AdsConsentManager"
//    private val consentInformation = UserMessagingPlatform.getConsentInformation(context)
//
//    fun interface OnConsentGatheringCompleteListener {
//        fun consentGatheringComplete(result: Boolean)
//    }
//
//
//    val canRequestAds: Boolean = getConsentResult(context)
//
//    //VietAnh fix get UTM alway true
//    private fun getConsentResult(context: Context): Boolean {
//        val consentResult = context.getSharedPreferences(context.packageName + "_preferences", 0)
//            .getString("IABTCF_PurposeConsents", "")
//        return consentResult!!.isEmpty() || consentResult[0].toString() == "1"
//    }
//
//    //function when no using device ID
//    fun gatherConsent(
//        activity: Activity,
//        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener,
//    ) {
//
//        val params = ConsentRequestParameters.Builder().build()
//
//        consentInformation.requestConsentInfoUpdate(
//            activity,
//            params,
//            {
//                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
//                    if (formError != null) {
//                        Log.v(TAG, "formError: ${formError.errorCode} ${formError.message}")
//                    } else {
//                        onConsentGatheringCompleteListener.consentGatheringComplete(getConsentResult(activity))
//                    }
//                }
//            },
//            { requestConsentError ->
//                Log.v(
//                    TAG,
//                    "requestConsentError: ${requestConsentError.errorCode} ${requestConsentError.message}"
//                )
//            },
//        )
//    }
//
//    //function when using device ID
//    fun gatherConsentDebug(
//        enableDebug : Boolean,
//        activity: Activity,
//        deviceHashId: String,
//        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener
//    ) {
//        val debugSettings =
//            ConsentDebugSettings.Builder(activity)
//                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//                .addTestDeviceHashedId(deviceHashId)
//                .build()
//
//        var params: ConsentRequestParameters? = null
//        if (enableDebug){
//            params = ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()
//        }
//        else{
//            params = ConsentRequestParameters.Builder().build()
//        }
//
//
//        consentInformation.requestConsentInfoUpdate(
//            activity,
//            params,
//            {
//
//                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
//                    if (formError != null) {
//                        Log.v(TAG, "formError: ${formError.errorCode} ${formError.message}")
//                    } else {
//                        onConsentGatheringCompleteListener.consentGatheringComplete(getConsentResult(activity))
//                    }
//                }
//            },
//            { requestConsentError ->
//                Log.v(
//                    TAG,
//                    "requestConsentError: ${requestConsentError.errorCode} ${requestConsentError.message}"
//                )
//            },
//        )
//    }
//
//
//    companion object {
//        @Volatile
//        private var instance: AdsConsentManager2? = null
//
//        fun getInstance(context: Context) =
//            instance
//                ?: synchronized(this) {
//                    instance ?: AdsConsentManager2(context).also { instance = it }
//                }
//    }
//}
