package com.dennis.bookora.repository.auth

import com.dennis.bookora.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

object FirebaseAuthManager {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { Firebase.firestore }

    suspend fun register(email: String, password: String, firstName: String, lastName: String, phone: String = ""): Boolean {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = auth.currentUser
        user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName("$firstName $lastName").build())?.await()

        // Save profile to Firestore
        val uid = user?.uid ?: return false
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val userDoc = mapOf(
            "id" to uid,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "phone" to phone,
            "avatarUrl" to "",
            "memberSince" to sdf.format(Date()),
            "rating" to 0.0,
            "booksPosted" to 0,
            "booksShared" to 0,
            "favoritesCount" to 0,
            "bio" to ""
        )
        firestore.collection("users").document(uid).set(userDoc).await()
        return result.user != null
    }

    suspend fun login(email: String, password: String): Boolean {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user != null
    }

    suspend fun signInWithGoogle(idToken: String): Boolean {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        // Ensure profile exists in Firestore
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
                "email" to (result.user?.email ?: ""),
                "phone" to "",
                "avatarUrl" to (result.user?.photoUrl?.toString() ?: ""),
                "memberSince" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                "rating" to 0.0,
                "booksPosted" to 0,
                "booksShared" to 0,
                "favoritesCount" to 0,
                "bio" to ""
            )
            firestore.collection("users").document(uid).set(userDoc).await()
        }
        return true
    }

    fun logout() {
        auth.signOut()
    }

    fun currentUser() = auth.currentUser

    suspend fun getUserProfile(uid: String): User? {
        val doc = firestore.collection("users").document(uid).get().await()
        if (!doc.exists()) return null
        val data = doc.data ?: return null
        return User(
            id = data["id"] as? String ?: uid,
            firstName = data["firstName"] as? String ?: "",
            lastName = data["lastName"] as? String ?: "",
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

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>) {
        firestore.collection("users").document(uid).set(updates, SetOptions.merge()).await()
        // Update firebase auth display name if name changed
        val user = auth.currentUser
        val first = updates["firstName"] as? String
        val last = updates["lastName"] as? String
        if (!first.isNullOrBlank() || !last.isNullOrBlank()) {
            val display = listOfNotNull(first, last).joinToString(" ")
            user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(display).build())?.await()
        }
    }
}
