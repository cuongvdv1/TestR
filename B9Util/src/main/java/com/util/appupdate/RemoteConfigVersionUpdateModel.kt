package com.util.appupdate

//default no_update
data class RemoteConfigVersionUpdateModel (
    val lastest_version: String,
    val lastest_update_content: String,
    val must_update: List<String>,
    val should_update: List<String>,
    val no_update: List<String>
)