package com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vm.backgroundremove.objectremove.R

class BackGroundAdapter(
    private var context: Context,
    private var backGroundList: List<Int>

): RecyclerView.Adapter<BackGroundAdapter.BackGroundViewHolder>() {
    class BackGroundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_item_background)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackGroundViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_background, parent, false)
        return BackGroundViewHolder(view)
    }

    override fun getItemCount(): Int {
      return  backGroundList.size
    }

    override fun onBindViewHolder(holder: BackGroundViewHolder, position: Int) {
        val background = backGroundList[position]
        Glide.with(holder.imageView.context).load(background).into(holder.imageView)
//        holder.imageView.setBackgroundResource(background)
    }
    interface backGroundSelect {
        fun backGroundSelected(position: Int)
    }

}