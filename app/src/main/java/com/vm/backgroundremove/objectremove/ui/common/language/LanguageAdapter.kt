package com.vm.backgroundremove.objectremove.ui.common.language

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a8_app_utils.SystemUtil

class LanguageAdapter(
    private val context: Context,
    private var list: List<LanguageModel>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<LanguageAdapter.LanguageHolder>() {

    var selectedItemPosition: Int = -1
    private var previousSelectedItemPosition: Int = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LanguageHolder {
        return LanguageHolder(
            LayoutInflater.from(context).inflate(R.layout.item_language, parent, false)
        )
    }


    fun updateData(newList: List<LanguageModel>) {
        list = newList
        for (language in list) {
            Log.d("LanguageAdapter", "${language.code} , ${language.active}")
        }
        notifyDataSetChanged()

    }

    fun setSelectedLanguage(selectedLanguage: LanguageModel) {
        for (data in list) {
            data.active = data == selectedLanguage
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: LanguageHolder, position: Int) {
        val data = list[position]
        holder.ivLanguage.setImageResource(data.image)
        holder.tvLanguage.setText(data.languageName)
        if (position == selectedItemPosition) {
            holder.itemView.setBackgroundResource(R.drawable.bg_item_language_on)
            holder.tvLanguage.setTextColor(Color.WHITE)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_item_language_off)
            holder.tvLanguage.setTextColor(Color.BLACK)
        }

        holder.itemView.setOnClickListener {
            // Lưu vị trí item trước đó
            previousSelectedItemPosition = selectedItemPosition
            // Cập nhật vị trí item mới
            selectedItemPosition = holder.adapterPosition
            // Thông báo thay đổi cho item trước đó và item mới
            notifyItemChanged(previousSelectedItemPosition)
            notifyItemChanged(selectedItemPosition)
            itemClickListener.onItemClick(position)
            SystemUtil.setLocale(context)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class LanguageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivLanguage: ImageView = itemView.findViewById(R.id.iv_language)
        val tvLanguage: TextView = itemView.findViewById(R.id.tv_language)
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}