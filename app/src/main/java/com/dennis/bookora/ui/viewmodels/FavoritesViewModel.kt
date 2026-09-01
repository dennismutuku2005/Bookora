package com.dennis.bookora.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennis.bookora.models.Book
import com.dennis.bookora.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repo: BookRepository
) : ViewModel() {

    var favorites by mutableStateOf<List<Book>>(emptyList())
    var isLoading by mutableStateOf(false)

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            isLoading = true
            try {
                favorites = repo.getFavorites()
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to load favorites: ${e.message}"))
            } finally {
                isLoading = false
            }
        }
    }
}
