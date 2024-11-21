/**
 * 20241001 check OK
 */

package com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.ads

data class RemoteConfigAdAppopenResumeModel(

    //not using
    val ad_name: String,

    //properties ad_id in manager
    //using setId in setting function in app
    val ad_id: String,

    //properties is_show in manager
    //using setIsShow in setting function in app
    val is_show: Boolean,
)
