package com.lib.admob

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.lib.admob.resumeAds.AppOpenResumeManager
import com.lib.admob.resumeAds.callback.OpenCallback


abstract class B9AdApplication: Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private val TAG = "B9AdApplication"

    var currentActivity: Activity? = null

    open fun getCallback(): OpenCallback{
        return object : OpenCallback {
            override fun onNextAction() {
                //Toast.makeText(currentActivity, "on next action", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate() {
        super<Application>.onCreate()
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        //facebook sdk init here
        //adjust init here
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.v(TAG, "start ads $currentActivity")
        currentActivity?.let {
            AppOpenResumeManager.showAdResume(it, getCallback())
        }

    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }


}
