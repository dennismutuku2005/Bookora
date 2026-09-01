package com.dennis.bookora.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennis.bookora.models.Book
import com.dennis.bookora.models.ClaimRequest
import com.dennis.bookora.models.User
import com.dennis.bookora.repository.BookRepository
import com.dennis.bookora.repository.auth.AuthManager
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
                val currentUid = AuthManager.currentUser()?.uid
                val b = repo.getBookById(bookId)
                book.value = b
                if (b == null) {
                    error.value = "Book not found."
                } else {
                    isOwner.value = (currentUid != null && currentUid == b.ownerId)
                    // Stop screen loading immediately as soon as book is fetched!
                    isLoading.value = false

                    // Fetch owner profile asynchronously in background so UI is instant
                    if (b.ownerId.isNotBlank()) {
                        launch {
                            try {
                                ownerUser.value = repo.getUserProfile(b.ownerId)
                            } catch (_: Exception) {}
                        }
                    }

                    // Fetch existing claim requests if not owner
                    if (currentUid != null && currentUid != b.ownerId) {
                        launch {
                            try {
                                val claims = repo.getMyClaims(currentUid, "claimer")
                                val existingClaim = claims.find { it.bookId == bookId }
                                claimState.value = existingClaim
                            } catch (_: Exception) {}
                        }
                    }
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load book details."
            } finally {
                isLoading.value = false
            }
        }
    }

    fun claimBook(onSuccess: (ClaimRequest, String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val b = book.value ?: return@launch
            isClaiming.value = true
            try {
                val request = repo.claimBook(b.id, b.title, b.ownerId)
                claimState.value = request

                // Auto-create/open private chat and post introduction message
                val owner = ownerUser.value
                val ownerName = owner?.fullName?.ifBlank { owner.username } ?: b.ownerUsername.ifBlank { "Owner" }
                val conversationId = repo.getOrCreateConversation(
                    bookId = b.id,
                    bookTitle = b.title,
                    otherUserId = b.ownerId,
                    otherUserName = ownerName
                )
                repo.sendMessage(
                    conversationId = conversationId,
                    text = "👋 Hi! I submitted a claim request for \"${b.title}\". Let's connect for pickup!"
                )

                onSuccess(request, conversationId)
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
            val ownerName = owner?.fullName?.ifBlank { owner.username } ?: b.ownerUsername.ifBlank { "Owner" }
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
