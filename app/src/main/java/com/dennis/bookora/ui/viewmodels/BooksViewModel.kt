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

    var error = mutableStateOf<String?>(null)
        private set

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                val fetched = repo.getBooks()
                // get user's favorites
                val favs = try { repo.getFavorites() } catch (_: Exception) { emptyList() }
                val favIds = favs.map { it.id }.toSet()

                // enrich owner usernames when missing and mark favorites
                val enriched = fetched.map { b ->
                    val withOwner = if (b.ownerUsername.isBlank() && b.ownerId.isNotBlank()) {
                        try {
                            val profile = repo.getUserProfile(b.ownerId)
                            if (profile != null) b.copy(ownerUsername = profile.username) else b
                        } catch (_: Exception) { b }
                    } else b
                    if (favIds.contains(withOwner.id)) withOwner.copy(isFavorite = true) else withOwner.copy(isFavorite = false)
                }
                books.value = enriched
                val fetchedFeatured = repo.getFeaturedBooks()
                featured.value = fetchedFeatured.map { it.copy(isFavorite = favIds.contains(it.id)) }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load books"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun toggleFavorite(bookId: String) {
        viewModelScope.launch {
            // determine current state
            val inBooks = books.value.firstOrNull { it.id == bookId }
            val wasFav = inBooks?.isFavorite ?: featured.value.firstOrNull { it.id == bookId }?.isFavorite ?: false
            // optimistic UI
            books.value = books.value.map { if (it.id == bookId) it.copy(isFavorite = !it.isFavorite) else it }
            featured.value = featured.value.map { if (it.id == bookId) it.copy(isFavorite = !it.isFavorite) else it }

            try {
                repo.toggleFavorite(bookId)
                // refresh favorites from backend to ensure consistency
                val favs = try { repo.getFavorites() } catch (_: Exception) { emptyList() }
                val favIds = favs.map { it.id }.toSet()
                books.value = books.value.map { it.copy(isFavorite = favIds.contains(it.id)) }
                featured.value = featured.value.map { it.copy(isFavorite = favIds.contains(it.id)) }
            } catch (_: Exception) {
                // revert optimistic change on failure
                books.value = books.value.map { if (it.id == bookId) it.copy(isFavorite = wasFav) else it }
                featured.value = featured.value.map { if (it.id == bookId) it.copy(isFavorite = wasFav) else it }
            }
        }
    }
}
