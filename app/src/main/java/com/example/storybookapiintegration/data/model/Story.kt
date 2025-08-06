// Story.kt
package com.example.storybookapiintegration.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Story(
    val id: Int,
    val title: String,
    val type: String?,
    val content: String,
    val image: String?,  // Make nullable
    val icon: String?    // Make nullable
) : Parcelable