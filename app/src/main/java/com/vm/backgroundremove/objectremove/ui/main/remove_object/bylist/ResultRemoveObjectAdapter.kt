package com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.databinding.ItemChooseObjectBinding

class ResultRemoveObjectAdapter(
    private val list: List<String>
) : RecyclerView.Adapter<ResultRemoveObjectAdapter.ViewHolder>() {
    private val selectedItems = mutableSetOf<Int>()

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

    }


    fun getSelectedItems(): List<String> {
        return selectedItems.map { list[it] }
    }
}
