package com.dennis.bookora.repository

import android.content.Context
import android.net.Uri
import com.dennis.bookora.api.RetrofitClient
import com.dennis.bookora.api.toBook
import com.dennis.bookora.api.toCategory
import com.dennis.bookora.api.toNotification
import com.dennis.bookora.api.toUser
import com.dennis.bookora.api.toChatConversation
import com.dennis.bookora.api.toMessage
import com.dennis.bookora.api.toClaimRequest
import com.dennis.bookora.models.Book
import com.dennis.bookora.models.Category
import com.dennis.bookora.models.ChatConversation
import com.dennis.bookora.models.ClaimRequest
import com.dennis.bookora.models.ClaimStatus
import com.dennis.bookora.models.ListingCondition
import com.dennis.bookora.models.ListingType
import com.dennis.bookora.models.Message
import com.dennis.bookora.models.Notification
import com.dennis.bookora.models.User
import com.dennis.bookora.repository.auth.AuthSession
import com.dennis.bookora.repository.auth.AuthManager
import com.dennis.bookora.data.local.BookoraDatabase
import com.dennis.bookora.BookoraApplication
import com.dennis.bookora.data.local.dao.BookDao
import com.dennis.bookora.data.local.dao.CategoryDao
import com.dennis.bookora.data.local.entities.toBook
import com.dennis.bookora.data.local.entities.toCategory
import com.dennis.bookora.data.local.entities.toEntity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

