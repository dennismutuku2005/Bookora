package com.dennis.bookora.repository

import com.dennis.bookora.models.Book
import com.dennis.bookora.models.Category
import com.dennis.bookora.models.Message
import com.dennis.bookora.models.Notification
import com.dennis.bookora.models.User

interface BookRepository {
    suspend fun getCurrentUser(): User
    suspend fun getUsers(): List<User>
    suspend fun getCategories(): List<Category>
    suspend fun getBooks(): List<Book>
    suspend fun getFeaturedBooks(): List<Book>
    suspend fun getBookById(bookId: String): Book?
    suspend fun getMessages(conversationId: String): List<Message>
    suspend fun getNotifications(): List<Notification>
    suspend fun getFavorites(): List<Book>
    suspend fun saveBook(book: Book)
    suspend fun toggleFavorite(bookId: String)
}
