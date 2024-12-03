package com.vm.backgroundremove.objectremove.a8_app_utils

import android.content.Intent
import android.os.Build
import androidx.core.os.BundleCompat

inline fun <reified T> Intent.getParcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        extras?.let { BundleCompat.getParcelable(it, key, T::class.java) }
    } else {
        getParcelableExtra(key)
    }
}
