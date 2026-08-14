package com.dennis.bookora.api

import com.dennis.bookora.models.Book
import com.dennis.bookora.models.Category
import com.dennis.bookora.models.User

data class ApiEnvelope<T>(
    val status: String? = null,
    val message: String? = null,
    val data: T? = null
)

data class BookListResponse(
    val total: Int = 0,
    val page: Int = 1,
    val per_page: Int = 20,
    val total_pages: Int = 0,
    val items: List<Book> = emptyList()
)

data class UserApiResponse(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val memberSince: String = "",
    val rating: Double = 0.0,
    val booksPosted: Int = 0,
    val booksShared: Int = 0,
    val favoritesCount: Int = 0,
    val bio: String = "",
    val shareContactByEmail: Boolean = true
)

fun UserApiResponse.toUser(): User = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    username = username,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    memberSince = memberSince,
    rating = rating,
    booksPosted = booksPosted,
    booksShared = booksShared,
    favoritesCount = favoritesCount,
    bio = bio,
    shareContactByEmail = shareContactByEmail
)

fun User.toApiResponse(): UserApiResponse = UserApiResponse(
    id = id,
    firstName = firstName,
    lastName = lastName,
    username = username,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    memberSince = memberSince,
    rating = rating,
    booksPosted = booksPosted,
    booksShared = booksShared,
    favoritesCount = favoritesCount,
    bio = bio,
    shareContactByEmail = shareContactByEmail
)

data class BookApiResponse(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val category: String = "",
    val condition: String = "GOOD",
    val location: String = "",
    val postedDate: String = "",
    val postedTimestamp: Long = 0L,
    val coverUrl: String = "",
    val listingType: String = "GIVEAWAY",
    val description: String = "",
    val ownerId: String = "",
    val ownerUsername: String = "",
    val rating: Double = 0.0,
    val distance: String = "",
    val isFavorite: Boolean = false,
    val coverColor: Long = 0xFFF0F4FF,
    val created_at: String? = null,
    val updated_at: String? = null
)

fun BookApiResponse.toBook(): Book = Book(
    id = id,
    title = title,
    author = author,
    category = category,
    condition = runCatching { com.dennis.bookora.models.ListingCondition.valueOf(condition) }.getOrDefault(com.dennis.bookora.models.ListingCondition.GOOD),
    location = location,
    postedDate = postedDate,
    postedTimestamp = postedTimestamp,
    coverUrl = coverUrl,
    listingType = runCatching { com.dennis.bookora.models.ListingType.valueOf(listingType) }.getOrDefault(com.dennis.bookora.models.ListingType.GIVEAWAY),
    description = description,
    ownerId = ownerId,
    ownerUsername = ownerUsername,
    rating = rating,
    distance = distance,
    isFavorite = isFavorite,
    coverColor = coverColor
)

data class CategoryApiResponse(
    val id: String = "",
    val title: String = ""
)

fun CategoryApiResponse.toCategory(): Category = Category(id = id, title = title)

data class ConversationApiResponse(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val bookId: String = "",
    val bookTitle: String = "",
    val unreadCount: Int = 0,
    val created_at: String? = null,
    val updated_at: String? = null
)

fun ConversationApiResponse.toChatConversation(): com.dennis.bookora.models.ChatConversation = com.dennis.bookora.models.ChatConversation(
    id = id,
    participantIds = participantIds,
    participantNames = participantNames,
    lastMessage = lastMessage,
    lastTimestamp = lastTimestamp,
    bookId = bookId,
    bookTitle = bookTitle,
    unreadCount = unreadCount
)

data class MessageApiResponse(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val read: Boolean = false,
    val isMine: Boolean = false,
    val created_at: String? = null
)

fun MessageApiResponse.toMessage(): com.dennis.bookora.models.Message = com.dennis.bookora.models.Message(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    senderName = senderName,
    text = text,
    timestamp = timestamp,
    isMine = isMine,
    read = read
)

data class ClaimApiResponse(
    val id: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val claimerId: String = "",
    val claimerName: String = "",
    val claimerEmail: String = "",
    val claimerPhone: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val status: String = "PENDING",
    val timestamp: Long = 0L,
    val confirmedByClaimer: Boolean = false,
    val confirmedByOwner: Boolean = false,
    val created_at: String? = null,
    val updated_at: String? = null
)

fun ClaimApiResponse.toClaimRequest(): com.dennis.bookora.models.ClaimRequest = com.dennis.bookora.models.ClaimRequest(
    id = id,
    bookId = bookId,
    bookTitle = bookTitle,
    claimerId = claimerId,
    claimerName = claimerName,
    claimerEmail = claimerEmail,
    claimerPhone = claimerPhone,
    ownerId = ownerId,
    ownerName = ownerName,
    status = runCatching { com.dennis.bookora.models.ClaimStatus.valueOf(status) }.getOrDefault(com.dennis.bookora.models.ClaimStatus.PENDING),
    timestamp = timestamp,
    confirmedByClaimer = confirmedByClaimer,
    confirmedByOwner = confirmedByOwner
)
