package com.vm.backgroundremove.objectremove.ui.main.edit

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.databinding.ItemChoosePhotoEditBinding

class ChoosePhotoEditAdapter(
    private val context: Context,
    private val listPhoto: List<String>
): RecyclerView.Adapter<ChoosePhotoEditAdapter.ChoosePhotoEditHolder>() {
    class ChoosePhotoEditHolder(val binding: ItemChoosePhotoEditBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePhotoEditHolder {
        val binding = ItemChoosePhotoEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChoosePhotoEditHolder(binding)
    }

    override fun getItemCount(): Int {
        return listPhoto.size
    }

    override fun onBindViewHolder(holder: ChoosePhotoEditHolder, position: Int) {
    }
}