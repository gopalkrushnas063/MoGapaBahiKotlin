package com.example.storybookapiintegration.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storybookapiintegration.R
import com.example.storybookapiintegration.data.model.Story
import com.example.storybookapiintegration.data.remote.RetrofitInstance
import com.example.storybookapiintegration.data.repository.StoryRepository
import com.example.storybookapiintegration.databinding.ActivityMainBinding
import com.example.storybookapiintegration.ui.adapter.LatestStoriesAdapter
import com.example.storybookapiintegration.ui.adapter.StoryAdapter
import com.example.storybookapiintegration.ui.decorations.GridSpacingItemDecoration
import com.example.storybookapiintegration.ui.viewmodel.StoryViewModel
import com.example.storybookapiintegration.ui.viewmodel.StoryViewModelFactory
import com.example.storybookapiintegration.utils.Resource
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: StoryViewModel by viewModels {
        StoryViewModelFactory(StoryRepository(RetrofitInstance.api))
    }
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var latestStoriesAdapter: LatestStoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupObservers()
        viewModel.fetchAllStories()
    }

    private fun setupViews() {
        // Setup carousel
        latestStoriesAdapter = LatestStoriesAdapter(emptyList()) { story ->
            openStoryDetails(story)
        }
        binding.carouselViewPager.adapter = latestStoriesAdapter

        // Setup recycler view with GridLayoutManager
        setupRecyclerView()

        // Hide views initially
        binding.carouselTitle.visibility = View.GONE
        binding.carouselViewPager.visibility = View.GONE
        binding.tabLayout.visibility = View.GONE
        binding.storiesRecyclerView.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        // Create GridLayoutManager with 2 columns
        val gridLayoutManager = GridLayoutManager(this, 2)
        binding.storiesRecyclerView.layoutManager = gridLayoutManager

        storyAdapter = StoryAdapter(emptyList()) { story ->
            openStoryDetails(story)
        }
        binding.storiesRecyclerView.adapter = storyAdapter

        // Add item decoration with proper spacing
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        binding.storiesRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount = 2,
                spacing = spacingInPixels,
                includeEdge = true
            )
        )

        // Remove padding from RecyclerView to let decoration handle spacing
        binding.storiesRecyclerView.setPadding(0, 0, 0, 0)
        binding.storiesRecyclerView.clipToPadding = false
    }

    private fun setupObservers() {
        viewModel.stories.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideShimmers()
                    resource.data?.let { stories ->
                        showContentViews()

                        // Update carousel with latest 5 stories
                        latestStoriesAdapter.updateStories(stories.take(5))

                        // Update main list
                        storyAdapter.updateStories(stories)

                        // Show empty view if no stories
                        binding.emptyView.visibility = if (stories.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                is Resource.Error -> {
                    hideShimmers()
                    binding.emptyView.visibility = View.VISIBLE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showShimmers()
                }
            }
        }

        viewModel.storyTypes.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { types ->
                        if (types.isNotEmpty()) {
                            setupTabs(types)
                            binding.tabLayout.visibility = View.VISIBLE
                        }
                    }
                }
                is Resource.Error -> {
                    binding.tabLayout.visibility = View.GONE
                    Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Loading state if needed
                }
            }
        }
    }

    private fun setupTabs(types: List<String>) {
        binding.tabLayout.removeAllTabs()

        // Add "All" tab first
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All"))

        // Add other types (filter out null/empty types first)
        val validTypes = types.filter { !it.isNullOrBlank() }.distinct()
        validTypes.forEach { type ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(type))
        }

        // Add "Others" tab at the end
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Others"))

        // Set up tab listener
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    filterStoriesByTabPosition(position, validTypes)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterStoriesByTabPosition(position: Int, types: List<String>) {
        viewModel.stories.value?.let { resource ->
            if (resource is Resource.Success) {
                resource.data?.let { allStories ->
                    val filtered = when {
                        position == 0 -> allStories // Show all stories
                        position == types.size + 1 -> { // "Others" tab position
                            allStories.filter { story ->
                                story.type.isNullOrBlank() ||
                                        !types.contains(story.type.trim())
                            }
                        }
                        else -> {
                            val type = types[position - 1] // Adjust for "All" tab
                            allStories.filter { it.type.equals(type, ignoreCase = true) }
                        }
                    }
                    storyAdapter.updateStories(filtered)
                    binding.emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showShimmers() {
        with(binding) {
            shimmerCarousel.visibility = View.VISIBLE
            shimmerCarousel.startShimmer()
            shimmerStoryList.visibility = View.VISIBLE
            shimmerStoryList.startShimmer()

            carouselTitle.visibility = View.GONE
            carouselViewPager.visibility = View.GONE
            tabLayout.visibility = View.GONE
            storiesRecyclerView.visibility = View.GONE
            emptyView.visibility = View.GONE
        }
    }

    private fun hideShimmers() {
        with(binding) {
            shimmerCarousel.stopShimmer()
            shimmerCarousel.visibility = View.GONE
            shimmerStoryList.stopShimmer()
            shimmerStoryList.visibility = View.GONE
        }
    }

    private fun showContentViews() {
        with(binding) {
            carouselTitle.visibility = View.VISIBLE
            carouselViewPager.visibility = View.VISIBLE
            storiesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun openStoryDetails(story: Story) {
        startActivity(Intent(this, StoryDetailsActivity::class.java).apply {
            putExtra("STORY_DATA", story)
        })
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerCarousel.stopShimmer()
        binding.shimmerStoryList.stopShimmer()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.stories.value is Resource.Loading) {
            binding.shimmerCarousel.startShimmer()
            binding.shimmerStoryList.startShimmer()
        }
    }
}