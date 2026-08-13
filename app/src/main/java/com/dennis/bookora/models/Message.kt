package com.dennis.bookora.models

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val isMine: Boolean = false,
    val read: Boolean = false
)
