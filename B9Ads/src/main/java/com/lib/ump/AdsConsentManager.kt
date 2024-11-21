package com.lib.ump

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.util.FirebaseLogEventUtils

class AdsConsentManager private constructor(context: Context) {

    private val TAG = "AdsConsentManager"
    private val consentInformation = UserMessagingPlatform.getConsentInformation(context)

    //ANH edit
    //ad result to return
    fun interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(result: Boolean)
    }

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()


    /**
     * ANH ad new for consent form result
     * canRequestAd always return true
     */

    private fun getConsentResult(context: Context): Boolean {
        val consentResult = context.getSharedPreferences(context.packageName + "_preferences", 0)
            .getString("IABTCF_PurposeConsents", "")
        return consentResult!!.isEmpty() || consentResult[0].toString() == "1"
    }

    /**
     * ANH gatherConsent
     * using for both test device and real device
     */
    fun gatherConsent(
        activity: Activity,
        //for debug deviceID
        deviceHashId: String,
        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener
    ) {

        var params: ConsentRequestParameters? = null

        if (deviceHashId.isNotEmpty()) {
            val debugSettings =
                ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId(deviceHashId)
                    .build()
            params = ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()

        }else{
            params = ConsentRequestParameters.Builder().build()
        }

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {

                var statusBeforeConsent = consentInformation.consentStatus
                Log.v(TAG, "statusBeforeConsent: $statusBeforeConsent")

                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {

                        //form error also call consentCallback
                        onConsentGatheringCompleteListener.consentGatheringComplete(false)

                        Log.v(TAG, "formError: ${formError.errorCode} ${formError.message}")

                        //ump log
                        FirebaseLogEventUtils.logEventAdConsent(activity, "consent_formError: "+ formError.message)

                    } else {

                        var consentFormResult = getConsentResult(activity)
                        onConsentGatheringCompleteListener.consentGatheringComplete(consentFormResult)

                        //check case
                        var statusAfterConsent = consentInformation.consentStatus
                        Log.v(TAG, "statusAfterConsent: $statusAfterConsent")

                        if ((statusBeforeConsent == ConsentInformation.ConsentStatus.NOT_REQUIRED) &&
                            (statusAfterConsent == ConsentInformation.ConsentStatus.NOT_REQUIRED)) {
                            Log.v(TAG, "user not in EU, consent not require, consentFormResult = $consentFormResult")

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(activity, "consent_not_required")

                        }else if ((statusBeforeConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (consentFormResult)){
                            Log.v(TAG, "user obtained OK consent, consentFormResult = $consentFormResult")

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(activity, "consent_obtained_OK")

                        }else if ((statusBeforeConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (!consentFormResult)){
                            Log.v(TAG, "user obtained NG consent, consentFormResult = $consentFormResult")

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(activity, "consent_obtained_NG")

                        }else if ((statusBeforeConsent == ConsentInformation.ConsentStatus.REQUIRED) &&
                            (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (consentFormResult)){
                            Log.v(TAG, "user new accept consent, consentFormResult = $consentFormResult")

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(activity, "consent_new_OK")

                        }else if ((statusBeforeConsent == ConsentInformation.ConsentStatus.REQUIRED) &&
                            (statusAfterConsent == ConsentInformation.ConsentStatus.OBTAINED) &&
                            (!consentFormResult)){
                            Log.v(TAG, "user new refuse consent, consentFormResult = $consentFormResult")

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(activity, "consent_new_NG")

                        }else{
                            Log.v(TAG, "user consent other case, consentFormResult = $consentFormResult")

                            //ump log
                            FirebaseLogEventUtils.logEventAdConsent(activity, "consent_other_case")

                        }
                    }
                }
            },
            { requestConsentError ->

                //request update error also call consentCallback
                onConsentGatheringCompleteListener.consentGatheringComplete(false)

                Log.v(TAG, "requestConsentError: ${requestConsentError.errorCode} ${requestConsentError.message}")

                //ump log
                FirebaseLogEventUtils.logEventAdConsent(activity, "consent_requestConsentError: "+requestConsentError.message)
            },
        )

    }


    //ANH comment out because using above function
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
//                        onConsentGatheringCompleteListener.consentGatheringComplete()
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

    //ANH comment out because using above function
//    fun gatherConsentDebug(
//        activity: Activity,
//        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener,
//        deviceHashId: String
//    ) {
//        val debugSettings =
//            ConsentDebugSettings.Builder(activity)
//                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//                .addTestDeviceHashedId(deviceHashId)
//                .build()
//
//        val params = ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()
//
//        consentInformation.requestConsentInfoUpdate(
//            activity,
//            params,
//            {
//                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
//                    if (formError != null) {
//                        Log.v(TAG, "formError: ${formError.errorCode} ${formError.message}")
//                    } else {
//                        onConsentGatheringCompleteListener.consentGatheringComplete()
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

    companion object {
        @Volatile
        private var instance: AdsConsentManager? = null

        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance ?: AdsConsentManager(context).also { instance = it }
                }
    }
}
