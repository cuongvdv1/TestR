package com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist

import android.util.Log
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
    private var isSelectionEnabled = true
    private val disabledItems = mutableSetOf<String>()
    fun setOnSelectionChangedListener(listener: (Int) -> Unit) {
        onSelectionChangedListener = listener
    }

    fun setSelectionEnabled(enabled: Boolean) {
        isSelectionEnabled = enabled
        if (!enabled) clearSelections() // Clear nếu disable
        notifyDataSetChanged()
    }

    fun setDisabledItems(disabledList: List<String>) {
        disabledItems.clear()
        disabledItems.addAll(disabledList)
        notifyDataSetChanged()
    }

    fun clearSelections() {
        selectedItems.clear()
        notifyDataSetChanged() // Cập nhật giao diện
        onSelectionChangedListener?.invoke(selectedItems.size)
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

    fun mergeSelectionsWithDisabled(disabledItems: List<String>): List<String> {
        val allSelectedItems = getSelectedItems().toMutableList()
        allSelectedItems.addAll(disabledItems.filterNot { allSelectedItems.contains(it) })
        return allSelectedItems
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemText = list[position]
        holder.binding.tvObject.text = itemText
        Log.d("TAG_MODEL", "disabledItems: $disabledItems")
        Log.d("TAG_MODEL", "itemText: $itemText")
        val isDisabled = disabledItems.any { disabledItem ->
            disabledItem.trim().lowercase() == itemText.trim().lowercase()
        }


        val isSelected = selectedItems.contains(position)

        if (isDisabled) {
            holder.binding.tvObject.alpha = 0.3f
            holder.binding.root.isEnabled = false
        } else {
            holder.binding.root.isEnabled = true
            holder.binding.tvObject.alpha = 1f
            holder.binding.root.setOnClickListener {

                if (isSelectionEnabled) {
                    if (disabledItems.size == 0) {
                        if (selectedItems.contains(position)) {
                            selectedItems.remove(position)
                        } else {
                            selectedItems.add(position)
                        }
                        Log.d("selectedItems", "${selectedItems.size}")
                        onSelectionChangedListener?.invoke(selectedItems.size)
                        notifyItemChanged(position)
                    }

                }

            }


        }


        if (isSelected) {
            holder.binding.tvObject.setBackgroundResource(R.drawable.bg_border_stroke_radius_16_seleceted)
        } else {
            holder.binding.tvObject.setBackgroundResource(R.drawable.bg_border_stroke_radius_16_unselceted)
        }
    }


    fun getSelectedItems(): List<String> {
        return selectedItems.map { list[it] }
    }
}
