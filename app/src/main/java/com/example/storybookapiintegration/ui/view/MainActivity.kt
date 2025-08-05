// ui/view/MainActivity.kt
package com.example.storybookapiintegration.ui.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.storybookapiintegration.databinding.ActivityMainBinding
import com.example.storybookapiintegration.data.remote.RetrofitInstance
import com.example.storybookapiintegration.data.repository.StoryRepository
import com.example.storybookapiintegration.ui.adapter.StoryAdapter
import com.example.storybookapiintegration.ui.viewmodel.*
import com.example.storybookapiintegration.utils.Resource

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: StoryViewModel by viewModels {
        StoryViewModelFactory(StoryRepository(RetrofitInstance.api))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        viewModel.fetchAllStories()
    }

    private fun setupRecyclerView() {
        binding.storiesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        viewModel.stories.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let { stories ->
                        if (stories.isEmpty()) {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.storiesRecyclerView.visibility = View.GONE
                        } else {
                            binding.emptyView.visibility = View.GONE
                            binding.storiesRecyclerView.visibility = View.VISIBLE
                            val adapter = StoryAdapter(stories) { story ->
                                // Handle story click
                            }
                            binding.storiesRecyclerView.adapter = adapter
                        }
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }
}