package com.vm.backgroundremove.objectremove.ui.main.your_projects.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.HOUR_FORMAT
import com.vm.backgroundremove.objectremove.a8_app_utils.convertTime
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ItemProjectBinding
import java.util.concurrent.Executors

class ProjectAdapter : ListAdapter<HistoryModel, ProjectAdapter.ProjectViewHolder>(
    AsyncDifferConfig.Builder(diffCallback)
        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
        .build()
) {
    private var onViewMoreClick: (HistoryModel) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProjectViewHolder(ItemProjectBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    fun setOnViewMoreClick(onViewMoreClick: (HistoryModel) -> Unit) {
        this.onViewMoreClick = onViewMoreClick
    }


    inner class ProjectViewHolder(private val binding: ItemProjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
            private var data: HistoryModel? = null

        init {
            binding.root.tap {
                data?.let(onViewMoreClick)
            }
        }

        fun bindData(data: HistoryModel) {
            this.data = data
            binding.tvItemName.text = data.name
            binding.tvTimeProcess.text = data.time.convertTime(HOUR_FORMAT)
            binding.tvDayProcess.text = data.time.convertTime()
            binding.progress.progress = data.process
            binding.tvProgress.text = "${data.process}%"

            if (data.isSuccess()) {
                Glide.with(binding.root.context).load(data.imageResult)
                    .placeholder(R.drawable.img_project_empty).into(binding.imgProcess)
            }
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<HistoryModel>() {
            override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean {
                return oldItem == newItem
            }

        }
    }
}