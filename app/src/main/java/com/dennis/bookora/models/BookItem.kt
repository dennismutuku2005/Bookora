package com.dennis.bookora.models

import androidx.compose.ui.graphics.Color

data class BookItem(
    val id: String,
    val title: String,
    val author: String,
    val type: String,
    val username: String,
    val timeAgo: String,
    val views: Int,
    val color: Color
)
