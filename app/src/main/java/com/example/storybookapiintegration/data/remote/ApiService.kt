package com.example.storybookapiintegration.data.remote

import com.example.storybookapiintegration.data.model.Story
import retrofit2.http.GET

interface ApiService {
    @GET("story/all_stories")
    suspend fun getAllStories(): List<Story>
}