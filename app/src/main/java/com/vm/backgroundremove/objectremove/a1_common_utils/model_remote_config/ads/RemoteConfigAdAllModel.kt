package com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.ads

class RemoteConfigAdAllModel(

    //pending
    var ad_context: String = "default",

    //pending
    var mediation_network: String = "admob",

    //pending
    var is_show: Boolean = true,

    //remote config InterManager.interInterval
    var inter_interval: Long = 15,

    //remote config InterManager.interStartInterval
    var inter_start_interval: Long = 5,
) {

    fun getInterInterval(): Long {
        return inter_interval * 1000
    }

    fun getInterStartInterval(): Long {
        return inter_start_interval * 1000
    }
}