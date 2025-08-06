// LatestStoriesAdapter.kt
package com.example.storybookapiintegration.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storybookapiintegration.R
import com.example.storybookapiintegration.data.model.Story
import com.example.storybookapiintegration.databinding.ItemCarouselStoryBinding

class LatestStoriesAdapter(
    private var stories: List<Story>, // Change from val to var
    private val onItemClick: (Story) -> Unit
) : RecyclerView.Adapter<LatestStoriesAdapter.LatestStoryViewHolder>() {

    companion object {
         val PLACEHOLDER_IMAGE = R.drawable.placeholder_image
    }

    // Add this update function
    fun updateStories(newStories: List<Story>) {
        this.stories = newStories
        notifyDataSetChanged()
    }

    inner class LatestStoryViewHolder(val binding: ItemCarouselStoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestStoryViewHolder {
        val binding = ItemCarouselStoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LatestStoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LatestStoryViewHolder, position: Int) {
        val story = stories[position]
        with(holder.binding) {
            // Handle null/empty image URL
            if (story.image.isNullOrBlank()) {
                storyImage.setImageResource(PLACEHOLDER_IMAGE)
            } else {
                Glide.with(root.context)
                    .load(story.image)
                    .placeholder(PLACEHOLDER_IMAGE)
                    .error(PLACEHOLDER_IMAGE)
                    .into(storyImage)
            }

            storyTitle.text = story.title
            storyType.text = story.type ?: "Unknown"

            root.setOnClickListener { onItemClick(story) }
        }
    }

    override fun getItemCount() = stories.size
}