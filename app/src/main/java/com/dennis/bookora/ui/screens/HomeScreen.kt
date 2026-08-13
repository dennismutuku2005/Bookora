package com.dennis.bookora.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dennis.bookora.models.BookItem
import com.dennis.bookora.ui.components.CleanBookCard
import com.dennis.bookora.ui.components.VerticalBookCard
import com.dennis.bookora.ui.viewmodels.BooksViewModel

@Composable
fun HomeScreen(onBookClick: (String) -> Unit) {
    val vm: BooksViewModel = hiltViewModel()
    val featured = vm.featured.value
    val all = vm.books.value

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Featured Books", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(featured) { book ->
                // Map Book -> BookItem for existing UI
                val item = BookItem(book.id, book.title, book.author, book.category, book.ownerId, book.postedDate, 0, Color(0xFFF0F4FF))
                VerticalBookCard(item, onBookClick)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Nearby Listings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(12.dp))
        all.forEach { book ->
            val item = BookItem(book.id, book.title, book.author, book.category, book.ownerId, book.postedDate, 0, Color(0xFFF8F9FA))
            CleanBookCard(item, onBookClick)
        }
    }
}
