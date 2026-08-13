package com.dennis.bookora.repository.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import com.dennis.bookora.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

object FirebaseAuthManager {
    private const val TAG = "FirebaseAuthManager"

    fun ensureInitialized(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }

    private val auth get() = FirebaseAuth.getInstance()
    private val firestore by lazy { Firebase.firestore }
    private val storage by lazy { Firebase.storage }

    suspend fun uploadProfileImage(uid: String, imageUri: Uri): String {
        try {
            Log.d(TAG, "Uploading image for $uid: $imageUri")
            val ref = storage.reference.child("avatars/$uid.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            Log.d(TAG, "Upload success: $url")
            return url
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed", e)
            throw e
        }
    }

    suspend fun register(email: String, password: String, firstName: String, lastName: String, phone: String = "", username: String = ""): Boolean {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = auth.currentUser
            user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName("$firstName $lastName").build())?.await()

            val uid = user?.uid ?: return false
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val userDoc = mapOf(
                "id" to uid,
                "firstName" to firstName,
                "lastName" to lastName,
                "username" to username.ifBlank { email.substringBefore("@") },
                "email" to email,
                "phone" to phone,
                "avatarUrl" to "",
                "memberSince" to sdf.format(Date()),
                "rating" to 0.0,
                "booksPosted" to 0,
                "booksShared" to 0,
                "favoritesCount" to 0,
                "bio" to "Book lover and exchange enthusiast 📚",
                "shareContactByEmail" to true
            )
            firestore.collection("users").document(uid).set(userDoc).await()
            return result.user != null
        } catch (e: Exception) {
            Log.e(TAG, "Register failed", e)
            return false
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            return result.user != null
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            return false
        }
    }

    suspend fun signInWithGoogle(idToken: String): Boolean {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: return false
            val doc = firestore.collection("users").document(uid).get().await()
            if (!doc.exists()) {
                val displayName = result.user?.displayName ?: ""
                val parts = displayName.split(" ")
                val first = parts.getOrNull(0) ?: ""
                val last = parts.drop(1).joinToString(" ")
                val userDoc = mapOf(
                    "id" to uid,
                    "firstName" to first,
                    "lastName" to last,
                    "username" to (result.user?.email?.substringBefore("@") ?: "reader"),
                    "email" to (result.user?.email ?: ""),
                    "phone" to "",
                    "avatarUrl" to (result.user?.photoUrl?.toString() ?: ""),
                    "memberSince" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    "rating" to 0.0,
                    "booksPosted" to 0,
                    "booksShared" to 0,
                    "favoritesCount" to 0,
                    "bio" to "Book lover and exchange enthusiast 📚",
                    "shareContactByEmail" to true
                )
                firestore.collection("users").document(uid).set(userDoc).await()
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Google sign in failed", e)
            return false
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun currentUser() = auth.currentUser

    suspend fun getUserProfile(uid: String): User? {
        try {
            val firebaseUser = auth.currentUser
            val doc = firestore.collection("users").document(uid).get().await()
            if (!doc.exists()) {
                // Create user doc if missing
                val displayName = firebaseUser?.displayName ?: ""
                val parts = displayName.split(" ")
                val first = parts.getOrNull(0)?.ifBlank { "Book" } ?: "Book"
                val last = parts.drop(1).joinToString(" ").ifBlank { "Reader" }
                val defaultUsername = firebaseUser?.email?.substringBefore("@") ?: "reader"
                val fallbackUserDoc = mapOf(
                    "id" to uid,
                    "firstName" to first,
                    "lastName" to last,
                    "username" to defaultUsername,
                    "email" to (firebaseUser?.email ?: ""),
                    "phone" to (firebaseUser?.phoneNumber ?: ""),
                    "avatarUrl" to (firebaseUser?.photoUrl?.toString() ?: ""),
                    "memberSince" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    "rating" to 0.0,
                    "booksPosted" to 0,
                    "booksShared" to 0,
                    "favoritesCount" to 0,
                    "bio" to "Book lover and exchange enthusiast 📚",
                    "shareContactByEmail" to true
                )
                try { firestore.collection("users").document(uid).set(fallbackUserDoc).await() } catch (_: Exception) {}
                return User(
                    id = uid,
                    firstName = first,
                    lastName = last,
                    username = defaultUsername,
                    email = firebaseUser?.email ?: "",
                    phone = firebaseUser?.phoneNumber ?: "",
                    avatarUrl = firebaseUser?.photoUrl?.toString() ?: "",
                    memberSince = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    rating = 0.0,
                    booksPosted = 0,
                    booksShared = 0,
                    favoritesCount = 0,
                    bio = "Book lover and exchange enthusiast 📚",
                    shareContactByEmail = true
                )
            }
            val data = doc.data ?: return null

            val booksCount = try {
                firestore.collection("books").whereEqualTo("ownerId", uid).get().await().size()
            } catch (_: Exception) {
                (data["booksPosted"] as? Number)?.toInt() ?: 0
            }

            return User(
                id = data["id"] as? String ?: uid,
                firstName = data["firstName"] as? String ?: "",
                lastName = data["lastName"] as? String ?: "",
                username = data["username"] as? String ?: "",
                email = data["email"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                avatarUrl = data["avatarUrl"] as? String ?: "",
                memberSince = data["memberSince"] as? String ?: "",
                rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
                booksPosted = booksCount,
                booksShared = (data["booksShared"] as? Number)?.toInt() ?: 0,
                favoritesCount = (data["favoritesCount"] as? Number)?.toInt() ?: 0,
                bio = data["bio"] as? String ?: "",
                shareContactByEmail = data["shareContactByEmail"] as? Boolean ?: true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Get profile failed for $uid", e)
            val firebaseUser = auth.currentUser
            return User(
                id = uid,
                firstName = firebaseUser?.displayName?.split(" ")?.getOrNull(0) ?: "Book",
                lastName = firebaseUser?.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "Reader",
                username = firebaseUser?.email?.substringBefore("@") ?: "reader",
                email = firebaseUser?.email ?: "",
                phone = "",
                avatarUrl = firebaseUser?.photoUrl?.toString() ?: "",
                memberSince = "",
                rating = 0.0,
                booksPosted = 0,
                booksShared = 0,
                favoritesCount = 0,
                bio = "Book lover and exchange enthusiast 📚"
            )
        }
    }

    suspend fun isUsernameAvailable(username: String, excludeUid: String? = null): Boolean {
        try {
            if (username.isBlank()) return true
            val query = firestore.collection("users").whereEqualTo("username", username).get().await()
            val docs = query.documents
            if (docs.isEmpty()) return true
            if (excludeUid != null && docs.size == 1 && docs[0].getString("id") == excludeUid) return true
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Username check failed", e)
            return true
        }
    }

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>) {
        try {
            Log.d(TAG, "Updating profile for $uid: $updates")
            firestore.collection("users").document(uid).set(updates, SetOptions.merge()).await()
            val user = auth.currentUser
            val first = updates["firstName"] as? String
            val last = updates["lastName"] as? String
            if (!first.isNullOrBlank() || !last.isNullOrBlank()) {
                val display = listOfNotNull(first, last).joinToString(" ")
                user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(display).build())?.await()
            }
            Log.d(TAG, "Profile update success")
        } catch (e: Exception) {
            Log.e(TAG, "Update profile failed", e)
            throw e
        }
    }
}
