package com.dennis.bookora.models

data class Favorite(
    val id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val timestamp: Long = 0L
)
