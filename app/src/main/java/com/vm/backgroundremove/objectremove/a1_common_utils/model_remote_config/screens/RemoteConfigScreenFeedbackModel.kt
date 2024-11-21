package com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens

data class RemoteConfigScreenFeedbackModel (
    //
    val screen_name: String,

    //gmail: direct go gmail
    //bottom_sheet: open bottom sheet
    val type: String,

    //default email in feedback
    val fb_email: String,

    //tittle in email
    val fb_tittle: String,

    //custom properties
)