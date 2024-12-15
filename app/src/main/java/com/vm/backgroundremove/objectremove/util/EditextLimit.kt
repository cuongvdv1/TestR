package com.vm.backgroundremove.objectremove.util

import android.text.InputFilter
import android.text.Spanned

class LineBreakInputFilter(private val maxCharsPerLine: Int) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val currentText = dest.toString()
        val newText = currentText.substring(0, dstart) + source?.subSequence(start, end) + currentText.substring(dend)

        val lines = newText.split("\n")
        for (line in lines) {
            if (line.length > maxCharsPerLine) {
                return ""
            }
        }
        return null
    }
}

