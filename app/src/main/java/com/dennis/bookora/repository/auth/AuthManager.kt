package com.dennis.bookora.repository.auth

import android.content.Context
import android.net.Uri
import com.dennis.bookora.api.RetrofitClient
import com.dennis.bookora.api.toUser
import com.dennis.bookora.models.User

object AuthManager {
    private var cachedUser: User? = null

    fun ensureInitialized(context: Context) {
        AuthSession.init(context)
    }

    fun getCachedUser(): User? = cachedUser

    suspend fun uploadProfileImage(uid: String, imageUri: Uri): String {
        return imageUri.toString()
    }

    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String = "",
        username: String = ""
    ): Boolean {
        val payload = linkedMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to username.ifBlank { email.substringBefore("@") },
            "email" to email,
            "phone" to phone,
            "phoneNumber" to phone,
            "password" to password
        )
        val response = RetrofitClient.apiService.register(action = "register", body = payload)
        val user = response.body()?.data?.toUser()
        if (user != null) {
            cachedUser = user
            AuthSession.saveUser(user)
            return true
        }
        return false
    }

    suspend fun login(email: String, password: String): Boolean {
        val response = RetrofitClient.apiService.login(action = "login", body = mapOf(
            "email" to email,
            "password" to password
        ))
        val user = response.body()?.data?.toUser()
        if (user != null) {
            cachedUser = user
            AuthSession.saveUser(user)
            return true
        }
        return false
    }

    suspend fun signInWithGoogle(idToken: String): Boolean = false

    fun logout() {
        cachedUser = null
        AuthSession.clear()
    }

    fun clearUserCache(uid: String? = null) {
        if (uid == null) cachedUser = null else if (cachedUser?.id == uid) cachedUser = null
    }

    fun currentUser(): User? = cachedUser ?: run {
        val currentId = AuthSession.currentUserId() ?: return null
        cachedUser?.takeIf { it.id == currentId } ?: null
    }

    suspend fun getUserProfile(uid: String, forceRefresh: Boolean = false): User? {
        if (!forceRefresh && cachedUser != null && cachedUser?.id == uid) return cachedUser
        val response = RetrofitClient.apiService.getProfile(userId = uid)
        val user = response.body()?.data?.toUser()
        if (user != null) {
            cachedUser = user
            AuthSession.saveUser(user)
        }
        return user
    }

    suspend fun isUsernameAvailable(username: String, excludeUid: String? = null): Boolean {
        if (username.isBlank()) return true
        val users = RetrofitClient.apiService.getUsers().body()?.data.orEmpty().map { it.toUser() }
        return users.none { it.username.equals(username, ignoreCase = true) && it.id != excludeUid }
    }

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>) {
        val payload = linkedMapOf<String, Any>("user_id" to uid)
        payload.putAll(updates)
        val response = RetrofitClient.apiService.updateProfile(action = "update", body = payload)
        val user = response.body()?.data?.toUser()
        if (user != null) {
            cachedUser = user
            AuthSession.saveUser(user)
        }
    }
}
