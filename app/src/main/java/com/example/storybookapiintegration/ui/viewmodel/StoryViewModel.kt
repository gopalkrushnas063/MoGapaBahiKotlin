package com.example.storybookapiintegration.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storybookapiintegration.data.model.Story
import com.example.storybookapiintegration.data.repository.StoryRepository
import com.example.storybookapiintegration.utils.Resource
import kotlinx.coroutines.launch

// StoryViewModel.kt
class StoryViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _stories = MutableLiveData<Resource<List<Story>>>()
    val stories: LiveData<Resource<List<Story>>> = _stories

    private val _storyTypes = MutableLiveData<Resource<List<String>>>()
    val storyTypes: LiveData<Resource<List<String>>> = _storyTypes

    fun fetchAllStories() {
        _stories.value = Resource.Loading()
        _storyTypes.value = Resource.Loading()

        viewModelScope.launch {
            val storiesResult = repository.getAllStories()
            _stories.value = storiesResult

            // Only fetch types if we got stories successfully
            if (storiesResult is Resource.Success) {
                val typesResult = repository.getStoryTypes()
                _storyTypes.value = typesResult
            } else {
                _storyTypes.value = Resource.Error(storiesResult.message ?: "Failed to fetch data")
            }
        }
    }
}