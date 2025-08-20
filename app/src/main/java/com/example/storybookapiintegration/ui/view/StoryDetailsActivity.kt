package com.example.storybookapiintegration.ui.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.storybookapiintegration.R
import com.example.storybookapiintegration.data.local.AppDatabase
import com.example.storybookapiintegration.data.model.Bookmark
import com.example.storybookapiintegration.data.model.Story
import com.example.storybookapiintegration.data.repository.BookmarkRepository
import com.example.storybookapiintegration.databinding.ActivityStoryDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class StoryDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryDetailsBinding
    private lateinit var bookmarkRepository: BookmarkRepository
    private var currentStory: Story? = null
    private var isBookmarked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database
        val database = AppDatabase.getDatabase(this)
        bookmarkRepository = BookmarkRepository(database.bookmarkDao())

        setupToolbar()
        displayStory()
        checkBookmarkStatus()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun displayStory() {
        currentStory = intent.getParcelableExtra<Story>("STORY_DATA")
        currentStory?.let { renderStory(it) }
    }

    // update the checkBookmarkStatus method
    private fun checkBookmarkStatus() {
        currentStory?.let { story ->
            lifecycleScope.launch {
                isBookmarked = bookmarkRepository.isBookmarked(story.id)
                invalidateOptionsMenu()
            }
        }
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

            binding.toolbar.title = story.title
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_story_details, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val bookmarkItem = menu.findItem(R.id.action_bookmark)
        if (isBookmarked) {
            bookmarkItem.setIcon(R.drawable.ic_bookmark_filled)
            bookmarkItem.title = "Remove Bookmark"
        } else {
            bookmarkItem.setIcon(R.drawable.ic_bookmark_outline)
            bookmarkItem.title = "Bookmark"
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareStory()
                true
            }
            R.id.action_bookmark -> {
                toggleBookmark()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareStory() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val story = currentStory ?: return@launch

                // Create share content
                val shareText = buildString {
                    append("${story.title}\n\n")
                    append("Category: ${story.type ?: "Unknown"}\n\n")
                    append(story.content)
                    if (!story.image.isNullOrBlank()) {
                        append("\n\nImage: ${story.image}")
                    }
                }

                // Capture screenshot for sharing
                val screenshotUri = captureScreenshot()

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)

                    // Add image if available
                    screenshotUri?.let {
                        putExtra(Intent.EXTRA_STREAM, it)
                        type = "image/*"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }

                withContext(Dispatchers.Main) {
                    startActivity(Intent.createChooser(shareIntent, "Share Story via"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun captureScreenshot(): Uri? {
        return try {
            val rootView = window.decorView.rootView
            val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            rootView.draw(canvas)

            val file = File(externalCacheDir, "story_screenshot.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }

            FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun toggleBookmark() {
        currentStory?.let { story ->
            lifecycleScope.launch(Dispatchers.IO) {
                if (isBookmarked) {
                    // Remove bookmark
                    val bookmark = bookmarkRepository.getBookmarkById(story.id)
                    bookmark?.let { bookmarkRepository.deleteBookmark(it) }
                    isBookmarked = false
                } else {
                    // Add bookmark
                    val bookmark = Bookmark(
                        storyId = story.id,
                        title = story.title,
                        type = story.type,
                        content = story.content,
                        image = story.image,
                        icon = story.icon
                    )
                    bookmarkRepository.insertBookmark(bookmark)
                    isBookmarked = true
                }

                withContext(Dispatchers.Main) {
                    invalidateOptionsMenu() // Update menu icon
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}