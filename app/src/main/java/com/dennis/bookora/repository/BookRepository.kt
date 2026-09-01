package com.dennis.bookora.repository

import com.dennis.bookora.models.Book
import com.dennis.bookora.models.Category
import com.dennis.bookora.models.ChatConversation
import com.dennis.bookora.models.ClaimRequest
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
    suspend fun updateBook(bookId: String, book: Book)
    suspend fun deleteBook(bookId: String)
    suspend fun getMyBooks(userId: String): List<Book>
    suspend fun toggleFavorite(bookId: String)
    suspend fun getUserProfile(uid: String): User?
    suspend fun uploadBookCoverImage(context: android.content.Context, imageUri: android.net.Uri): String

    // Chat
    suspend fun getOrCreateConversation(
        bookId: String,
        bookTitle: String,
        otherUserId: String,
        otherUserName: String
    ): String
    suspend fun sendMessage(conversationId: String, text: String): Message
    suspend fun getConversations(): List<ChatConversation>

    // Claim
    suspend fun claimBook(bookId: String, bookTitle: String, ownerId: String): ClaimRequest
    suspend fun getClaimRequest(claimRequestId: String): ClaimRequest?
    suspend fun getMyClaims(userId: String, type: String? = null): List<ClaimRequest>
    suspend fun confirmBookReceived(claimRequestId: String)
    suspend fun confirmBookShared(claimRequestId: String)
}
