package com.dennis.bookora.repository.dummy

import com.dennis.bookora.models.Book
import com.dennis.bookora.models.Category
import com.dennis.bookora.models.ListingCondition
import com.dennis.bookora.models.ListingType
import com.dennis.bookora.models.Message
import com.dennis.bookora.models.Notification
import com.dennis.bookora.models.User
import com.dennis.bookora.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class DummyRepository : BookRepository {
    private val users = sampleUsers
    private val books = sampleBooks
    private val notifications = sampleNotifications
    private val messages = sampleMessages

    override suspend fun getCurrentUser(): User = withContext(Dispatchers.Default) {
        delay(200)
        users.first()
    }

    override suspend fun getUsers(): List<User> = withContext(Dispatchers.Default) {
        delay(100)
        users
    }

    override suspend fun getCategories(): List<Category> = withContext(Dispatchers.Default) {
        delay(80)
        sampleCategories
    }

    override suspend fun getBooks(): List<Book> = withContext(Dispatchers.Default) {
        delay(200)
        books
    }

    override suspend fun getFeaturedBooks(): List<Book> = withContext(Dispatchers.Default) {
        delay(150)
        books.filter { it.listingType == ListingType.EXCHANGE }.take(8)
    }

    override suspend fun getBookById(bookId: String): Book? = withContext(Dispatchers.Default) {
        delay(120)
        books.firstOrNull { it.id == bookId }
    }

    override suspend fun getMessages(conversationId: String): List<Message> = withContext(Dispatchers.Default) {
        delay(120)
        messages.filter { it.conversationId == conversationId }
    }

    override suspend fun getNotifications(): List<Notification> = withContext(Dispatchers.Default) {
        delay(120)
        notifications
    }

    override suspend fun getFavorites(): List<Book> = withContext(Dispatchers.Default) {
        delay(120)
        books.filter { it.isFavorite }
    }

    override suspend fun saveBook(book: Book) {
        withContext(Dispatchers.Default) {
            delay(150)
        }
    }

    override suspend fun toggleFavorite(bookId: String) {
        withContext(Dispatchers.Default) {
            delay(120)
        }
    }
}

private val sampleCategories = listOf(
    Category("cat_programming", "Programming"),
    Category("cat_science", "Science"),
    Category("cat_business", "Business"),
    Category("cat_technology", "Technology"),
    Category("cat_history", "History"),
    Category("cat_novel", "Novel"),
    Category("cat_fantasy", "Fantasy"),
    Category("cat_romance", "Romance"),
    Category("cat_education", "Education")
)

private val sampleUsers = listOf(
    User(
        id = "user_01",
        firstName = "Dennis",
        lastName = "Mutuku",
        username = "dennismutuku",
        email = "dennis@example.com",
        phone = "+254712345678",
        avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=80",
        memberSince = "June 2023",
        rating = 4.9,
        booksPosted = 16,
        booksShared = 28,
        favoritesCount = 12
    ),
    User(
        id = "user_02",
        firstName = "Amina",
        lastName = "Osei",
        username = "aminaosei",
        email = "amina@example.com",
        phone = "+233201234567",
        avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=200&q=80",
        memberSince = "March 2024",
        rating = 4.7,
        booksPosted = 12,
        booksShared = 20,
        favoritesCount = 10
    )
)

