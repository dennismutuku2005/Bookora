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

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            try {
                books.value = repo.getBooks()
                featured.value = repo.getFeaturedBooks()
            } catch (_: Exception) {
            }
        }
    }
}
