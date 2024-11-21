/**
 * 20290929 chek OK
 */

package com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.ads

class RemoteConfigAdSplashModel(

    var ad_name: String = "ad_splash",

    //SpalashAdManager.ad_id_inter
    //set in constructor
    var ad_id_appopen: String = "",

    //SpalashAdManager.ad_id_inter
    //set in constructor
    var ad_id_inter: String = "",

    //implemted trong SpalashAdManager
    //set in constructor
    var is_show_app_open:Boolean = true,

    //implemted trong SpalashAdManager
    //set in constructor
    var is_show_inter:Boolean = true,

    //implemted trong SpalashAdManager.AdMaxLoadingTime
    //set in constructor
    var max_loading_time: Long = 15,

    //implemted trong SpalashAdManager.rate
    //set in constructor
    //1-30: app_opem; rest: inter
    var appopen_rate: Int = 30,

) {
    fun getMaxLoadingTime(): Long {
        return max_loading_time * 1000
    }
}