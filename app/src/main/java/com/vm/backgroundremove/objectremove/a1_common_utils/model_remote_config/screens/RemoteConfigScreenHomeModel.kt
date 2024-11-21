package com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens

data class RemoteConfigScreenHomeModel (
    //fix properties
    val screen_name: String,
    val is_hide_navi_menu: Boolean,

    //custom properties
    val ad_placement:String,
    val ad_type:String,

)