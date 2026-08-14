package com.dennis.bookora.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennis.bookora.models.Book
import com.dennis.bookora.repository.BookRepository
import com.dennis.bookora.repository.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyListingsViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _listings = mutableStateOf<List<Book>>(emptyList())
    val listings: State<List<Book>> = _listings

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadListings()
    }

    fun loadListings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = AuthManager.currentUser()?.uid
                if (uid != null) {
                    _listings.value = repository.getMyBooks(uid)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteListing(bookId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteBook(bookId)
                _listings.value = _listings.value.filter { it.id != bookId }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Delete failed")
            }
        }
    }
}
