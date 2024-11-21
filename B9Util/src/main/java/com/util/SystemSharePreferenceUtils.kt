/** CUONG TUAN
 * lam truoc lib nay de dung shared preference khac voi remote config
  */

package com.util

import android.content.Context

object SystemSharePreferenceUtils {
    private const val PREF_NAME = "SystemSharePreference"
    private const val KEY_APP_OPEN_COUNT = "sys_app_open_count"
    private const val KEY_USER_LTV = "sys_user_ltv"

    fun getString(context: Context, key: String, defValue: String = ""): String {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getString(key, defValue) ?: defValue
    }

    fun saveString(context: Context, key: String, value: String) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pre.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getInt(context: Context, key: String, defValue: Int = 0): Int {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getInt(key, defValue)
    }

    fun saveInt(context: Context, key: String, value: Int) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pre.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getFloat(context: Context, key: String, defValue: Float = 0F): Float {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getFloat(key, defValue)
    }

    fun saveFloat(context: Context, key: String, value: Float) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pre.edit()
        editor.putFloat(key, value)
        editor.apply()
    }


    fun getBoolean(context: Context, key: String, defValue: Boolean = false): Boolean {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getBoolean(key, defValue)
    }

    fun saveBoolean(context: Context, key: String, value: Boolean) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pre.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    //custom function
    //app open count
    fun getAppOpenCount(context: Context): Int {
        return getInt(context, KEY_APP_OPEN_COUNT, 0)
    }

    fun setAppOpenCount(context: Context, count: Int) {
        saveInt(context, KEY_APP_OPEN_COUNT, count)
    }

    fun updateAppOpenCount(context: Context) {
        val count = getAppOpenCount(context)
        setAppOpenCount(context, count + 1)
    }

    //user LTV
    fun getUserLTV(context: Context): Float {
        return getFloat(context, KEY_USER_LTV, 0F)
    }

    fun setUserLTV(context: Context, ltv: Float) {
        saveFloat(context, KEY_USER_LTV, ltv)

    }
    fun updateUserLTV(context: Context, ltv: Float) {
        val count = getUserLTV(context)
        setUserLTV(context, count + ltv)
    }

    //log event


}
