package com.dennis.bookora.models

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val avatarUrl: String,
    val memberSince: String,
    val rating: Double,
    val booksPosted: Int,
    val booksShared: Int,
    val favoritesCount: Int,
    val bio: String = ""
) {
    val fullName: String get() = "$firstName $lastName"
}
