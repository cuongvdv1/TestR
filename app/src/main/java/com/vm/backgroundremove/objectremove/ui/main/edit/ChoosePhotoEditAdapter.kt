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
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ItemChoosePhotoEditBinding

class ChoosePhotoEditAdapter(
    private val context: Context,
    private val onItemClick: ((HistoryModel) -> Unit)? = null
) : RecyclerView.Adapter<ChoosePhotoEditAdapter.ChoosePhotoEditHolder>() {
    class ChoosePhotoEditHolder(val binding: ItemChoosePhotoEditBinding) :
        RecyclerView.ViewHolder(binding.root)

    val listPhoto= mutableListOf<HistoryModel>()

    fun setData(list: List<HistoryModel>) {
        listPhoto.clear()
        listPhoto.addAll(list)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePhotoEditHolder {
        val binding = ItemChoosePhotoEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChoosePhotoEditHolder(binding)
    }

    override fun getItemCount(): Int {
        return listPhoto.size
    }

    override fun onBindViewHolder(holder: ChoosePhotoEditHolder, position: Int) {
        val imageUrl = listPhoto[position].imageResult
        Glide.with(context)
            .load(imageUrl)
            .into(holder.binding.ivChoosePhoto)

        holder.binding.root.tap {
            onItemClick?.invoke(listPhoto[position])
        }
        if(listPhoto[position].isSelected){
            holder.binding.root.setBackgroundResource(R.drawable.bg_border_stroke_gradient_16)
        }else{
            holder.binding.root.background = null
        }
    }
}