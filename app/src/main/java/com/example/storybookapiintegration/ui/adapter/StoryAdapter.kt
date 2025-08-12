package com.example.storybookapiintegration.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.storybookapiintegration.R
import com.example.storybookapiintegration.data.model.Story
import com.example.storybookapiintegration.databinding.ItemStoryBinding

class StoryAdapter(
    private var stories: List<Story>,
    private val onItemClick: (Story) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    companion object {
        val PLACEHOLDER_IMAGE = R.drawable.placeholder_image
        private const val CORNER_RADIUS = 16 // dp
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story) {
            val context = binding.root.context

            // Handle image
            if (story.image.isNullOrBlank()) {
                binding.storyImage.setImageResource(PLACEHOLDER_IMAGE)
            } else {
                Glide.with(context)
                    .load(story.image)
                    .placeholder(PLACEHOLDER_IMAGE)
                    .error(PLACEHOLDER_IMAGE)
                    .transform(RoundedCorners(dpToPx(CORNER_RADIUS, context)))
                    .into(binding.storyImage)
            }

            // Handle icon
            if (story.icon.isNullOrBlank()) {
                binding.storyIcon.setImageResource(PLACEHOLDER_IMAGE)
            } else {
                Glide.with(context)
                    .load(story.icon)
                    .placeholder(PLACEHOLDER_IMAGE)
                    .error(PLACEHOLDER_IMAGE)
                    .transform(RoundedCorners(dpToPx(CORNER_RADIUS, context)))
                    .into(binding.storyIcon)
            }

            binding.storyTitle.text = story.title ?: context.getString(R.string.untitled_story)
            binding.storyType.text = story.type ?: context.getString(R.string.unknown_category)

            // Use binding.root instead of itemView
            binding.root.setOnClickListener { onItemClick(story) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        // Set fixed width for grid items
        val displayMetrics = parent.context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val spacing = parent.context.resources.getDimensionPixelSize(R.dimen.grid_spacing)
        val columnWidth = (screenWidth - (3 * spacing)) / 2 // 3 spacings: left, middle, right

        binding.root.layoutParams.width = columnWidth
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount() = stories.size

    fun updateStories(newStories: List<Story>) {
        this.stories = newStories
        notifyDataSetChanged()
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}