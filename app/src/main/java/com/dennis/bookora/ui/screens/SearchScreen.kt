package com.dennis.bookora.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dennis.bookora.models.BookItem
import com.dennis.bookora.ui.components.CleanBookCard

@Composable
fun SearchScreen(onBookClick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
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
            items(5) {
                CleanBookCard(
                    BookItem(it.toString(), "Search Result $it", "Author Name", if(it % 2 == 0) "Exchange" else "Giveaway", "user_$it", "${it+1}h ago", 20 + it * 5, Color(0xFFF8F9FA)),
                    onBookClick
                )
            }
        }
    }
}
