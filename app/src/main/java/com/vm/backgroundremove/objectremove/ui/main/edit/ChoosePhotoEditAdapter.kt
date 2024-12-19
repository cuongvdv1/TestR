package com.vm.backgroundremove.objectremove.ui.main.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vm.backgroundremove.objectremove.databinding.ItemChoosePhotoEditBinding

class ChoosePhotoEditAdapter(
    private val context: Context,
    private val listPhoto: List<String>
) : RecyclerView.Adapter<ChoosePhotoEditAdapter.ChoosePhotoEditHolder>() {
    class ChoosePhotoEditHolder(val binding: ItemChoosePhotoEditBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePhotoEditHolder {
        val binding =
            ItemChoosePhotoEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChoosePhotoEditHolder(binding)
    }

    override fun getItemCount(): Int {
        return listPhoto.size
    }

    override fun onBindViewHolder(holder: ChoosePhotoEditHolder, position: Int) {
        val imageUrl = listPhoto[position]
        Glide.with(context).asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    @NonNull resource: Bitmap,
                    @Nullable transition: Transition<in Bitmap>?
                ) {
                    holder.binding.ivChoosePhoto.setBitmap(resource)
                }
                override fun onLoadCleared(@Nullable placeholder: Drawable?) {
                }
            })
    }
}