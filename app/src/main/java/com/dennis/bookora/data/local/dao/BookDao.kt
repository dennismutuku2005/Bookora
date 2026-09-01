package com.dennis.bookora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dennis.bookora.data.local.entities.BookEntity

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY postedTimestamp DESC")
    suspend fun getAllBooks(): List<BookEntity>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: String): BookEntity?

    @Query("SELECT * FROM books WHERE isFavorite = 1")
    suspend fun getFavoriteBooks(): List<BookEntity>

    @Query("SELECT * FROM books WHERE ownerId = :ownerId")
    suspend fun getBooksByOwner(ownerId: String): List<BookEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBook(id: String)

    @Query("DELETE FROM books")
    suspend fun clearAllBooks()
}
