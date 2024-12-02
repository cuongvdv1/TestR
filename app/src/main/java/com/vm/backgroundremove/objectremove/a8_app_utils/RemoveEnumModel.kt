package com.vm.backgroundremove.objectremove.a8_app_utils

import com.google.gson.Gson


enum class GenerateState {
    GENERATE,
    RESET,
    RETRY;
    fun convertToString() = Gson().toJson(this)
}

enum class ProcessState {
    SUCCESS,
    FAILED,
    PROCESSING,
    NEW
}
