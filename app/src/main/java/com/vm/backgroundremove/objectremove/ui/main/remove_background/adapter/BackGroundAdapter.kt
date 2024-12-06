package com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vm.backgroundremove.objectremove.R

interface BackGroundSelectorListener {
    fun onBackGroundClicked(position: Int)
}

class BackGroundAdapter(
    private var context: Context,
    private var backGroundList: List<Int>,
    private var actionListener: BackGroundSelectorListener? = null

): RecyclerView.Adapter<BackGroundAdapter.BackGroundViewHolder>() {
    inner class BackGroundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv_backGround: ImageView = itemView.findViewById(R.id.iv_item_background)
    }
    fun setActionListener(actionListener:BackGroundSelectorListener) {
        this.actionListener = actionListener
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
        Log.d("TAG_DEBUG", "Binding position: $position, Background: $background, ActionListener: $actionListener, Context: $context")
        Glide.with(holder.iv_backGround.context).load(background).into(holder.iv_backGround)
        holder.iv_backGround.setOnClickListener {
            if (context != null && actionListener != null) {
//                val bitmap = BitmapFactory.decodeResource(context.resources, background)
                actionListener?.onBackGroundClicked(position)
            } else {
                Log.e("BackGroundAdapter", "Context or ActionListener is null!")
            }
        }
    }

}