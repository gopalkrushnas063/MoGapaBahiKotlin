package com.example.storybookapiintegration.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storybookapiintegration.R
import com.example.storybookapiintegration.data.model.Story
import com.example.storybookapiintegration.databinding.ItemCarouselStoryBinding

class LatestStoriesAdapter(
    private var stories: List<Story>,
    private val onItemClick: (Story) -> Unit
) : RecyclerView.Adapter<LatestStoriesAdapter.LatestStoryViewHolder>() {

    companion object {
        val PLACEHOLDER_IMAGE = R.drawable.placeholder_image
        private const val MAX_VALUE = Int.MAX_VALUE
    }

    val middlePosition: Int
        get() = if (stories.isEmpty()) 0 else (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2) % stories.size

    private fun getActualPosition(position: Int): Int {
        return if (stories.isEmpty()) 0 else position % stories.size
    }

    fun updateStories(newStories: List<Story>) {
        this.stories = newStories
        notifyDataSetChanged()
    }

    inner class LatestStoryViewHolder(val binding: ItemCarouselStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story) {
            with(binding) {
                if (story.image.isNullOrBlank()) {
                    storyImage.setImageResource(PLACEHOLDER_IMAGE)
                } else {
                    Glide.with(root.context)
                        .load(story.image)
                        .placeholder(PLACEHOLDER_IMAGE)
                        .error(PLACEHOLDER_IMAGE)
                        .into(storyImage)
                }

                storyTitle.text = story.title ?: root.context.getString(R.string.untitled_story)
                storyType.text = story.type ?: root.context.getString(R.string.unknown_category)

                root.setOnClickListener { onItemClick(story) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestStoryViewHolder {
        val binding = ItemCarouselStoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        // Set match_parent for both dimensions
        binding.root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        return LatestStoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LatestStoryViewHolder, position: Int) {
        if (stories.isNotEmpty()) {
            val actualPosition = getActualPosition(position)
            holder.bind(stories[actualPosition])
        }
    }

    override fun getItemCount(): Int {
        return if (stories.isEmpty()) 0 else Int.MAX_VALUE
    }
}