class ApiBookRepository @Inject constructor(
    private val bookDao: BookDao,
    private val categoryDao: CategoryDao
) : BookRepository {
    private fun currentUid(): String =
        AuthManager.currentUser()?.uid ?: AuthSession.currentUserId() ?: throw IllegalStateException("Not logged in")

    // Upload book cover image
    override suspend fun uploadBookCoverImage(context: Context, imageUri: Uri): String {
        val uid = AuthSession.currentUserId() ?: AuthManager.currentUser()?.id ?: "guest_user"
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw Exception("Could not open selected image stream")
            
            val fileName = "book_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(context.cacheDir, fileName)
            
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val requestBody = file.asRequestBody("image/jpeg".toMediaType())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

            val response = RetrofitClient.apiService.uploadImage(type = "book", userId = uid, file = part)
            
            file.delete() // Clean up temp file
            
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data?.url ?: imageUri.toString()
            } else {
                val errorMsg = response.body()?.message ?: "Server returned error code (${response.code()})"
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            throw Exception("Failed to upload image: ${e.message}")
        }
    }

    override suspend fun getCurrentUser(): User {
        val uid = currentUid()
        return AuthManager.getUserProfile(uid) ?: throw IllegalStateException("User profile not found")
    }

    override suspend fun getUsers(): List<User> {
        return try {
            RetrofitClient.apiService.getUsers().body()?.data.orEmpty().map { it.toUser() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getCategories(): List<Category> {
        return try {
            val categories = RetrofitClient.apiService.getCategories().body()?.data.orEmpty().map { it.toCategory() }
            if (categories.isNotEmpty()) {
                categoryDao.insertCategories(categories.map { it.toEntity() })
            }
            categories
        } catch (_: Exception) {
            categoryDao.getAllCategories().map { it.toCategory() }
        }
    }

    override suspend fun getBooks(): List<Book> {
        return try {
            val resp = RetrofitClient.apiService.getBooks()
            val books = resp.body()?.data.orEmpty().map { it.toBook() }
            if (books.isNotEmpty()) {
                bookDao.insertBooks(books.map { it.toEntity() })
            }
            books
        } catch (_: Exception) {
            bookDao.getAllBooks().map { it.toBook() }
        }
    }

    override suspend fun getFeaturedBooks(): List<Book> {
        return getBooks().filter { it.listingType == ListingType.EXCHANGE }
    }

    override suspend fun getBookById(bookId: String): Book? {
        return try {
            val book = RetrofitClient.apiService.getBookById(id = bookId).body()?.data?.toBook()
            if (book != null) {
                bookDao.insertBook(book.toEntity())
            }
            book ?: bookDao.getBookById(bookId)?.toBook()
        } catch (_: Exception) {
            bookDao.getBookById(bookId)?.toBook()
        }
    }

    override suspend fun getMessages(conversationId: String): List<Message> {
        return try {
            val uid = currentUid()
            RetrofitClient.apiService.getMessages(conversationId = conversationId).body()?.data.orEmpty().map {
                it.toMessage().copy(isMine = it.senderId == uid)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getNotifications(): List<Notification> {
        val uid = currentUid()
        return try {
            RetrofitClient.apiService.getNotifications(userId = uid).body()?.data.orEmpty().map { it.toNotification() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getFavorites(): List<Book> {
        val uid = currentUid()
        return try {
            RetrofitClient.apiService.getFavorites(userId = uid).body()?.data.orEmpty().map { it.toBook() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun saveBook(book: Book) {
        val uid = currentUid()
        val map = linkedMapOf<String, Any>(
            "user_id" to uid,
            "title" to book.title,
            "author" to book.author,
            "category" to book.category,
            "condition" to book.condition.name,
            "location" to book.location,
            "postedDate" to book.postedDate,
            "postedTimestamp" to book.postedTimestamp,
            "coverUrl" to book.coverUrl,
            "listingType" to book.listingType.name,
            "description" to book.description,
            "ownerId" to uid,
            "ownerUsername" to book.ownerUsername.ifBlank { AuthManager.getUserProfile(uid)?.username.orEmpty() },
            "rating" to book.rating,
            "distance" to book.distance,
            "isFavorite" to book.isFavorite,
            "coverColor" to book.coverColor.toString()
        )
        RetrofitClient.apiService.createBook(map)
    }

    override suspend fun updateBook(bookId: String, book: Book) {
        val uid = currentUid()
        val map = linkedMapOf<String, Any>(
            "user_id" to uid,
            "id" to bookId,
            "title" to book.title,
            "author" to book.author,
            "category" to book.category,
            "condition" to book.condition.name,
            "location" to book.location,
            "coverUrl" to book.coverUrl,
            "listingType" to book.listingType.name,
            "description" to book.description
        )
        RetrofitClient.apiService.updateBook(id = bookId, body = map)
    }

    override suspend fun deleteBook(bookId: String) {
        try { bookDao.deleteBook(bookId) } catch (_: Exception) {}
        RetrofitClient.apiService.deleteBook(id = bookId, userId = currentUid())
    }

    override suspend fun getMyBooks(userId: String): List<Book> {
        return try {
            val response = RetrofitClient.apiService.getMyListings(userId = userId)
            response.body()?.data.orEmpty().map { it.toBook() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun toggleFavorite(bookId: String) {
        val uid = currentUid()
        val response = RetrofitClient.apiService.addFavorite(
            action = "add",
            body = mapOf("user_id" to uid, "book_id" to bookId)
        )
        if (response.isSuccessful && response.body()?.status == "error") {
            RetrofitClient.apiService.removeFavorite(
                action = "remove",
                body = mapOf("user_id" to uid, "book_id" to bookId)
            )
        }
    }

    override suspend fun getUserProfile(uid: String): User? {
        return try {
            RetrofitClient.apiService.getProfile(userId = uid).body()?.data?.toUser()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getOrCreateConversation(
        bookId: String,
        bookTitle: String,
        otherUserId: String,
        otherUserName: String
    ): String {
        val uid = currentUid()
        val myProfile = AuthManager.getUserProfile(uid)
        val myName = myProfile?.displayName ?: "User"
        
        return try {
            val response = RetrofitClient.apiService.createConversation(
                action = "create",
                body = mapOf(
                    "user_id" to uid,
                    "other_user_id" to otherUserId,
                    "other_user_name" to otherUserName,
                    "book_id" to bookId,
                    "book_title" to bookTitle
                )
            )
            response.body()?.data?.id ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    override suspend fun sendMessage(conversationId: String, text: String): Message {
        val uid = currentUid()
        val myProfile = AuthManager.getUserProfile(uid)
        val myName = myProfile?.displayName ?: "User"
        
        return try {
            val response = RetrofitClient.apiService.sendMessage(
                action = "send",
                body = mapOf(
                    "conversation_id" to conversationId,
                    "user_id" to uid,
                    "sender_name" to myName,
                    "text" to text
                )
            )
            response.body()?.data?.toMessage()?.copy(isMine = true) ?: Message()
        } catch (_: Exception) {
            Message()
        }
    }

    override suspend fun getConversations(): List<ChatConversation> {
        val uid = currentUid()
        return try {
            RetrofitClient.apiService.getConversations(userId = uid).body()?.data.orEmpty().map { it.toChatConversation() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun claimBook(bookId: String, bookTitle: String, ownerId: String): ClaimRequest {
        val uid = currentUid()
        val myProfile = AuthManager.getUserProfile(uid)
        val myName = myProfile?.displayName ?: "User"
        val myEmail = myProfile?.email ?: ""
        val myPhone = myProfile?.phone ?: ""
        
        val ownerProfile = AuthManager.getUserProfile(ownerId)
        val ownerName = ownerProfile?.displayName ?: "Owner"

        return try {
            val response = RetrofitClient.apiService.createClaim(
                action = "create",
                body = mapOf(
                    "book_id" to bookId,
                    "book_title" to bookTitle,
                    "claimer_id" to uid,
                    "claimer_name" to myName,
                    "claimer_email" to myEmail,
                    "claimer_phone" to myPhone,
                    "owner_id" to ownerId,
                    "owner_name" to ownerName
                )
            )
            response.body()?.data?.toClaimRequest() ?: ClaimRequest()
        } catch (_: Exception) {
            ClaimRequest()
        }
    }

    override suspend fun getClaimRequest(claimRequestId: String): ClaimRequest? {
        return try {
            RetrofitClient.apiService.getClaim(claimId = claimRequestId).body()?.data?.toClaimRequest()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getMyClaims(userId: String, type: String?): List<ClaimRequest> {
        return try {
            RetrofitClient.apiService.getMyClaims(userId = userId, type = type).body()?.data.orEmpty().map { it.toClaimRequest() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun confirmBookReceived(claimRequestId: String) {
        val uid = currentUid()
        try {
            RetrofitClient.apiService.confirmBookReceived(
                action = "confirm_received",
                body = mapOf("claim_id" to claimRequestId, "user_id" to uid)
            )
        } catch (_: Exception) {}
    }

    override suspend fun confirmBookShared(claimRequestId: String) {
        val uid = currentUid()
        try {
            RetrofitClient.apiService.confirmBookShared(
                action = "confirm_shared",
                body = mapOf("claim_id" to claimRequestId, "user_id" to uid)
            )
        } catch (_: Exception) {}
    }
}
