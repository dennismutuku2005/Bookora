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
import com.dennis.bookora.models.BookItem
import com.dennis.bookora.ui.components.CleanBookCard
import com.dennis.bookora.ui.components.VerticalBookCard

@Composable
fun HomeScreen(onBookClick: (String) -> Unit) {
    val books = listOf(
        BookItem("1", "Atomic Habits", "James Clear", "Science", "dennis_m", "2h ago", 128, Color(0xFFF0F4FF)),
        BookItem("2", "The Alchemist", "Paulo Coelho", "Philosophy", "sarah_k", "5h ago", 94, Color(0xFFFFF7F0)),
        BookItem("3", "Deep Work", "Cal Newport", "Coding", "mike_r", "1d ago", 76, Color(0xFFF0FFF4)),
        BookItem("4", "Principles", "Ray Dalio", "Maths", "anna_l", "3d ago", 54, Color(0xFFFFF0F0))
    )

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Featured Books", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(books) { book ->
                VerticalBookCard(book, onBookClick)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Nearby Listings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(12.dp))
        books.forEach { book ->
            CleanBookCard(book, onBookClick)
        }
    }
}
