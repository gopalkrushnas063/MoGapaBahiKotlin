package com.example.storybookapiintegration.ui.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.storybookapiintegration.R
import com.example.storybookapiintegration.data.remote.RetrofitInstance
import com.example.storybookapiintegration.data.repository.StoryRepository
import com.example.storybookapiintegration.databinding.FragmentHomeBinding
import com.example.storybookapiintegration.ui.adapter.LatestStoriesAdapter
import com.example.storybookapiintegration.ui.adapter.StoryAdapter
import com.example.storybookapiintegration.ui.decorations.GridSpacingItemDecoration
import com.example.storybookapiintegration.ui.viewmodel.StoryViewModel
import com.example.storybookapiintegration.ui.viewmodel.StoryViewModelFactory
import com.example.storybookapiintegration.utils.Resource
import com.google.android.material.tabs.TabLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.storybookapiintegration.data.model.Story

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StoryViewModel by viewModels {
        StoryViewModelFactory(StoryRepository(RetrofitInstance.api))
    }
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var latestStoriesAdapter: LatestStoriesAdapter
    private val carouselAutoScrollHandler = Handler(Looper.getMainLooper())
    private var carouselAutoScrollRunnable: Runnable? = null
    private val carouselScrollDelay = 3000L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupViews()
        setupObservers()
        viewModel.fetchAllStories()
    }

    private fun setupViews() {
        latestStoriesAdapter = LatestStoriesAdapter(emptyList()) { story ->
            openStoryDetails(story)
        }

        binding.carouselViewPager.apply {
            adapter = latestStoriesAdapter
            offscreenPageLimit = 3
            clipToPadding = false
            clipChildren = false
            getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.3).toInt()
            val peekOffset = resources.getDimensionPixelOffset(R.dimen.carousel_peek_offset)
            setPadding(peekOffset, 0, peekOffset, 0)
            setPageTransformer { page, position ->
                page.translationX = peekOffset * position
            }
        }

        setupAutoScrollingCarousel()
        setupRecyclerView()

        binding.carouselTitle.visibility = View.GONE
        binding.carouselViewPager.visibility = View.GONE
        binding.tabLayout.visibility = View.GONE
        binding.storiesRecyclerView.visibility = View.GONE
    }

    private fun setupAutoScrollingCarousel() {
        binding.carouselViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                resetAutoScroll()
            }
        })
        startAutoScroll()
    }

    private fun startAutoScroll() {
        carouselAutoScrollRunnable = object : Runnable {
            override fun run() {
                val currentItem = binding.carouselViewPager.currentItem
                val itemCount = latestStoriesAdapter.itemCount
                if (itemCount > 0) {
                    val nextItem = if (currentItem == itemCount - 1) 0 else currentItem + 1
                    binding.carouselViewPager.setCurrentItem(nextItem, true)
                }
                carouselAutoScrollHandler.postDelayed(this, carouselScrollDelay)
            }
        }
        carouselAutoScrollRunnable?.let {
            carouselAutoScrollHandler.postDelayed(it, carouselScrollDelay)
        }
    }

    private fun resetAutoScroll() {
        carouselAutoScrollHandler.removeCallbacks(carouselAutoScrollRunnable ?: return)
        startAutoScroll()
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.storiesRecyclerView.layoutManager = gridLayoutManager

        storyAdapter = StoryAdapter(emptyList()) { story ->
            openStoryDetails(story)
        }
        binding.storiesRecyclerView.adapter = storyAdapter

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        binding.storiesRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(spanCount = 2, spacing = spacingInPixels, includeEdge = true)
        )
        binding.storiesRecyclerView.setPadding(0, 0, 0, 0)
        binding.storiesRecyclerView.clipToPadding = false
    }

    private fun setupObservers() {
        viewModel.stories.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    hideShimmers()
                    resource.data?.let { stories ->
                        showContentViews()
                        latestStoriesAdapter.updateStories(stories.take(5))
                        binding.carouselViewPager.setCurrentItem(latestStoriesAdapter.middlePosition, false)
                        storyAdapter.updateStories(stories)
                        binding.emptyView.visibility = if (stories.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                is Resource.Error -> {
                    hideShimmers()
                    binding.emptyView.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> showShimmers()
            }
        }

        viewModel.storyTypes.observe(viewLifecycleOwner) { resource ->
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
                    Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun setupTabs(types: List<String>) {
        binding.tabLayout.removeAllTabs()
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All"))
        val validTypes = types.filter { !it.isNullOrBlank() }.distinct()
        validTypes.forEach { type ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(type))
        }
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Others"))

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
                        position == 0 -> allStories
                        position == types.size + 1 -> {
                            allStories.filter { story ->
                                story.type.isNullOrBlank() || !types.contains(story.type.trim())
                            }
                        }
                        else -> {
                            val type = types[position - 1]
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
        startActivity(Intent(requireContext(), StoryDetailsActivity::class.java).apply {
            putExtra("STORY_DATA", story)
        })
    }

    override fun onPause() {
        super.onPause()
        carouselAutoScrollHandler.removeCallbacks(carouselAutoScrollRunnable ?: return)
        binding.shimmerCarousel.stopShimmer()
        binding.shimmerStoryList.stopShimmer()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.stories.value is Resource.Loading) {
            binding.shimmerCarousel.startShimmer()
            binding.shimmerStoryList.startShimmer()
        }
        resetAutoScroll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        carouselAutoScrollHandler.removeCallbacks(carouselAutoScrollRunnable ?: return)
        _binding = null
    }
}