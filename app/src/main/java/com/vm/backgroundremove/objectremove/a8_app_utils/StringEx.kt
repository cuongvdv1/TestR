package com.vm.backgroundremove.objectremove.a8_app_utils

import com.google.gson.Gson

inline fun <reified T> String.convertToObject(): T = Gson().fromJson(this, T::class.java)
