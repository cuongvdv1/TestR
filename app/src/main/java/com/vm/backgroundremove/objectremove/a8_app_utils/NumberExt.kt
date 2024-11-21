package com.vm.backgroundremove.objectremove.a8_app_utils

import android.content.res.Resources
import android.util.TypedValue

fun Float.toDp(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )
}