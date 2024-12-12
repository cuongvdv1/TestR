package com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vm.backgroundremove.objectremove.ui.main.choose_photo_rmv_bg.model.ChoosePhotoModel
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.databinding.ItemChoosePhotoBinding
import com.vm.backgroundremove.objectremove.inteface.OnClickChoosePhoto
import com.vm.backgroundremove.objectremove.util.dpToPx

class ChoosePhotoAdapter(
    private val context: Context,
    private val listPhoto: List<ChoosePhotoModel>,
    private var listener: OnClickChoosePhoto

) : RecyclerView.Adapter<ChoosePhotoAdapter.ChoosePhotoHolder>() {

    private var selectedPosition = -1

    class ChoosePhotoHolder(val binding: ItemChoosePhotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePhotoHolder {
        val binding =
            ItemChoosePhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChoosePhotoHolder(binding)
    }

    override fun getItemCount(): Int {
        return listPhoto.size
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: ChoosePhotoHolder, @SuppressLint("RecyclerView") position: Int) {
        val imageInfo = listPhoto[position]
        Glide.with(context)
            .load(imageInfo.uri)
            .placeholder(R.drawable.img_camera)
            .into(holder.binding.ivChoosePhoto)

        if (selectedPosition == position && position != 0) {
            holder.binding.bgItemSelected.visibility = View.VISIBLE
            holder.binding.icPhotoSelected.visibility = View.VISIBLE
            holder.itemView.setBackgroundResource(R.drawable.bg_border_8_item_photo)
            holder.itemView.setPadding(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
        } else {
            holder.binding.bgItemSelected.visibility = View.GONE
            holder.binding.icPhotoSelected.visibility = View.GONE
            holder.itemView.background = null
            holder.itemView.setPadding(0, 0, 0, 0)
        }

        holder.itemView.setOnClickListener {
            if (position == 0){
                listener.onClickItemCamera()
            }
            else{
                listener.onClickItemPhoto(imageInfo)
            }
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)

        }
    }
}
