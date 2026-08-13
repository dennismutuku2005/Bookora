package com.dennis.bookora.repository

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
import com.dennis.bookora.repository.auth.FirebaseAuthManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class FirebaseBookRepository @Inject constructor() : BookRepository {
    private val firestore = Firebase.firestore

    // ──────────────── helpers ────────────────

    private fun currentUid(): String =
        FirebaseAuthManager.currentUser()?.uid ?: throw IllegalStateException("Not logged in")

    private fun docToBook(doc: com.google.firebase.firestore.DocumentSnapshot): Book? {
        val d = doc.data ?: return null
        return Book(
            id = doc.id,
            title = d["title"] as? String ?: "",
            author = d["author"] as? String ?: "",
            category = d["category"] as? String ?: "",
            condition = runCatching {
                ListingCondition.valueOf((d["condition"] as? String) ?: "GOOD")
            }.getOrDefault(ListingCondition.GOOD),
            location = d["location"] as? String ?: "",
            postedDate = d["postedDate"] as? String ?: "",
            postedTimestamp = (d["postedTimestamp"] as? Number)?.toLong() ?: 0L,
            coverUrl = d["coverUrl"] as? String ?: "",
            listingType = runCatching {
                ListingType.valueOf((d["listingType"] as? String) ?: "GIVEAWAY")
            }.getOrDefault(ListingType.GIVEAWAY),
            description = d["description"] as? String ?: "",
            ownerId = d["ownerId"] as? String ?: "",
            ownerUsername = d["ownerUsername"] as? String ?: "",
            rating = (d["rating"] as? Number)?.toDouble() ?: 0.0,
            distance = d["distance"] as? String ?: "",
            isFavorite = (d["isFavorite"] as? Boolean) ?: false,
            coverColor = (d["coverColor"] as? Number)?.toLong() ?: 0xFFF0F4FF
        )
    }

    // ──────────────── user ────────────────

    override suspend fun getCurrentUser(): User {
        val uid = currentUid()
        return FirebaseAuthManager.getUserProfile(uid)!!
    }

    override suspend fun getUserProfile(uid: String): User? =
        FirebaseAuthManager.getUserProfile(uid)

    override suspend fun getUsers(): List<User> {
        val snap = firestore.collection("users").get().await()
        return snap.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            User(
                id = data["id"] as? String ?: doc.id,
                firstName = data["firstName"] as? String ?: "",
                lastName = data["lastName"] as? String ?: "",
                username = data["username"] as? String ?: "",
                email = data["email"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                avatarUrl = data["avatarUrl"] as? String ?: "",
                memberSince = data["memberSince"] as? String ?: "",
                rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
                booksPosted = (data["booksPosted"] as? Number)?.toInt() ?: 0,
                booksShared = (data["booksShared"] as? Number)?.toInt() ?: 0,
                favoritesCount = (data["favoritesCount"] as? Number)?.toInt() ?: 0,
                bio = data["bio"] as? String ?: ""
            )
        }
    }

    // ──────────────── categories ────────────────

    override suspend fun getCategories(): List<Category> = listOf()

    // ──────────────── books ────────────────

    override suspend fun getBooks(): List<Book> {
        return try {
            val snap = firestore.collection("books").get().await()
            snap.documents.mapNotNull { docToBook(it) }
                .sortedByDescending { it.postedTimestamp }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getFeaturedBooks(): List<Book> {
        return try {
            val snap = firestore.collection("books")
                .whereEqualTo("listingType", "EXCHANGE")
                .get().await()
            snap.documents.mapNotNull { docToBook(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getBookById(bookId: String): Book? {
        return try {
            val doc = firestore.collection("books").document(bookId).get().await()
            if (doc.exists()) docToBook(doc) else null
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun saveBook(book: Book) {
        val uid = currentUid()
        val username = try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            userDoc.getString("username") ?: ""
        } catch (_: Exception) { "" }

        val doc = mutableMapOf(
            "title" to book.title,
            "author" to book.author,
            "category" to book.category,
            "condition" to book.condition.name,
            "location" to book.location,
            "postedDate" to book.postedDate,
            "postedTimestamp" to System.currentTimeMillis(),
            "coverUrl" to book.coverUrl,
            "listingType" to book.listingType.name,
            "description" to book.description,
            "ownerId" to uid,
            "ownerUsername" to username,
            "rating" to book.rating,
            "distance" to book.distance,
            "isFavorite" to book.isFavorite,
            "coverColor" to book.coverColor
        )
        firestore.collection("books").add(doc).await()
    }

    override suspend fun updateBook(bookId: String, book: Book) {
        val doc = mapOf(
            "title" to book.title,
            "author" to book.author,
            "category" to book.category,
            "condition" to book.condition.name,
            "location" to book.location,
            "coverUrl" to book.coverUrl,
            "listingType" to book.listingType.name,
            "description" to book.description,
        )
        firestore.collection("books").document(bookId).update(doc).await()
    }

    override suspend fun deleteBook(bookId: String) {
        firestore.collection("books").document(bookId).delete().await()
    }

    override suspend fun getMyBooks(userId: String): List<Book> {
        return try {
            val snap = firestore.collection("books")
                .whereEqualTo("ownerId", userId)
                .get().await()
            snap.documents.mapNotNull { docToBook(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun toggleFavorite(bookId: String) {
        val uid = currentUid()
        val favRef = firestore.collection("users").document(uid).collection("favorites").document(bookId)
        val existing = favRef.get().await()
        if (existing.exists()) {
            favRef.delete().await()
        } else {
            favRef.set(mapOf("bookId" to bookId, "timestamp" to System.currentTimeMillis())).await()
        }
    }

    override suspend fun getFavorites(): List<Book> {
        val uid = try { currentUid() } catch (_: Exception) { return emptyList() }
        return try {
            val favSnap = firestore.collection("users").document(uid).collection("favorites").get().await()
            val ids = favSnap.documents.mapNotNull { it.getString("bookId") }
            if (ids.isEmpty()) return emptyList()
            // Firestore whereIn has a limit; fetch in batches of 10
            val chunks = ids.chunked(10)
            val books = mutableListOf<Book>()
            for (chunk in chunks) {
                val snap = firestore.collection("books").whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk).get().await()
                books.addAll(snap.documents.mapNotNull { docToBook(it) })
            }
            books.sortedByDescending { it.postedTimestamp }
        } catch (_: Exception) { emptyList() }
    }

    // ──────────────── notifications ────────────────

    override suspend fun getNotifications(): List<Notification> {
        val uid = currentUid()
        return try {
            val snap = firestore.collection("notifications")
                .document(uid)
                .collection("items")
                .get().await()
            snap.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                Notification(
                    id = doc.id,
                    title = d["title"] as? String ?: "",
                    subtitle = d["subtitle"] as? String ?: "",
                    timeAgo = d["timeAgo"] as? String ?: "",
                    isRead = d["isRead"] as? Boolean ?: false,
                    type = d["type"] as? String ?: "notification",
                    conversationId = d["conversationId"] as? String ?: "",
                    senderId = d["senderId"] as? String ?: "",
                    bookId = d["bookId"] as? String ?: "",
                    claimRequestId = d["claimRequestId"] as? String ?: "",
                    timestamp = (d["timestamp"] as? Number)?.toLong() ?: 0L
                )
            }.sortedByDescending { it.timestamp }
        } catch (_: Exception) { emptyList() }
    }

    // ──────────────── chat ────────────────

    override suspend fun getConversations(): List<ChatConversation> {
        val uid = currentUid()
        return try {
            val snap = firestore.collection("conversations")
                .whereArrayContains("participantIds", uid)
                .get().await()
            snap.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                @Suppress("UNCHECKED_CAST")
                ChatConversation(
                    id = doc.id,
                    participantIds = (d["participantIds"] as? List<String>) ?: emptyList(),
                    participantNames = (d["participantNames"] as? Map<String, String>) ?: emptyMap(),
                    lastMessage = d["lastMessage"] as? String ?: "",
                    lastTimestamp = (d["lastTimestamp"] as? Number)?.toLong() ?: 0L,
                    bookId = d["bookId"] as? String ?: "",
                    bookTitle = d["bookTitle"] as? String ?: "",
                    unreadCount = (d["unreadCount_$uid"] as? Number)?.toInt() ?: 0
                )
            }.sortedByDescending { it.lastTimestamp }
        } catch (_: Exception) { emptyList() }
    }

    override suspend fun getOrCreateConversation(
        bookId: String,
        bookTitle: String,
        otherUserId: String,
        otherUserName: String
    ): String {
        val uid = currentUid()
        val myProfile = try { FirebaseAuthManager.getUserProfile(uid) } catch (_: Exception) { null }
        val myName = myProfile?.let { "${it.firstName} ${it.lastName}".trim() }
            ?: FirebaseAuthManager.currentUser()?.displayName ?: "User"

        val existing = firestore.collection("conversations")
            .whereArrayContains("participantIds", uid)
            .get().await()

        val found = existing.documents.firstOrNull { doc ->
            val ids = doc["participantIds"] as? List<*>
            val bId = doc.getString("bookId")
            ids?.contains(otherUserId) == true && bId == bookId
        }
        if (found != null) return found.id

        val convoData = mapOf(
            "participantIds" to listOf(uid, otherUserId),
            "participantNames" to mapOf(uid to myName, otherUserId to otherUserName),
            "bookId" to bookId,
            "bookTitle" to bookTitle,
            "lastMessage" to "",
            "lastTimestamp" to System.currentTimeMillis(),
            "unreadCount_$otherUserId" to 0
        )
        val ref = firestore.collection("conversations").add(convoData).await()
        return ref.id
    }

    override suspend fun sendMessage(conversationId: String, text: String): Message {
        val uid = currentUid()
        val myProfile = try { FirebaseAuthManager.getUserProfile(uid) } catch (_: Exception) { null }
        val myName = myProfile?.let { "${it.firstName} ${it.lastName}".trim() }
            ?: FirebaseAuthManager.currentUser()?.displayName ?: "User"

        val ts = System.currentTimeMillis()
        val msgData = mapOf(
            "senderId" to uid,
            "senderName" to myName,
            "text" to text,
            "timestamp" to ts,
            "read" to false
        )
        val msgRef = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(msgData).await()

        firestore.collection("conversations").document(conversationId).update(
            mapOf(
                "lastMessage" to text,
                "lastTimestamp" to ts
            )
        ).await()

        return Message(
            id = msgRef.id,
            conversationId = conversationId,
            senderId = uid,
            senderName = myName,
            text = text,
            timestamp = ts,
            isMine = true,
            read = false
        )
    }

    override suspend fun getMessages(conversationId: String): List<Message> {
        val uid = currentUid()
        return try {
            val snap = firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .get().await()
            snap.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                Message(
                    id = doc.id,
                    conversationId = conversationId,
                    senderId = d["senderId"] as? String ?: "",
                    senderName = d["senderName"] as? String ?: "",
                    text = d["text"] as? String ?: "",
                    timestamp = (d["timestamp"] as? Number)?.toLong() ?: 0L,
                    isMine = (d["senderId"] as? String) == uid,
                    read = d["read"] as? Boolean ?: false
                )
            }.sortedBy { it.timestamp }
        } catch (_: Exception) { emptyList() }
    }

    // ──────────────── claims ────────────────

    override suspend fun claimBook(bookId: String, bookTitle: String, ownerId: String): ClaimRequest {
        val uid = currentUid()
        val myProfile = try { FirebaseAuthManager.getUserProfile(uid) } catch (_: Exception) { null }
        val myName = myProfile?.let { "${it.firstName} ${it.lastName}".trim() } ?: "User"
        val myEmail = myProfile?.email ?: ""
        val myPhone = myProfile?.phone ?: ""

        val ownerProfile = try { FirebaseAuthManager.getUserProfile(ownerId) } catch (_: Exception) { null }
        val ownerName = ownerProfile?.let { "${it.firstName} ${it.lastName}".trim() } ?: "Owner"

        val ts = System.currentTimeMillis()
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        val claimData = mapOf(
            "bookId" to bookId,
            "bookTitle" to bookTitle,
            "claimerId" to uid,
            "claimerName" to myName,
            "claimerEmail" to myEmail,
            "claimerPhone" to myPhone,
            "ownerId" to ownerId,
            "ownerName" to ownerName,
            "status" to ClaimStatus.PENDING.name,
            "timestamp" to ts,
            "confirmedByClaimer" to false,
            "confirmedByOwner" to false
        )
        val ref = firestore.collection("claimRequests").add(claimData).await()

        val notifData = mapOf(
            "title" to "📚 New Claim Request",
            "subtitle" to "$myName wants to claim your book \"$bookTitle\"",
            "timeAgo" to sdf.format(Date(ts)),
            "isRead" to false,
            "type" to "claim",
            "claimRequestId" to ref.id,
            "bookId" to bookId,
            "senderId" to uid,
            "timestamp" to ts
        )
        firestore.collection("notifications").document(ownerId)
            .collection("items").add(notifData).await()

        return ClaimRequest(
            id = ref.id,
            bookId = bookId,
            bookTitle = bookTitle,
            claimerId = uid,
            claimerName = myName,
            claimerEmail = myEmail,
            claimerPhone = myPhone,
            ownerId = ownerId,
            ownerName = ownerName,
            status = ClaimStatus.PENDING,
            timestamp = ts
        )
    }

    override suspend fun getClaimRequest(claimRequestId: String): ClaimRequest? {
        return try {
            val doc = firestore.collection("claimRequests").document(claimRequestId).get().await()
            val d = doc.data ?: return null
            ClaimRequest(
                id = doc.id,
                bookId = d["bookId"] as? String ?: "",
                bookTitle = d["bookTitle"] as? String ?: "",
                claimerId = d["claimerId"] as? String ?: "",
                claimerName = d["claimerName"] as? String ?: "",
                claimerEmail = d["claimerEmail"] as? String ?: "",
                claimerPhone = d["claimerPhone"] as? String ?: "",
                ownerId = d["ownerId"] as? String ?: "",
                ownerName = d["ownerName"] as? String ?: "",
                status = runCatching { ClaimStatus.valueOf(d["status"] as? String ?: "") }
                    .getOrDefault(ClaimStatus.PENDING),
                timestamp = (d["timestamp"] as? Number)?.toLong() ?: 0L,
                confirmedByClaimer = d["confirmedByClaimer"] as? Boolean ?: false,
                confirmedByOwner = d["confirmedByOwner"] as? Boolean ?: false
            )
        } catch (_: Exception) { null }
    }

    override suspend fun confirmBookReceived(claimRequestId: String) {
        val claimRef = firestore.collection("claimRequests").document(claimRequestId)
        val claim = getClaimRequest(claimRequestId) ?: return

        claimRef.update("confirmedByClaimer", true).await()

        val updatedClaim = claim.copy(confirmedByClaimer = true)
        val newStatus = when {
            updatedClaim.confirmedByOwner -> ClaimStatus.COMPLETED
            else -> ClaimStatus.CONFIRMED_CLAIMER
        }
        claimRef.update("status", newStatus.name).await()

        val ts = System.currentTimeMillis()
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        if (newStatus == ClaimStatus.COMPLETED) {
            firestore.collection("notifications").document(claim.ownerId)
                .collection("items").add(mapOf(
                    "title" to "✅ Exchange Complete!",
                    "subtitle" to "${claim.claimerName} confirmed receiving \"${claim.bookTitle}\"",
                    "timeAgo" to sdf.format(Date(ts)),
                    "isRead" to false,
                    "type" to "notification",
                    "bookId" to claim.bookId,
                    "timestamp" to ts
                )).await()
        }
    }

    override suspend fun confirmBookShared(claimRequestId: String) {
        val claimRef = firestore.collection("claimRequests").document(claimRequestId)
        val claim = getClaimRequest(claimRequestId) ?: return

        claimRef.update("confirmedByOwner", true).await()

        val updatedClaim = claim.copy(confirmedByOwner = true)
        val newStatus = when {
            updatedClaim.confirmedByClaimer -> ClaimStatus.COMPLETED
            else -> ClaimStatus.CONFIRMED_OWNER
        }
        claimRef.update("status", newStatus.name).await()

        val ts = System.currentTimeMillis()
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        firestore.collection("notifications").document(claim.claimerId)
            .collection("items").add(mapOf(
                "title" to "📬 Book is on its way!",
                "subtitle" to "${claim.ownerName} confirmed sharing \"${claim.bookTitle}\"",
                "timeAgo" to sdf.format(Date(ts)),
                "isRead" to false,
                "type" to "claim",
                "claimRequestId" to claimRequestId,
                "bookId" to claim.bookId,
                "timestamp" to ts
            )).await()
    }
}
