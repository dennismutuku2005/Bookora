package com.dennis.bookora.models

data class Notification(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeAgo: String,
    val isRead: Boolean = false
)
