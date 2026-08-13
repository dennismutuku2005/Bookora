package com.dennis.bookora.repository

import com.dennis.bookora.models.Book
import com.dennis.bookora.models.Category
import com.dennis.bookora.models.ListingCondition
import com.dennis.bookora.models.ListingType
import com.dennis.bookora.models.Message
import com.dennis.bookora.models.Notification
import com.dennis.bookora.models.User
import com.dennis.bookora.repository.auth.FirebaseAuthManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseBookRepository @Inject constructor(): BookRepository {
    private val firestore = Firebase.firestore

    override suspend fun getCurrentUser(): User {
        val uid = FirebaseAuthManager.currentUser()?.uid ?: throw IllegalStateException("No user")
        return FirebaseAuthManager.getUserProfile(uid)!!
    }

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

    override suspend fun getCategories(): List<Category> {
        return listOf()
    }

    override suspend fun getBooks(): List<Book> {
        val snap = firestore.collection("books").orderBy("postedDate").get().await()
        return snap.documents.mapNotNull { doc ->
            val d = doc.data ?: return@mapNotNull null
            Book(
                id = doc.id,
                title = d["title"] as? String ?: "",
                author = d["author"] as? String ?: "",
                category = d["category"] as? String ?: "",
                condition = ListingCondition.valueOf(((d["condition"] as? String) ?: "GOOD")),
                location = d["location"] as? String ?: "",
                postedDate = d["postedDate"] as? String ?: "",
                coverUrl = d["coverUrl"] as? String ?: "",
                listingType = ListingType.valueOf(((d["listingType"] as? String) ?: "GIVEAWAY")),
                description = d["description"] as? String ?: "",
                ownerId = d["ownerId"] as? String ?: "",
                rating = (d["rating"] as? Number)?.toDouble() ?: 0.0,
                distance = d["distance"] as? String ?: "",
                isFavorite = (d["isFavorite"] as? Boolean) ?: false
            )
        }
    }

    override suspend fun getFeaturedBooks(): List<Book> {
        val snap = firestore.collection("books").whereEqualTo("listingType", "EXCHANGE").get().await()
        return snap.documents.mapNotNull { doc ->
            val d = doc.data ?: return@mapNotNull null
            Book(
                id = doc.id,
                title = d["title"] as? String ?: "",
                author = d["author"] as? String ?: "",
                category = d["category"] as? String ?: "",
                condition = ListingCondition.valueOf(((d["condition"] as? String) ?: "GOOD")),
                location = d["location"] as? String ?: "",
                postedDate = d["postedDate"] as? String ?: "",
                coverUrl = d["coverUrl"] as? String ?: "",
                listingType = ListingType.valueOf(((d["listingType"] as? String) ?: "GIVEAWAY")),
                description = d["description"] as? String ?: "",
                ownerId = d["ownerId"] as? String ?: "",
                rating = (d["rating"] as? Number)?.toDouble() ?: 0.0,
                distance = d["distance"] as? String ?: "",
                isFavorite = (d["isFavorite"] as? Boolean) ?: false
            )
        }
    }

    override suspend fun getBookById(bookId: String): Book? {
        val doc = firestore.collection("books").document(bookId).get().await()
        val d = doc.data ?: return null
        return Book(
            id = doc.id,
            title = d["title"] as? String ?: "",
            author = d["author"] as? String ?: "",
            category = d["category"] as? String ?: "",
            condition = ListingCondition.valueOf(((d["condition"] as? String) ?: "GOOD")),
            location = d["location"] as? String ?: "",
            postedDate = d["postedDate"] as? String ?: "",
            coverUrl = d["coverUrl"] as? String ?: "",
            listingType = ListingType.valueOf(((d["listingType"] as? String) ?: "GIVEAWAY")),
            description = d["description"] as? String ?: "",
            ownerId = d["ownerId"] as? String ?: "",
            rating = (d["rating"] as? Number)?.toDouble() ?: 0.0,
            distance = d["distance"] as? String ?: "",
            isFavorite = (d["isFavorite"] as? Boolean) ?: false
        )
    }

    override suspend fun getMessages(conversationId: String): List<Message> {
        return emptyList()
    }

    override suspend fun getNotifications(): List<Notification> {
        return emptyList()
    }

    override suspend fun getFavorites(): List<Book> {
        return emptyList()
    }

    override suspend fun saveBook(book: Book) {
        val doc = mapOf(
            "title" to book.title,
            "author" to book.author,
            "category" to book.category,
            "condition" to book.condition.name,
            "location" to book.location,
            "postedDate" to book.postedDate,
            "coverUrl" to book.coverUrl,
            "listingType" to book.listingType.name,
            "description" to book.description,
            "ownerId" to book.ownerId,
            "rating" to book.rating,
            "distance" to book.distance,
            "isFavorite" to book.isFavorite
        )
        firestore.collection("books").add(doc).await()
    }

    override suspend fun toggleFavorite(bookId: String) {
        // not implemented
    }
}
