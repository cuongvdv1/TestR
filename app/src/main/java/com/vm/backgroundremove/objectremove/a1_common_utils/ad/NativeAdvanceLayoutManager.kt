package com.vm.backgroundremove.objectremove.a1_common_utils.ad

import com.lib.admob.nativeAds.advance.base.NativeLayoutInlib

object NativeAdvanceLayoutManager {

    //get layout in lib
    fun getNativeLayoutInLib(key: String): NativeLayoutInlib?{
        return NativeLayoutInlib.entries.toTypedArray().find { it.key.equals(key)}
    }

    //get layout in app
    //custom layout
    fun getNativeLayoutInApp(key: String): NativeLayoutInapp?{
        return NativeLayoutInapp.entries.toTypedArray().find { it.key.equals(key)}
    }
}