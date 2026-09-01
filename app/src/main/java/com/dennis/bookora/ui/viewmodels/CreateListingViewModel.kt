package com.dennis.bookora.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dennis.bookora.models.Book
import com.dennis.bookora.models.ListingCondition
import com.dennis.bookora.models.ListingType
import com.dennis.bookora.repository.BookRepository
import com.dennis.bookora.repository.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreateListingViewModel @Inject constructor(
    private val repo: BookRepository
) : ViewModel() {

    var title by mutableStateOf("")
    var author by mutableStateOf("")
    var category by mutableStateOf("Fiction")
    var description by mutableStateOf("")
    var location by mutableStateOf("")
    var isExchange by mutableStateOf(true)
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var existingCoverUrl by mutableStateOf("")
    var condition by mutableStateOf("Like New")
    
    var isLoading by mutableStateOf(false)
    var isPublishing by mutableStateOf(false)
    
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object Success : UiEvent()
    }

    fun clearForm() {
        title = ""
        author = ""
        category = "Fiction"
        description = ""
        location = ""
        isExchange = true
        selectedImageUri = null
        existingCoverUrl = ""
        condition = "Like New"
    }

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val existingBook = repo.getBookById(bookId)
                if (existingBook != null) {
                    title = existingBook.title
                    author = existingBook.author
                    category = existingBook.category.ifBlank { "Fiction" }
                    description = existingBook.description
                    location = existingBook.location
                    isExchange = existingBook.listingType == ListingType.EXCHANGE
                    existingCoverUrl = existingBook.coverUrl
                    condition = when (existingBook.condition) {
                        ListingCondition.NEW -> "New"
                        ListingCondition.LIKE_NEW -> "Like New"
                        ListingCondition.GOOD -> "Good"
                        ListingCondition.FAIR -> "Fair"
                    }
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("❌ Failed to load book: ${e.message}"))
            } finally {
                isLoading = false
            }
        }
    }

    fun saveListing(context: Context, bookId: String?) {
        if (title.isBlank() || author.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Please enter book title and author")) }
            return
        }

        viewModelScope.launch {
            try {
                isPublishing = true
                val user = AuthManager.currentUser() ?: run {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Please sign in to publish"))
                    return@launch
                }

                var coverUrl = existingCoverUrl

                // Upload image if selected
                if (selectedImageUri != null) {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Uploading cover photo..."))
                    coverUrl = repo.uploadBookCoverImage(context, selectedImageUri!!)
                }

                val book = Book(
                    id = bookId ?: "",
                    title = title.trim(),
                    author = author.trim(),
                    category = category,
                    description = description.trim(),
                    location = location.trim(),
                    condition = when (condition) {
                        "New" -> ListingCondition.NEW
                        "Like New" -> ListingCondition.LIKE_NEW
                        "Fair" -> ListingCondition.FAIR
                        else -> ListingCondition.GOOD
                    },
                    coverUrl = coverUrl,
                    listingType = if (isExchange) ListingType.EXCHANGE else ListingType.GIVEAWAY,
                    ownerId = user.id,
                    ownerUsername = user.username,
                    postedTimestamp = System.currentTimeMillis(),
                    postedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
                )

                if (bookId == null) {
                    repo.saveBook(book)
                    clearForm()
                    _uiEvent.emit(UiEvent.Success)
                } else {
                    repo.updateBook(bookId, book)
                    _uiEvent.emit(UiEvent.Success)
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "Publish failed"))
            } finally {
                isPublishing = false
            }
        }
    }
}
