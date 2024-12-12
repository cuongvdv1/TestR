package com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.ui.main.remove_background.model.ColorModel

interface ColorSelectorListener {
    fun onColorClicked(position: Int, color: String)
}

class ColorAdapter(
    private var context: Context,
    private val colorList: List<ColorModel>,
    private var actionListener: ColorSelectorListener?
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {


    fun setActionListener(actionListener: ColorSelectorListener) {
        this.actionListener = actionListener
    }

    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgColor: View = itemView.findViewById(R.id.iv_color)
        val imgSelected: View = itemView.findViewById(R.id.iv_color_selected)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_color, parent, false)
        return ColorViewHolder(view)
    }

    override fun getItemCount(): Int {
        return colorList.size
    }
    fun selectItem(position: Int) {
        colorList.forEach { it.isSelected = false }
        colorList[position].isSelected = true
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colorList[position]
        holder.imgColor.setBackgroundResource(color.img)
        if (color.isSelected) {
            holder.imgSelected.visibility = View.VISIBLE
            holder.imgColor.visibility = View.VISIBLE
        } else {
            holder.imgSelected.visibility = View.INVISIBLE
            holder.imgColor.visibility = View.VISIBLE
        }
        holder.itemView.setOnClickListener {
            selectItem(position)
            actionListener?.onColorClicked(position, color.color)
        }
    }
}