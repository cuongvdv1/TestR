//package com.lib.ump
//
//import android.app.Activity
//import android.content.Context
//import android.util.Log
//import com.google.android.ump.ConsentDebugSettings
//import com.google.android.ump.ConsentInformation
//import com.google.android.ump.ConsentRequestParameters
//import com.google.android.ump.UserMessagingPlatform
//
//class AdsConsentManager3 private constructor(context: Context) {
//
//    private val TAG = "AdsConsentManager3"
//    private val consentInformation = UserMessagingPlatform.getConsentInformation(context)
//
//    fun interface OnConsentGatheringCompleteListener {
//        fun consentGatheringComplete(result: Boolean)
//    }
//
//    val canRequestAds: Boolean
//        get() = consentInformation.canRequestAds()
//
//
//
//    //VietAnh fix get UTM alway true
//    private fun getConsentResult(context: Context): Boolean {
//        val consentResult = context.getSharedPreferences(context.packageName + "_preferences", 0)
//            .getString("IABTCF_PurposeConsents", "")
//        return consentResult!!.isEmpty() || consentResult[0].toString() == "1"
//    }
//
//    //function when no using device ID
////    fun gatherConsent(
////        activity: Activity,
////        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener,
////    ) {
////
////        val params = ConsentRequestParameters.Builder().build()
////        Log.v(TAG, "canRequest1:"+getConsentResult(activity))
////        consentInformation.requestConsentInfoUpdate(
////            activity,
////            params,
////            {
////                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
////                    if (formError != null) {
////                        Log.v(TAG, "formError: ${formError.errorCode} ${formError.message}")
////                    } else {
////                        onConsentGatheringCompleteListener.consentGatheringComplete(getConsentResult(activity))
////                    }
////                    Log.v(TAG, "canRequest2:"+getConsentResult(activity))
////                }
////            },
////            { requestConsentError ->
////                Log.v(
////                    TAG,
////                    "requestConsentError: ${requestConsentError.errorCode} ${requestConsentError.message}"
////                )
////            },
////        )
////        Log.v(TAG, "canRequest3:"+getConsentResult(activity))
////    }
//
//    //function when using device ID
//
//    fun gatherConsent(
//        activity: Activity,
//        //for debug deviceID
//        deviceHashId: String,
//        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener
//    ) {
//
//        var params: ConsentRequestParameters? = null
//
//        if (deviceHashId.isNotEmpty()) {
//            val debugSettings =
//                ConsentDebugSettings.Builder(activity)
//                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//                    .addTestDeviceHashedId(deviceHashId)
//                    .build()
//            params = ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()
//
//        }else{
//            params = ConsentRequestParameters.Builder().build()
//        }
//
//        consentInformation.requestConsentInfoUpdate(
//            activity,
//            params,
//            {
//
//                var statusBeforeConsent = consentInformation.consentStatus
//                Log.v(TAG, "statusBeforeConsent: $statusBeforeConsent")
//
//                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
//                    if (formError != null) {
//                        Log.v(TAG, "formError: ${formError.errorCode} ${formError.message}")
//                    } else {
//
//                        var consentFormResult = getConsentResult(activity)
//                        onConsentGatheringCompleteListener.consentGatheringComplete(consentFormResult)
//
//                        //check case
//                        var statusAfterConsent = consentInformation.consentStatus
//                        Log.v(TAG, "statusAfterConsent: $statusAfterConsent")
//
//                        if ((statusBeforeConsent == ConsentInformation.ConsentStatus.NOT_REQUIRED) &&
//                            (statusAfterConsent == ConsentInformation.ConsentStatus.NOT_REQUIRED)) {
//                            Log.v(TAG, "user not in EU, consent not require, consentFormResult = $consentFormResult")
//                        }
//                        if ((statusBeforeConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
//                            (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
//                            (consentFormResult)){
//                            Log.v(TAG, "user obtained OK consent, consentFormResult = $consentFormResult")
//                        }
//
//                        if ((statusBeforeConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
//                            (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
//                            (!consentFormResult)){
//                            Log.v(TAG, "user obtained NG consent, consentFormResult = $consentFormResult")
//                        }
//
//                        if ((statusBeforeConsent == ConsentInformation.ConsentStatus.REQUIRED) &&
//                            (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
//                            (consentFormResult)){
//                            Log.v(TAG, "user new accept consent, consentFormResult = $consentFormResult")
//                        }
//
//                        if ((statusBeforeConsent == ConsentInformation.ConsentStatus.REQUIRED) &&
//                                    (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
//                            (!consentFormResult)){
//                            Log.v(TAG, "user new refuse consent, consentFormResult = $consentFormResult")
//                        }
//
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
//
//    }
//
//
//    companion object {
//        @Volatile
//        private var instance: AdsConsentManager3? = null
//
//        fun getInstance(context: Context) =
//            instance
//                ?: synchronized(this) {
//                    instance ?: AdsConsentManager3(context).also { instance = it }
//                }
//    }
//}
