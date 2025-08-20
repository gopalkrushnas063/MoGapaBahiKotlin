package com.example.storybookapiintegration.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storybookapiintegration.R
import com.example.storybookapiintegration.data.model.Story
import com.example.storybookapiintegration.databinding.ActivityStoryDetailsBinding

class StoryDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        displayStory()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Set back button click listener
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun displayStory() {
        val story = intent.getParcelableExtra<Story>("STORY_DATA")
        story?.let { renderStory(it) }
    }

    private fun renderStory(story: Story) {
        binding.apply {
            // Handle main image
            if (story.image.isNullOrBlank()) {
                storyImage.setImageResource(R.drawable.placeholder_image)
            } else {
                Glide.with(this@StoryDetailsActivity)
                    .load(story.image)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(storyImage)
            }

            // Handle icon
            if (story.icon.isNullOrBlank()) {
                storyIcon.setImageResource(R.drawable.placeholder_image)
            } else {
                Glide.with(this@StoryDetailsActivity)
                    .load(story.icon)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(storyIcon)
            }

            storyTitle.text = story.title
            storyType.text = story.type ?: "Unknown"
            storyContent.text = story.content

            // Set toolbar title to story title
            binding.toolbar.title = story.title
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}