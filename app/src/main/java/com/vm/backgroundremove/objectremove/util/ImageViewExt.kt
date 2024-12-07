package com.vm.backgroundremove.objectremove.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

fun getBitmapFrom(context: Context, url: String?, onLoaded: (Bitmap) -> Unit) {
    Glide.with(context)
        .asBitmap()
        .load(url)
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                Log.v("tag111", "bitmap ressult: $resource")
                onLoaded(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                Log.v("tag111", "bitmap fail")
            }


        })
}