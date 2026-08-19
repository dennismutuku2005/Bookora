package com.dennis.bookora.repository.auth

import android.content.Context
import android.content.SharedPreferences
import com.dennis.bookora.models.User
import com.google.gson.Gson

object AuthSession {
    private const val PREFS_NAME = "bookora_auth"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER = "user_json"
    private const val KEY_AUTH_TOKEN = "auth_token"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun currentUserId(): String? = if (::prefs.isInitialized) prefs.getString(KEY_USER_ID, null) else null

    fun getAuthToken(): String? = if (::prefs.isInitialized) prefs.getString(KEY_AUTH_TOKEN, null) else null

    fun isLoggedIn(): Boolean = !currentUserId().isNullOrBlank()

    fun getUser(): User? {
        if (!::prefs.isInitialized) return null
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun saveUser(user: User, token: String? = null) {
        if (!::prefs.isInitialized) return
        val authToken = token ?: "token_${user.id}_${System.currentTimeMillis()}"
        val json = gson.toJson(user)
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER, json)
            .putString(KEY_AUTH_TOKEN, authToken)
            .apply()
    }

    fun saveAuthToken(token: String) {
        if (!::prefs.isInitialized) return
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun clear() {
        if (!::prefs.isInitialized) return
        prefs.edit().clear().apply()
    }
}
