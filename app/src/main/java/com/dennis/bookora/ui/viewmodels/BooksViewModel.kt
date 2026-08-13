package com.dennis.bookora.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennis.bookora.models.Book
import com.dennis.bookora.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val repo: BookRepository
): ViewModel() {
    var books = mutableStateOf<List<Book>>(emptyList())
        private set

    var featured = mutableStateOf<List<Book>>(emptyList())
        private set

    var isLoading = mutableStateOf(true)
        private set

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val fetched = repo.getBooks()
                // enrich owner usernames when missing
                val enriched = fetched.map { b ->
                    if (b.ownerUsername.isBlank() && b.ownerId.isNotBlank()) {
                        try {
                            val profile = repo.getUserProfile(b.ownerId)
                            if (profile != null) b.copy(ownerUsername = profile.username) else b
                        } catch (_: Exception) { b }
                    } else b
                }
                books.value = enriched
                val fetchedFeatured = repo.getFeaturedBooks()
                featured.value = fetchedFeatured
            } catch (_: Exception) {
            } finally {
                isLoading.value = false
            }
        }
    }
}
