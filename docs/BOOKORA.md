# Bookora - Community Book Sharing Platform

## Overview

**Bookora** is a modern Android application that builds a sustainable community of book lovers by enabling users to share, exchange, and discover books. It solves the problem of book accessibility while promoting environmental sustainability through resource sharing.

## What is Bookora?

Bookora is a community-driven platform that connects readers who have books they want to share with those who want to read them. It's designed for:

- **Book Enthusiasts** - People passionate about reading and collecting books
- **Students** - Looking for affordable or free textbooks and educational materials
- **Budget-Conscious Readers** - Seeking access to books without purchasing
- **Environmentally Conscious People** - Reducing waste through circular economy
- **Community Builders** - Creating local networks of shared interests

### Core Problem Solved

1. **Book Accessibility** - Not everyone can afford new books
2. **Waste Reduction** - Books that could be reused are often thrown away
3. **Community Building** - Limited platforms to connect local readers
4. **Book Discovery** - Finding relevant books in your area is difficult

## What Bookora Does

### 1. **User Management**
- **Registration & Login** - Email signup or Google OAuth 2.0 Sign-In
- **User Profiles** - Customize with username, phone, bio, avatar
- **User Ratings** - Build trust through community ratings
- **Profile Statistics** - Track listings posted, books shared, activity

### 2. **Book Listing Management**
- **Post Books** - Create listings with:
  - Title, author, category
  - Book condition (New, Like New, Good, Fair)
  - Description and details
  - Cover image
  - Listing type (Giveaway or Exchange)
  - Location information
  
- **Browse Books** - Discover listings with:
  - Search by title, author, category
  - Filter by condition and listing type
  - See distance to each book
  - View owner ratings
  
- **Edit/Delete** - Manage your own listings
- **View Details** - Full book information with owner contact

### 3. **Favorites System**
- **Save Books** - Mark books as favorites for later
- **Quick Access** - View all favorited books in dedicated section
- **Remove** - Un-favorite books anytime
- **Organized Wishlist** - Keep track of books you want

### 4. **Messaging & Chat**
- **Direct Messaging** - Contact book owners
- **Conversations** - Maintain message threads with users
- **Book Context** - Messages linked to specific books
- **Read Status** - Track read/unread messages
- **Real-time Updates** - Instant message notifications

### 5. **Search & Discovery**
- **Full-Text Search** - Search by book title, author, or keywords
- **Category Filtering** - Browse by genre (Fiction, Science, History, etc.)
- **Location-Based** - Find books near you
- **Latest First** - See recently posted books
- **Featured** - Browse exchange listings prominently

### 6. **Notifications**
- **Real-time Alerts** - Get notified of:
  - New messages from users
  - When someone favorites your book
  - When new books match your interests
  - Activity on your listings
  
- **Notification Center** - View all notifications in one place
- **Read/Unread Status** - Track viewed notifications

### 7. **Statistics & Analytics**
- **Personal Dashboard** - See your:
  - Total books posted
  - Total books shared/exchanged
  - Favorites count
  - Average rating
  - Activity metrics
  
- **Seller Stats** - View other users' history and reputation

### 8. **Database Persistence**
- **Room Database** - Local caching for:
  - Offline access to previously loaded books
  - Fast app performance
  - Reduced server load
  - Sync when online
  
- **Automatic Sync** - Keep local data updated with server

## Key Features

### 🎯 Listing Types
- **Giveaway** - Free books you want to share
- **Exchange** - Books you want to trade for other books

### 📍 Location-Based
- Find books near you using location services
- See distance to each listing
- Browse by proximity

### 🔐 Secure Authentication
- Email & password signup
- Google Sign-In with OAuth 2.0
- JWT token-based sessions
- Secure credential storage

### 🎨 Modern UI
- Built with Jetpack Compose (declarative UI)
- Material Design 3 components
- Responsive layouts
- Light/Dark theme support
- Smooth animations

### 💾 Smart Data Management
- Room database for local caching
- Cache-first strategy for better UX
- Automatic offline support
- Efficient data synchronization

### 🚀 Performance
- Fast book discovery
- Instant UI updates
- Lazy loading for lists
- Optimized database queries
- Image caching with Coil

## How to Use Bookora

### As a Book Giver/Exchanger
1. **Sign Up** - Create account (email or Google)
2. **Post Book** - Click "Add Book" and fill details
3. **Select Type** - Choose Giveaway or Exchange
4. **Upload Cover** - Take or select book cover photo
5. **Publish** - List is now live and searchable
6. **Receive Messages** - People will contact you about your books

