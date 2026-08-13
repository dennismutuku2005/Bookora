package com.dennis.bookora.models

data class Notification(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val timeAgo: String = "",
    val isRead: Boolean = false,
    val type: String = "notification", // "notification" | "claim"
    val conversationId: String = "",
    val senderId: String = "",
    val bookId: String = "",
    val claimRequestId: String = "",
    val timestamp: Long = 0L
)
