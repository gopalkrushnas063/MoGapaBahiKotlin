// data/model/Bookmark.kt
package com.example.storybookapiintegration.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey val storyId: Int,
    val title: String,
    val type: String?,
    val content: String,
    val image: String?,
    val icon: String?,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable