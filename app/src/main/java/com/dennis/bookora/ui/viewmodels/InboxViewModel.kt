package com.dennis.bookora.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennis.bookora.models.ChatConversation
import com.dennis.bookora.models.Notification
import com.dennis.bookora.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val repo: BookRepository
) : ViewModel() {

    var conversations = mutableStateOf<List<ChatConversation>>(emptyList())
        private set

    var notifications = mutableStateOf<List<Notification>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var error = mutableStateOf<String?>(null)
        private set

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                conversations.value = repo.getConversations()
                notifications.value = repo.getNotifications()
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load data"
            } finally {
                isLoading.value = false
            }
        }
    }
}
