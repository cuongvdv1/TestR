package com.vm.backgroundremove.objectremove.a8_app_utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import com.vm.backgroundremove.objectremove.ui.common.intro.IntroActivity
import com.vm.backgroundremove.objectremove.ui.common.language.LanguageStartActivity
import com.vm.backgroundremove.objectremove.ui.common.update.AppUpdateActivity
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import com.vm.backgroundremove.objectremove.ui.main.user_perpose.UserPerposeActivity
import java.util.Locale

object SystemUtil {
    private var myLocale: Locale? = null

    // Lưu ngôn ngữ đã cài đặt
    fun saveLocale(context: Context, lang: String?) {
        setPreLanguage(context, lang)
    }

    // Load lại ngôn ngữ đã lưu và thay đổi chúng
    fun setLocale(context: Context) {
        val language = getPreLanguage(context)
        if (language == "") {
            val config = Configuration()
            val locale = Locale.getDefault()
            Locale.setDefault(locale)
            config.locale = locale
            context.resources
                .updateConfiguration(config, context.resources.displayMetrics)
        } else {
            changeLang(language, context)
        }
    }

    // method phục vụ cho việc thay đổi ngôn ngữ.
    fun changeLang(lang: String?, context: Context) {
        if (lang.equals("", ignoreCase = true)) return
        myLocale = Locale(lang)
        saveLocale(context, lang)
        Locale.setDefault(myLocale)
        val config = Configuration()
        config.locale = myLocale
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getPreLanguage(mContext: Context): String? {
        val preferences = mContext.getSharedPreferences("data", Context.MODE_PRIVATE)
        return preferences.getString("KEY_LANGUAGE", "")
    }

    fun setPreLanguage(context: Context, language: String?) {
        if (language == null || language == "") {
        } else {
            val preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString("KEY_LANGUAGE", language)
            editor.apply()
        }
    }

    fun setActive(context: Context, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("active", value)
        editor.apply()
    }

    fun getActive(context: Context, value: Boolean): Boolean {
        val sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("active", value)
    }

    fun openAppInPlayStore(context: Context) {
        val appPackageName = context.packageName
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // context là Activity hoặc Dialog
            if (context is Activity) {
                context.startActivity(intent)
            } else if (context is Dialog) {
                context.context.startActivity(intent)
            } else {
                context.startActivity(intent)
            }
        } catch (e: android.content.ActivityNotFoundException) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (context is Activity) {
                context.startActivity(intent)
            } else if (context is Dialog) {
                context.context.startActivity(intent)
            } else {
                context.startActivity(intent)
            }
        }
    }

    /**
     * return next activity in common screen
     * config next screen in firebase remote config
     */

    fun getActivityClass(name: String?, defaultClass: Class<*>): Class<*>? {

        if (name.equals("language"))
            return LanguageStartActivity::class.java
        if (name.equals( "intro"))
            return IntroActivity::class.java
        if (name.equals("app_update"))
            return AppUpdateActivity::class.java
        //home activity- old version - using fragment
        if (name.equals("home"))
            return HomeActivity::class.java
        if (name.equals("user_purpose"))
            return UserPerposeActivity::class.java

        return defaultClass
    }

}
