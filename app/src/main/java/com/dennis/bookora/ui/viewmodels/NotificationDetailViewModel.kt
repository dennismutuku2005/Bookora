package com.dennis.bookora.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennis.bookora.models.ClaimRequest
import com.dennis.bookora.repository.BookRepository
import com.dennis.bookora.repository.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationDetailViewModel @Inject constructor(
    private val repo: BookRepository
) : ViewModel() {

    var title by mutableStateOf("Notification Details")
    var subtitle by mutableStateOf("")
    var timeAgo by mutableStateOf("")
    var claimRequestId by mutableStateOf("")
    var bookId by mutableStateOf("")
    var senderId by mutableStateOf("")
    var conversationId by mutableStateOf("")
    var isLoading by mutableStateOf(true)

    var claimRequest by mutableStateOf<ClaimRequest?>(null)
    var isConfirming by mutableStateOf(false)

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    fun loadNotification(notificationId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val notifications = repo.getNotifications()
                val notification = notifications.find { it.id == notificationId }

                if (notification != null) {
                    title = notification.title
                    subtitle = notification.subtitle
                    timeAgo = notification.timeAgo
                    claimRequestId = notification.claimRequestId
                    bookId = notification.bookId
                    senderId = notification.senderId
                    conversationId = notification.conversationId

                    if (claimRequestId.isNotBlank()) {
                        claimRequest = repo.getClaimRequest(claimRequestId)
                    }
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to load notification: ${e.message}"))
            } finally {
                isLoading = false
            }
        }
    }

    fun confirmAction(isIClaimer: Boolean, isIOwner: Boolean) {
        val claimId = claimRequest?.id ?: return
        viewModelScope.launch {
            isConfirming = true
            try {
                if (isIClaimer) {
                    repo.confirmBookReceived(claimId)
                }
                if (isIOwner) {
                    repo.confirmBookShared(claimId)
                }

                // Refresh
                val updated = repo.getClaimRequest(claimId)
                claimRequest = updated
                
                if (updated?.confirmedByClaimer == true && updated.confirmedByOwner) {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Both confirmed! Book exchange completed."))
                } else {
                    _uiEvent.emit(UiEvent.ShowSnackbar("You confirmed! Waiting for the other party."))
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "Error confirming"))
            } finally {
                isConfirming = false
            }
        }
    }
}