### As a Book Seeker
1. **Sign Up** - Create your account
2. **Browse** - Explore books available near you
3. **Search** - Find specific titles or authors
4. **Filter** - By category, condition, type
5. **Favorite** - Save books you're interested in
6. **Contact** - Message owners about books
7. **Meet** - Arrange pickup or delivery
8. **Rate** - Leave feedback after exchange

## Technology Stack

### Frontend
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Navigation:** Jetpack Navigation
- **Database:** Room (SQLite)
- **HTTP Client:** Retrofit
- **Dependency Injection:** Hilt

### Backend
- **Language:** PHP 7.4+
- **Database:** MySQL 5.7+
- **API Style:** RESTful
- **Authentication:** JWT tokens
- **Image Storage:** File system / Cloud storage

### Design System
- **Material Design 3**
- **Material Icons**
- **Accessibility First**
- **Dark/Light Themes**

## Data Flow

### Creating a Book Listing
```
User Form Input → Validation → Image Upload → API Request → 
Database Insert → UI Update → Notification → Book Live
```

### Searching/Browsing
```
User Search → Local Cache (instant) + API Call (background) → 
Merge Results → UI Display → Smooth Scroll/Filter
```

### Messaging
```
User Types Message → Send to API → Store in DB → 
Recipient Notified → Real-time Update → Read Status
```

## Benefits of Bookora

### For Readers
✅ Access books without buying  
✅ Find local reading community  
✅ Save money on book purchases  
✅ Discover new authors and genres  
✅ Connect with fellow book lovers  

### For Book Owners
✅ Give books a second life  
✅ Connect with interested readers  
✅ Free books finding new homes  
✅ Build reputation in community  
✅ Share knowledge and passion  

### For Society
✅ Reduce waste and landfill  
✅ Promote reading culture  
✅ Support sustainable consumption  
✅ Build local communities  
✅ Equal access to knowledge  

## Database Schema

### Core Tables
- **users** - User accounts and profiles
- **books** - Book listings
- **categories** - Book categories
- **favorites** - Saved books
- **chat_conversations** - Message threads
- **messages** - Individual messages
- **notifications** - User alerts
- **claim_requests** - Book transfer requests

### Key Relationships
- One user → Many books (one-to-many)
- One category → Many books (one-to-many)
- Many users → Many books (many-to-many via favorites)
- One conversation → Many messages (one-to-many)

## API Endpoints

### Authentication
- `POST /auth.php` - Login, Register, Google Sign-In

### Books
- `GET /books.php` - List all books
- `POST /books.php` - Create book
- `PUT /books.php` - Update book
- `DELETE /books.php` - Delete book

### Favorites
- `GET /favorites.php` - Get favorite books
- `POST /favorites.php` - Add/Remove favorites

### Messaging
- `GET /chat.php` - Get conversations
- `POST /chat.php` - Send message

### User
- `GET /profile.php` - Get user profile
- `POST /profile.php` - Update profile

### Notifications
- `GET /notifications.php` - Get user notifications

## Offline Support

Bookora works offline by:
1. **Caching data** locally using Room database
2. **Loading cached books** instantly on app startup
3. **Queuing messages** for send when online
4. **Syncing** when internet connection restored
5. **Showing indicators** of online/offline status

## Security Features

- **JWT Authentication** - Secure token-based sessions
- **Password Hashing** - BCrypt password encryption
- **HTTPS** - Encrypted API communication
- **Permission Handling** - Runtime permissions for camera, location
- **Credential Storage** - Secure local credential storage
- **Input Validation** - Client and server-side validation

## Performance Optimization

- **Lazy Loading** - Load books as user scrolls
- **Image Caching** - Cache downloaded book covers
- **Database Indexing** - Optimized query performance
- **Coroutines** - Non-blocking async operations
- **Pagination** - Limit data per request
- **Compression** - Reduce network payload

## Future Enhancements

🚀 **Planned Features:**
- Advanced recommendation engine
- Book clubs and reading groups
- Author direct connections
- Reading challenge system
- Book reviews and ratings
- Wishlist sharing
- Social media integration
- Payment for premium listings
- Literary events near you
- Book subscription features

## Community Guidelines

Bookora users agree to:
- ✅ Treat books with respect
- ✅ Communicate honestly about book condition
- ✅ Be courteous and kind to other users
- ✅ Respect privacy of other members
- ✅ Report inappropriate behavior
- ✅ Keep books clean and readable
- ✅ Honor agreed exchanges

## Support & Feedback

Users can:
- Report bugs and issues
- Suggest new features
- Provide app reviews
- Share feedback via in-app forms
- Contact support team

## License

Bookora is developed as an educational project for the Technical University of Kenya, IBL23305 Mobile Application Development course.

---

**Bookora: Connecting readers. Sharing stories. Building community.**

For more technical details, see the main [README.md](../README.md)
