package com.example.storybookapiintegration.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storybookapiintegration.data.model.Story
import com.example.storybookapiintegration.databinding.ItemStoryBinding

class StoryAdapter(
    private val stories: List<Story>,
    private val onItemClick: (Story) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    inner class StoryViewHolder(val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]
        holder.binding.storyTitle.text = story.title
        holder.binding.storyType.text = story.type
        Glide.with(holder.itemView.context)
            .load(story.image)
            .into(holder.binding.storyImage)

        holder.itemView.setOnClickListener {
            onItemClick(story)
        }
    }

    override fun getItemCount() = stories.size
}