private val sampleBooks = listOf(
    Book(
        id = "book_01",
        title = "The Quiet Code",
        author = "Mila Hart",
        category = "Programming",
        condition = ListingCondition.LIKE_NEW,
        location = "Nairobi, Kenya",
        postedDate = "2 days ago",
        coverUrl = "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&w=500&q=80",
        listingType = ListingType.EXCHANGE,
        description = "A modern look at building meaningful software with mindful practices.",
        ownerId = "user_02",
        rating = 4.8,
        distance = "1.3 km",
        isFavorite = true
    ),
    Book(
        id = "book_02",
        title = "Storycraft",
        author = "Nina Osei",
        category = "Novel",
        condition = ListingCondition.GOOD,
        location = "Nairobi, Kenya",
        postedDate = "5 days ago",
        coverUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=500&q=80",
        listingType = ListingType.GIVEAWAY,
        description = "A warm and playful novel about community, books, and shared journeys.",
        ownerId = "user_01",
        rating = 4.6,
        distance = "3.2 km",
        isFavorite = false
    ),
    Book(
        id = "book_03",
        title = "Future Proof",
        author = "Noah Adeyemi",
        category = "Technology",
        condition = ListingCondition.NEW,
        location = "Kisumu, Kenya",
        postedDate = "1 day ago",
        coverUrl = "https://images.unsplash.com/photo-1455885662336-2eadd66e9232?auto=format&fit=crop&w=500&q=80",
        listingType = ListingType.EXCHANGE,
        description = "Explore the ideas shaping the next wave of digital products.",
        ownerId = "user_03",
        rating = 4.9,
        distance = "5.8 km",
        isFavorite = true
    ),
    Book(
        id = "book_04",
        title = "The Soft Algorithm",
        author = "Tessa Kim",
        category = "Science",
        condition = ListingCondition.LIKE_NEW,
        location = "Mombasa, Kenya",
        postedDate = "7 hours ago",
        coverUrl = "https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=500&q=80",
        listingType = ListingType.GIVEAWAY,
        description = "A thoughtful collection of essays on life, science, and creativity.",
        ownerId = "user_04",
        rating = 4.7,
        distance = "12 km",
        isFavorite = false
    )
).let { baseBooks ->
    baseBooks + (5..30).map { index ->
        Book(
            id = "book_%02d".format(index),
            title = listOf("Atlas of Stories", "Midnight Edition", "Creative Minds", "City of Pages", "Design Rituals", "Mindful Algorithms", "Quiet Spaces", "Bloom Theory", "Kindred Journey")[index % 9],
            author = listOf("Abe Okoye", "Lina Kaur", "Diego Ramos", "Ophelia Reed", "Mara Chen", "Ibrahim Saleh", "Noelle Park", "Sofia Lima", "Luca Moretti")[index % 9],
            category = listOf("Fantasy", "Business", "History", "Romance", "Education", "Science", "Technology", "Novel", "Programming")[index % 9],
            condition = ListingCondition.values()[index % ListingCondition.values().size],
            location = listOf("Nairobi", "Kisumu", "Mombasa", "Nakuru", "Malindi")[index % 5] + ", Kenya",
            postedDate = listOf("Today", "Yesterday", "3 days ago", "1 week ago")[index % 4],
            coverUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=500&q=80&sig=$index",
            listingType = if (index % 2 == 0) ListingType.EXCHANGE else ListingType.GIVEAWAY,
            description = "A beautifully designed read with a warm narrative voice and practical insights.",
            ownerId = sampleUsers[0].id,
            rating = 4.5 + (index % 5) * 0.1,
            distance = "${1 + index % 14}.2 km",
            isFavorite = index % 4 == 0
        )
    }
}

private val sampleMessages = listOf(
    Message(
        id = "msg_01",
        conversationId = "conv_01",
        senderId = "user_02",
        senderName = "Amina",
        text = "Hi Dennis, I’m interested in swapping books. Is the cover still available?",
        timestamp = 1691172000000,
        isMine = false
    ),
    Message(
        id = "msg_02",
        conversationId = "conv_01",
        senderId = "user_01",
        senderName = "Dennis",
        text = "Yes! It’s in great shape. I’d love something in business or design.",
        timestamp = 1691175600000,
        isMine = true
    )
)

private val sampleNotifications = listOf(
    Notification(
        id = "notif_01",
        title = "Someone added your book to favorites.",
        subtitle = "A reader liked The Quiet Code.",
        timeAgo = "1h ago"
    ),
    Notification(
        id = "notif_02",
        title = "A user wants to exchange books.",
        subtitle = "Amina sent a message about Storycraft.",
        timeAgo = "3h ago"
    ),
    Notification(
        id = "notif_03",
        title = "Your listing has new views.",
        subtitle = "50 people viewed your latest post.",
        timeAgo = "Yesterday"
    )
)
