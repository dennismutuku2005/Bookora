package com.dennis.bookora.models

data class ChatConversation(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val bookId: String = "",
    val bookTitle: String = "",
    val unreadCount: Int = 0
)
