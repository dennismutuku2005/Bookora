package com.dennis.bookora.repository.auth

import android.content.Context
import android.content.SharedPreferences
import com.dennis.bookora.models.User

object AuthSession {
    private const val PREFS_NAME = "bookora_auth"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER = "user_json"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun currentUserId(): String? = if (::prefs.isInitialized) prefs.getString(KEY_USER_ID, null) else null

    fun isLoggedIn(): Boolean = !currentUserId().isNullOrBlank()

    fun saveUser(user: User) {
        if (!::prefs.isInitialized) return
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER, user.toString())
            .apply()
    }

    fun clear() {
        if (!::prefs.isInitialized) return
        prefs.edit().clear().apply()
    }
}
