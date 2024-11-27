package com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens

data class RemoteConfigScreenIntroModel (
    //fix properties
    val screen_name: String,
    val is_show_012: Int,
    val is_hide_navi_menu: Boolean,
    val layout_name: String,
    val next_screen_default: String,

    //Anh 20241117 add
    val is_reload_ad_when_change_slide: Boolean,

    //custom properties
    val is_slide1_show: Boolean,
    val is_slide1_ad_show: Boolean,

    val is_slide2_show: Boolean,
    val is_slide2_ad_show: Boolean,

    val is_slide3_show: Boolean,
    val is_slide3_ad_show: Boolean,

    val is_slide4_show: Boolean,
    val is_slide4_ad_show: Boolean,

    //adjust layout
    val distance_button_next_vs_ad: Int,
    val font_size: Float,

    //native fullscreen pisition
    val ad_native_full_pos1: Int,
    val ad_native_full_pos2: Int,
)