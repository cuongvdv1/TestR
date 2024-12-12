package com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.databinding.ItemChooseObjectBinding

class RemoveObjectAdapter(
    private val list: List<String>
) : RecyclerView.Adapter<RemoveObjectAdapter.ViewHolder>() {
    private val selectedItems = mutableSetOf<Int>()
    private var onSelectionChangedListener: ((Int) -> Unit)? = null

    fun setOnSelectionChangedListener(listener: (Int) -> Unit) {
        onSelectionChangedListener = listener
    }
    inner class ViewHolder(val binding: ItemChooseObjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChooseObjectBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvObject.text = list[position]
        if (selectedItems.contains(position)) {
            holder.binding.tvObject.setBackgroundResource(R.drawable.bg_border_stroke_radius_16_seleceted)
        } else {
            holder.binding.tvObject.setBackgroundResource(R.drawable.bg_border_stroke_radius_16_unselceted)
        }
        holder.binding.root.setOnClickListener {
            if (selectedItems.contains(position)) {
                selectedItems.remove(position)
            } else {
                selectedItems.add(position)
            }
            onSelectionChangedListener?.invoke(selectedItems.size)
            notifyItemChanged(position)
        }
    }


    fun getSelectedItems(): List<String> {
        return selectedItems.map { list[it] }
    }
}
