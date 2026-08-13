package com.dennis.bookora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dennis.bookora.ui.components.CleanBookCard
import com.dennis.bookora.ui.components.VerticalBookCard
import com.dennis.bookora.ui.viewmodels.BooksViewModel

@Composable
fun HomeScreen(onBookClick: (String) -> Unit) {
    val vm: BooksViewModel = hiltViewModel()
    val featured = vm.featured.value
    val all = vm.books.value
    val isLoading by vm.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (isLoading) {
            HomeSkeleton()
        } else if (all.isEmpty() && featured.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "No books listed yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Be the first to list a book and share with the community!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { vm.reload() }) {
                        Text("Refresh")
                    }
                }
            }
        } else {
            if (featured.isNotEmpty()) {
                Text(
                    "Featured Books",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(featured) { book ->
                        VerticalBookCard(book, onBookClick)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (all.isNotEmpty()) {
                Text(
                    "Nearby Listings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                all.forEach { book ->
                    CleanBookCard(book, onBookClick)
                }
            }
        }
    }
}

@Composable
fun HomeSkeleton() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(160.dp, 260.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.height(16.dp))
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
        }
    }
}
