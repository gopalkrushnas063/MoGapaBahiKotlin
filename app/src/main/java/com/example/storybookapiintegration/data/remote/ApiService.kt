package com.example.storybookapiintegration.data.remote

import com.example.storybookapiintegration.data.model.Story
import retrofit2.http.GET

interface ApiService {
    @GET("storybookapi.json")
    suspend fun getAllStories(): List<Story>
}