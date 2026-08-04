package com.dennis.bookora.models

import java.time.Instant

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isMine: Boolean
)
