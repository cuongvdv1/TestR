//list all remote config here
//include default and custom add remove config

package com.vm.backgroundremove.objectremove.a1_common_utils

object RemoteConfigKey {
    //app
    const val app_config = "app_config"
    const val version_update = "version_update"

    //ad all
    const val ad_config_all = "ad_config_all"

    //ad spash
    const val ad_splash = "ad_splash"

    //ad resume
    const val ad_appopen_resume = "ad_appopen_resume"

    //ad banner
    const val ad_banner_splash = "ad_banner_splash"
    const val ad_banner_adaptive_all = "ad_banner_adaptive_all"
    const val ad_banner_collapsible_all = "ad_banner_collapsible_all"
    const val ad_banner_collapsible_home = "ad_banner_collapsible_home"



    //ad inter
    const val ad_inter_intro = "ad_inter_intro"
    const val ad_inter_customize = "ad_inter_customize"

    //ad native
    const val ad_native_language_top = "ad_native_language_top"
    const val ad_native_language_bottom = "ad_native_language_bottom"
    const val ad_native_intro = "ad_native_intro"
    const val ad_native_fullscreen_intro = "ad_native_fullscreen_intro"
    const val ad_native_welcomeback = "ad_native_welcomeback"
    const val ad_native_app_update  = "ad_native_app_update"

    const val ad_native_home_app_setting = "ad_native_home_app_setting"
    const val ad_native_home_bot_setting = "ad_native_home_bot_setting"
    const val ad_native_home_history = "ad_native_home_history"
    const val ad_native_exit = "ad_native_exit"
    const val ad_native_feedback = "ad_native_feedback"

    //screen config
    const val screen_splash = "screen_splash"
    const val screen_language_start = "screen_language_start"
    const val screen_intro = "screen_intro"
    const val screen_app_update = "screen_app_update"
    const val screen_welcomeback = "screen_welcomeback"
    const val screen_feedback = "screen_feedback"
    const val screen_home = "screen_home"
    const val screen_customization = "screen_customization"

    //custom remote config here


    //pending update lib here
    fun getRemoteConfigStringKeyList(): MutableList<String> {
        return mutableListOf(

            //app
            app_config,
            version_update,

            //ad common
            ad_config_all,
            ad_splash,
            ad_appopen_resume,

            //banner
            ad_banner_splash,
            ad_banner_adaptive_all,
            ad_banner_collapsible_all,
            ad_banner_collapsible_home,

            //inter
            ad_inter_intro,
            ad_inter_customize,

            //native
            ad_native_language_top,
            ad_native_language_bottom,
            ad_native_intro,
            ad_native_fullscreen_intro,
            ad_native_welcomeback,
            ad_native_app_update,

            ad_native_home_app_setting,
            ad_native_home_bot_setting,
            ad_native_home_history,
            ad_native_exit,
            ad_native_feedback,

            //screen
            screen_splash,
            screen_language_start,
            screen_intro,
            screen_app_update,
            screen_welcomeback,
            screen_feedback,
            screen_home,
            screen_customization,
        )
    }

    fun getRemoteConfigBooleanKeyList(): MutableList<String> {
        return mutableListOf()
    }

    fun getRemoteConfigLongKeyList(): MutableList<String> {
        return mutableListOf()
    }

}