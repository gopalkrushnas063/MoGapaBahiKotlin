// data/local/BookmarkDao.kt (alternative)
package com.example.storybookapiintegration.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.example.storybookapiintegration.data.model.Bookmark

@Dao
interface BookmarkDao {
    @Insert
    suspend fun insertBookmark(bookmark: Bookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    suspend fun getAllBookmarks(): List<Bookmark>

    @Query("SELECT * FROM bookmarks WHERE storyId = :storyId")
    suspend fun getBookmarkById(storyId: Int): Bookmark?

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE storyId = :storyId)")
    suspend fun isBookmarked(storyId: Int): Boolean
}