package com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.R

interface ColorSelectorListener {
    fun onColorClicked(position: Int)
}
class ColorAdapter(
    private var context: Context,
    private val colorList: List<Int>,
    private var actionListener: ColorSelectorListener? = null
) :RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {



    fun setActionListener(actionListener: ColorSelectorListener) {
        this.actionListener = actionListener
    }

    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgColor: View = itemView.findViewById(R.id.iv_item_color)
        val imgSelected: View = itemView.findViewById(R.id.iv_selected_color)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_color, parent, false)
        return ColorViewHolder(view)
    }

    override fun getItemCount(): Int {
        return colorList.size
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colorList[position]
        holder.imgColor.setBackgroundResource(color)
        holder.imgColor.setOnClickListener {
            actionListener?.onColorClicked(position)
        }
    }
}