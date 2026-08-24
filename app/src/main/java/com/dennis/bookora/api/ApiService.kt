package com.dennis.bookora.api

import com.dennis.bookora.models.Book
import com.dennis.bookora.models.Category
import com.dennis.bookora.models.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("auth.php")
    suspend fun login(@Query("action") action: String = "login", @Body body: @JvmSuppressWildcards Map<String, String>): Response<ApiEnvelope<UserApiResponse>>

    @POST("auth.php")
    suspend fun register(@Query("action") action: String = "register", @Body body: @JvmSuppressWildcards Map<String, String>): Response<ApiEnvelope<UserApiResponse>>

    @POST("auth.php")
    suspend fun loginWithGoogle(@Query("action") action: String = "google_login", @Body body: @JvmSuppressWildcards Map<String, String>): Response<ApiEnvelope<UserApiResponse>>

    @GET("profile.php")
    suspend fun getProfile(@Query("action") action: String = "get", @Query("user_id") userId: String): Response<ApiEnvelope<UserApiResponse>>

    @POST("profile.php")
    suspend fun updateProfile(@Query("action") action: String = "update", @Body body: @JvmSuppressWildcards Map<String, Any>): Response<ApiEnvelope<UserApiResponse>>

    @GET("books.php")
    suspend fun getBooks(@Query("action") action: String = "list", @Query("q") query: String? = null, @Query("user_id") userId: String? = null): Response<ApiEnvelope<List<BookApiResponse>>>

    @GET("books.php")
    suspend fun getBookById(@Query("action") action: String = "get", @Query("id") id: String): Response<ApiEnvelope<BookApiResponse>>

    @POST("books.php")
    suspend fun createBook(@Body body: @JvmSuppressWildcards Map<String, Any>): Response<ApiEnvelope<BookApiResponse>>

    @PUT("books.php")
    suspend fun updateBook(@Query("id") id: String, @Body body: @JvmSuppressWildcards Map<String, Any>): Response<ApiEnvelope<BookApiResponse>>

    @DELETE("books.php")
    suspend fun deleteBook(@Query("id") id: String, @Query("user_id") userId: String): Response<ApiEnvelope<Any>>

    @GET("favorites.php")
    suspend fun getFavorites(@Query("action") action: String = "list", @Query("user_id") userId: String): Response<ApiEnvelope<List<BookApiResponse>>>

    @POST("favorites.php")
    suspend fun addFavorite(@Query("action") action: String = "add", @Body body: @JvmSuppressWildcards Map<String, String>): Response<ApiEnvelope<Any>>

    @POST("favorites.php")
    suspend fun removeFavorite(@Query("action") action: String = "remove", @Body body: @JvmSuppressWildcards Map<String, String>): Response<ApiEnvelope<Any>>

    @GET("mylistings.php")
    suspend fun getMyListings(@Query("user_id") userId: String): Response<ApiEnvelope<List<BookApiResponse>>>

    @GET("notifications.php")
    suspend fun getNotifications(@Query("user_id") userId: String): Response<ApiEnvelope<List<NotificationApiResponse>>>

    @GET("charts.php")
    suspend fun getStats(@Query("action") action: String = "stats", @Query("user_id") userId: String): Response<ApiEnvelope<StatsApiResponse>>

    @GET("categories.php")
    suspend fun getCategories(): Response<ApiEnvelope<List<CategoryApiResponse>>>

    @GET("users.php")
    suspend fun getUsers(): Response<ApiEnvelope<List<UserApiResponse>>>

    // Chat Endpoints
    @GET("chat.php")
    suspend fun getConversations(@Query("action") action: String = "conversations", @Query("user_id") userId: String): Response<ApiEnvelope<List<ConversationApiResponse>>>

    @POST("chat.php")
    suspend fun createConversation(@Query("action") action: String = "create", @Body body: @JvmSuppressWildcards Map<String, String>): Response<ApiEnvelope<ConversationApiResponse>>

    @GET("chat.php")
    suspend fun getMessages(@Query("action") action: String = "messages", @Query("conversation_id") conversationId: String): Response<ApiEnvelope<List<MessageApiResponse>>>

    @POST("chat.php")
    suspend fun sendMessage(@Query("action") action: String = "send", @Body body: @JvmSuppressWildcards Map<String, String>): Response<ApiEnvelope<MessageApiResponse>>

    // Claims Endpoints
    @GET("claims.php")
    suspend fun getClaim(@Query("action") action: String = "get", @Query("claim_id") claimId: String): Response<ApiEnvelope<ClaimApiResponse>>

    @GET("claims.php")
    suspend fun getMyClaims(@Query("action") action: String = "my_claims", @Query("user_id") userId: String, @Query("type") type: String? = null): Response<ApiEnvelope<List<ClaimApiResponse>>>

    @POST("claims.php")
    suspend fun createClaim(@Query("action") action: String = "create", @Body body: @JvmSuppressWildcards Map<String, Any>): Response<ApiEnvelope<ClaimApiResponse>>

    @POST("claims.php")
    suspend fun confirmBookReceived(@Query("action") action: String = "confirm_received", @Body body: @JvmSuppressWildcards Map<String, String>): Response<ApiEnvelope<ClaimApiResponse>>

    @POST("claims.php")
    suspend fun confirmBookShared(@Query("action") action: String = "confirm_shared", @Body body: @JvmSuppressWildcards Map<String, String>): Response<ApiEnvelope<ClaimApiResponse>>

    @POST("claims.php")
    suspend fun acceptClaim(@Query("action") action: String = "accept", @Body body: @JvmSuppressWildcards Map<String, String>): Response<ApiEnvelope<ClaimApiResponse>>

    @POST("claims.php")
    suspend fun rejectClaim(@Query("action") action: String = "reject", @Body body: @JvmSuppressWildcards Map<String, String>): Response<ApiEnvelope<ClaimApiResponse>>

    // Image Upload
    @Multipart
    @POST("upload.php")
    suspend fun uploadImage(
        @Query("type") type: String = "book",
        @Query("user_id") userId: String,
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
}

data class NotificationApiResponse(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val timeAgo: String = "",
    val isRead: Boolean = false,
    val type: String = "notification",
    val conversationId: String = "",
    val senderId: String = "",
    val bookId: String = "",
    val claimRequestId: String = "",
    val timestamp: Long = 0L
)

data class StatsApiResponse(
    val booksPosted: Int = 0,
    val favoritesCount: Int = 0,
    val booksShared: Int = 0,
    val unreadNotifications: Int = 0
)

data class UploadResponse(
    val status: String = "error",
    val message: String = "",
    val data: UploadData? = null
)

data class UploadData(
    val url: String = "",
    val filename: String = "",
    val size: Long = 0,
    val mimeType: String = ""
)

fun NotificationApiResponse.toNotification(): com.dennis.bookora.models.Notification = com.dennis.bookora.models.Notification(
    id = id,
    title = title,
    subtitle = subtitle,
    timeAgo = timeAgo,
    isRead = isRead,
    type = type,
    conversationId = conversationId,
    senderId = senderId,
    bookId = bookId,
    claimRequestId = claimRequestId,
    timestamp = timestamp
)
