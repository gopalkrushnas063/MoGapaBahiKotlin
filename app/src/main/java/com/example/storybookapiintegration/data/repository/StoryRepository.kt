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
                Log.d("API_RESPONSE", "Response: $response")
                Resource.Success(response)
            }
        } catch (e: HttpException) {
            Log.e("API_ERROR", "HTTP error: ${e.message}")
            Resource.Error(e.message ?: "An unknown error occurred")
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error: ${e.message}")
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}