package com.dennis.bookora.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennis.bookora.models.Book
import com.dennis.bookora.models.ClaimRequest
import com.dennis.bookora.models.User
import com.dennis.bookora.repository.BookRepository
import com.dennis.bookora.repository.auth.FirebaseAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val repo: BookRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val bookId: String = savedStateHandle["bookId"] ?: ""

    var book = mutableStateOf<Book?>(null)
        private set

    var ownerUser = mutableStateOf<User?>(null)
        private set

    var isOwner = mutableStateOf(false)
        private set

    var isLoading = mutableStateOf(true)
        private set

    var error = mutableStateOf<String?>(null)
        private set

    var claimState = mutableStateOf<ClaimRequest?>(null)
        private set

    var isClaiming = mutableStateOf(false)
        private set

    init {
        fetchBook()
    }

    fun fetchBook() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                val currentUid = FirebaseAuthManager.currentUser()?.uid
                val b = repo.getBookById(bookId)
                book.value = b
                if (b == null) {
                    error.value = "Book not found."
                } else {
                    isOwner.value = (currentUid != null && currentUid == b.ownerId)
                    if (b.ownerId.isNotBlank()) {
                        ownerUser.value = repo.getUserProfile(b.ownerId)
                    }
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load book details."
            } finally {
                isLoading.value = false
            }
        }
    }

    fun claimBook(onSuccess: (ClaimRequest) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val b = book.value ?: return@launch
            isClaiming.value = true
            try {
                val request = repo.claimBook(b.id, b.title, b.ownerId)
                claimState.value = request
                onSuccess(request)
            } catch (e: Exception) {
                onError(e.message ?: "Claim request failed")
            } finally {
                isClaiming.value = false
            }
        }
    }

    fun startChatWithOwner(onConversationReady: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val b = book.value ?: return@launch
            val owner = ownerUser.value
            val ownerName = owner?.fullName?.ifBlank { owner.username } ?: "Owner"
            try {
                val conversationId = repo.getOrCreateConversation(
                    bookId = b.id,
                    bookTitle = b.title,
                    otherUserId = b.ownerId,
                    otherUserName = ownerName
                )
                onConversationReady(conversationId)
            } catch (e: Exception) {
                onError(e.message ?: "Could not open chat")
            }
        }
    }
}
