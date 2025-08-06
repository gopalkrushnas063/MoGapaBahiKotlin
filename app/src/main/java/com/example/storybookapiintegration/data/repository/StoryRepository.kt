package com.example.storybookapiintegration.data.repository


import android.util.Log
import com.example.storybookapiintegration.data.model.Story
import com.example.storybookapiintegration.data.remote.ApiService
import com.example.storybookapiintegration.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

// In StoryRepository.kt
class StoryRepository(private val apiService: ApiService) {
    suspend fun getAllStories(): Resource<List<Story>> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getAllStories()
                Resource.Success(response)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getStoryTypes(): Resource<List<String>> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getAllStories()
                val types = response.map { it.type }.distinct()
                Resource.Success(types)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch story types")
        } as Resource<List<String>>
    }
}