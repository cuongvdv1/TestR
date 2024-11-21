/**
 * 20240929 can xu ly them is_auto_reload_timer va ad_reload_timer_interval
 */

package com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.ads

data class RemoteConfigAdBannerModel(

    //not properties in manager
    val ad_name: String,

    //properties in bannerManager
    val ad_id: String,

    //not properties in manager,
    //using adCommon class or in app
    val is_show: Boolean,

    //not properties in manager,
    //ANH dang nghien cuu reload timer cho banner
    val is_auto_reload_timer: Boolean,

    //not properties in manager
    //ANH dang nghien cuu reload timer cho banner
    val ad_reload_timer_interval: Long,

)
