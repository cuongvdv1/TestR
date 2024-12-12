package com.vm.backgroundremove.objectremove.a8_app_utils

import android.content.Context
import android.util.AttributeSet
import com.makeramen.roundedimageview.RoundedImageView

class SquareImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RoundedImageView(context, attrs, defStyle) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }
}