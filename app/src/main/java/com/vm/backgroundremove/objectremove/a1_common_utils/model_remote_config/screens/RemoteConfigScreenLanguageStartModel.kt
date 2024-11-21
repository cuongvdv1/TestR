package com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens

data class RemoteConfigScreenLanguageStartModel (
    //fix properties
    val screen_name: String,
    val is_show_012: Int,
    val is_hide_navi_menu: Boolean,
    val layout_name: String,
    val next_screen_default: String,

    //custom properties
    val ad_placement:String,
    val ad_type:String,

    //language adjust position
    val lang_c_pos:Int,
    val lang_p_pos:Int
)