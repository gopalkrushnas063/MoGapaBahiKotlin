package com.example.storybookapiintegration.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.storybookapiintegration.R
import com.example.storybookapiintegration.data.local.AppDatabase
import com.example.storybookapiintegration.data.model.Story
import com.example.storybookapiintegration.data.repository.BookmarkRepository
import com.example.storybookapiintegration.databinding.FragmentBookmarksBinding
import com.example.storybookapiintegration.ui.adapter.StoryAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BookmarksFragment : Fragment(R.layout.fragment_bookmarks) {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var storyAdapter: StoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookmarksBinding.bind(view)

        // Initialize database
        val database = AppDatabase.getDatabase(requireContext())
        bookmarkRepository = BookmarkRepository(database.bookmarkDao())

        setupRecyclerView()
        loadBookmarks()
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewBookmarks.layoutManager = gridLayoutManager

        storyAdapter = StoryAdapter(emptyList()) { story ->
            // Open story details
            val intent = Intent(requireContext(), StoryDetailsActivity::class.java).apply {
                putExtra("STORY_DATA", story)
            }
            startActivity(intent)
        }
        binding.recyclerViewBookmarks.adapter = storyAdapter
    }

    // update the loadBookmarks method
    private fun loadBookmarks() {
        lifecycleScope.launch {
            try {
                val bookmarks = bookmarkRepository.getAllBookmarks()
                if (bookmarks.isNotEmpty()) {
                    // Convert bookmarks to stories for the adapter
                    val stories = bookmarks.map { bookmark ->
                        Story(
                            id = bookmark.storyId,
                            title = bookmark.title,
                            type = bookmark.type,
                            content = bookmark.content,
                            image = bookmark.image,
                            icon = bookmark.icon
                        )
                    }
                    storyAdapter.updateStories(stories)
                    binding.emptyView.visibility = View.GONE
                } else {
                    binding.emptyView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.emptyView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}