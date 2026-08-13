package com.dennis.bookora.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.dennis.bookora.ui.viewmodels.BooksViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import com.dennis.bookora.models.BookItem
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dennis.bookora.ui.components.CleanBookCard

@Composable
fun SearchScreen(onBookClick: (String) -> Unit, vm: BooksViewModel = hiltViewModel()) {
    var query by remember { mutableStateOf("") }
    val books = vm.books.value
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search books, authors...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val results = if (query.isBlank()) books else books.filter { it.title.contains(query, true) || it.author.contains(query, true) }
            items(results) { book ->
                val item = BookItem(book.id, book.title, book.author, book.category, book.ownerId, book.postedDate, 0, Color(0xFFF8F9FA))
                CleanBookCard(item, onBookClick)
            }
        }
    }
}
