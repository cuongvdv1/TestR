/**
 * 20240924 check OK
 */

package com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.ads

data class RemoteConfigAdInterModel(
    val ad_name: String,

    val ad_id: String,

    //da implememted trong InterManager - properties InterSingle.isShow and AdCommon
    val is_show: Boolean,

    //implemented properties InterSingle.isShow.isPreloadAfterShowAd
    //tu dong reload sau khi show
    //cac man hinh dung 1 lan nhu intro thi de false
    //cac man hinh co the back lai thi de true
    var is_preload_after_show_ad:Boolean,

    //implemented properties InterSingle.isShowNextActivityBeforeAd
    //neu la true thi goi man B, roi show ad
    //neu la false thi show ad, click close ad-> hien thi man B
    var is_show_next_activity_before_ad: Boolean

)
