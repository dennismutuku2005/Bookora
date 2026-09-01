package com.dennis.bookora.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennis.bookora.models.Book
import com.dennis.bookora.models.User
import com.dennis.bookora.repository.BookRepository
import com.dennis.bookora.repository.auth.AuthManager
import com.dennis.bookora.repository.auth.AuthSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: BookRepository
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
    var favorites by mutableStateOf<List<Book>>(emptyList())
    var isLoading by mutableStateOf(true)
    var isRefreshing by mutableStateOf(false)

    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var username by mutableStateOf("")
    var phone by mutableStateOf("")
    var bio by mutableStateOf("Book lover and exchange enthusiast")
    var avatarUrl by mutableStateOf("")
    var shareContactByEmail by mutableStateOf(true)

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    init {
        loadProfile()
    }

    fun loadProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh) isRefreshing = true else isLoading = true
            try {
                val uid = AuthManager.currentUser()?.id ?: AuthSession.currentUserId()
                if (uid != null) {
                    val profile = AuthManager.getUserProfile(uid, forceRefresh = forceRefresh)
                    currentUser = profile
                    if (profile != null) {
                        firstName = profile.firstName
                        lastName = profile.lastName
                        username = profile.username
                        phone = profile.phone
                        bio = profile.bio.ifEmpty { "Book lover and exchange enthusiast" }
                        avatarUrl = profile.avatarUrl
                        shareContactByEmail = profile.shareContactByEmail
                    }
                    favorites = repo.getFavorites()
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to load profile: ${e.message}"))
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    fun toggleFavorite(bookId: String) {
        viewModelScope.launch {
            try {
                repo.toggleFavorite(bookId)
                favorites = repo.getFavorites()
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to toggle favorite: ${e.message}"))
            }
        }
    }
}
