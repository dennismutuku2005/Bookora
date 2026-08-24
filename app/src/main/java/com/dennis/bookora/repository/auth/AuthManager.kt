package com.dennis.bookora.repository.auth

import android.content.Context
import android.net.Uri
import com.dennis.bookora.api.ApiEnvelope
import com.dennis.bookora.api.RetrofitClient
import com.dennis.bookora.api.UserApiResponse
import com.dennis.bookora.api.toUser
import com.dennis.bookora.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response

object AuthManager {
    private var cachedUser: User? = null

    fun ensureInitialized(context: Context) {
        AuthSession.init(context)
    }

    fun getCachedUser(): User? = cachedUser

    suspend fun uploadProfileImage(context: Context, uid: String, imageUri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return imageUri.toString()
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(context.cacheDir, fileName)
            inputStream.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = okhttp3.MultipartBody.Part.createFormData("file", fileName, requestBody)
            val response = RetrofitClient.apiService.uploadImage(type = "profile", userId = uid, file = part)
            file.delete()
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data?.url ?: imageUri.toString()
            } else {
                imageUri.toString()
            }
        } catch (_: Exception) {
            imageUri.toString()
        }
    }

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
    ): Result<User> {
        return try {
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
            if (response.isSuccessful) {
                val user = response.body()?.data?.toUser()
                if (user != null) {
                    cachedUser = user
                    AuthSession.saveUser(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Registration succeeded but user data was missing"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = RetrofitClient.apiService.login(action = "login", body = mapOf(
                "email" to email,
                "password" to password
            ))
            if (response.isSuccessful) {
                val user = response.body()?.data?.toUser()
                if (user != null) {
                    cachedUser = user
                    AuthSession.saveUser(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Login succeeded but user data was missing"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> parseError(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val type = object : TypeToken<ApiEnvelope<UserApiResponse>>() {}.type
            val envelope = Gson().fromJson<ApiEnvelope<UserApiResponse>>(errorBody, type)
            envelope.message ?: "An unknown error occurred"
        } catch (e: Exception) {
            "Server error (${response.code()})"
        }
    }

    suspend fun signInWithGoogle(idToken: String): Boolean = false

    suspend fun loginWithGoogle(email: String, firstName: String, lastName: String): Result<User> {
        return try {
            val response = RetrofitClient.apiService.loginWithGoogle(
                body = mapOf(
                    "email" to email,
                    "firstName" to firstName,
                    "lastName" to lastName
                )
            )
            if (response.isSuccessful) {
                val user = response.body()?.data?.toUser()
                if (user != null) {
                    cachedUser = user
                    AuthSession.saveUser(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Google login succeeded but user data was missing"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        cachedUser = null
        AuthSession.clear()
    }

    fun clearUserCache(uid: String? = null) {
        if (uid == null) cachedUser = null else if (cachedUser?.id == uid) cachedUser = null
    }

    fun currentUser(): User? {
        if (cachedUser != null) return cachedUser
        val savedUser = AuthSession.getUser()
        if (savedUser != null) {
            cachedUser = savedUser
            return savedUser
        }
        return null
    }

    suspend fun getUserProfile(uid: String, forceRefresh: Boolean = false): User? {
        if (!forceRefresh && cachedUser != null && cachedUser?.id == uid) return cachedUser
        val response = RetrofitClient.apiService.getProfile(userId = uid)
        val user = response.body()?.data?.toUser()
        if (user != null) {
            if (uid == AuthSession.currentUserId()) {
                cachedUser = user
                AuthSession.saveUser(user)
            }
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
            if (uid == AuthSession.currentUserId()) {
                cachedUser = user
                AuthSession.saveUser(user)
            }
        }
    }
}
