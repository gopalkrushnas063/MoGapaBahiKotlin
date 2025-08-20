// data/repository/BookmarkRepository.kt
package com.example.storybookapiintegration.data.repository

import com.example.storybookapiintegration.data.local.BookmarkDao
import com.example.storybookapiintegration.data.model.Bookmark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {

    suspend fun insertBookmark(bookmark: Bookmark) = bookmarkDao.insertBookmark(bookmark)

    suspend fun deleteBookmark(bookmark: Bookmark) = bookmarkDao.deleteBookmark(bookmark)

    suspend fun getBookmarkById(storyId: Int): Bookmark? = bookmarkDao.getBookmarkById(storyId)

    suspend fun isBookmarked(storyId: Int): Boolean = bookmarkDao.isBookmarked(storyId)

    suspend fun getAllBookmarks(): List<Bookmark> = bookmarkDao.getAllBookmarks()
}