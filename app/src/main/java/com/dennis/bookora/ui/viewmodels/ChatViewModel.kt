package com.dennis.bookora.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennis.bookora.models.Message
import com.dennis.bookora.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: BookRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val conversationId: String = savedStateHandle["conversationId"] ?: ""

    var messages = mutableStateOf<List<Message>>(emptyList())
        private set

    var isLoading = mutableStateOf(true)
        private set

    var error = mutableStateOf<String?>(null)
        private set

    var otherUserName = mutableStateOf("Chat")
        private set

    var bookTitle = mutableStateOf("")
        private set

    init {
        loadConversationInfo()
        startListeningForMessages()
    }

    private fun loadConversationInfo() {
        otherUserName.value = "Chat"
        bookTitle.value = ""
    }

    private fun startListeningForMessages() {
        viewModelScope.launch {
            while (true) {
                try {
                    val fetched = repo.getMessages(conversationId)
                    messages.value = fetched
                    isLoading.value = false
                    error.value = null
                } catch (e: Exception) {
                    error.value = e.message
                    isLoading.value = false
                }
                // Poll every 2 seconds for new messages
                delay(2000)
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                repo.sendMessage(conversationId, text.trim())
                // Refresh messages immediately after sending
                messages.value = repo.getMessages(conversationId)
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }
}
