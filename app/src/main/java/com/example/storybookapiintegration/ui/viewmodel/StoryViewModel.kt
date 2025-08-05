package com.example.storybookapiintegration.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storybookapiintegration.data.model.Story
import com.example.storybookapiintegration.data.repository.StoryRepository
import com.example.storybookapiintegration.utils.Resource
import kotlinx.coroutines.launch

class StoryViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _stories = MutableLiveData<Resource<List<Story>>>()
    val stories: LiveData<Resource<List<Story>>> = _stories

    fun fetchAllStories() {
        _stories.value = Resource.Loading()
        viewModelScope.launch {
            _stories.value = repository.getAllStories()
        }
    }
}