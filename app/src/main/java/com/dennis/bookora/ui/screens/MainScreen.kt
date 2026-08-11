package com.dennis.bookora.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dennis.bookora.R

private enum class MainTab(val title: String) {
    Home("Home"),
    Search("Search"),
    Create("Create"),
    Notifications("Alerts"),
    Profile("Profile")
}

data class BookItem(
    val title: String,
    val author: String,
    val type: String, // "Exchange" or "Giveaway"
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    val (selectedTab, setSelectedTab) = remember { mutableStateOf(MainTab.Home) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when(selectedTab) {
                            MainTab.Home -> "Bookora"
                            else -> selectedTab.title
                        },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                },
                actions = {
                    if (selectedTab == MainTab.Profile) {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == selectedTab,
                        onClick = { setSelectedTab(tab) },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    MainTab.Home -> Icons.Rounded.Home
                                    MainTab.Search -> Icons.Rounded.Search
                                    MainTab.Create -> Icons.Rounded.AddCircle
                                    MainTab.Notifications -> Icons.Rounded.Notifications
                                    MainTab.Profile -> Icons.Rounded.Person
                                },
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                MainTab.Home -> HomeTabContent()
                MainTab.Search -> SearchTabContent()
                MainTab.Create -> CreateTabContent()
                MainTab.Notifications -> NotificationsTabContent()
                MainTab.Profile -> ProfileTabContent(onLogout)
            }
        }
    }
}

@Composable
private fun HomeTabContent() {
    val gummyBooks = listOf(
        BookItem("Atomic Habits", "James Clear", "Giveaway", Color(0xFFFFEBEE)),
        BookItem("The Alchemist", "Paulo Coelho", "Exchange", Color(0xFFE3F2FD)),
        BookItem("Deep Work", "Cal Newport", "Exchange", Color(0xFFF1F8E9)),
        BookItem("Psychology of Money", "Morgan Housel", "Giveaway", Color(0xFFFFF3E0))
    )

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Trending Now", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(gummyBooks) { book ->
                GummyBookCard(book)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Recently Added", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        gummyBooks.reversed().forEach { book ->
            BookHorizontalCard(book)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun GummyBookCard(book: BookItem) {
    Card(
        modifier = Modifier.size(width = 160.dp, height = 220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = book.color)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.5f), CircleShape), contentAlignment = Alignment.Center) {
                Text(if(book.type == "Exchange") "🔄" else "🎁", fontSize = 20.sp)
            }
            Column {
                Text(book.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(book.author, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                Text(book.type, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun BookHorizontalCard(book: BookItem) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(book.color), contentAlignment = Alignment.Center) {
                Text("📖", fontSize = 30.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold)
                Text(book.author, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                book.type,
                color = if(book.type == "Giveaway") Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun SearchTabContent() {
    var query by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search books, authors...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        // Simple list for search results
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(5) {
                BookHorizontalCard(BookItem("Search Result $it", "Author Name", if(it % 2 == 0) "Exchange" else "Giveaway", Color.White))
            }
        }
    }
}

@Composable
private fun CreateTabContent() {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var isExchange by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("New Listing", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Text("Add Book Photo", color = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Book Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth())
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Listing Type", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isExchange, onClick = { isExchange = true })
            Text("Exchange", modifier = Modifier.clickable { isExchange = true })
            Spacer(modifier = Modifier.width(24.dp))
            RadioButton(selected = !isExchange, onClick = { isExchange = false })
            Text("Giveaway", modifier = Modifier.clickable { isExchange = false })
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { /* Create */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Post Listing")
        }
    }
}

@Composable
private fun NotificationsTabContent() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        repeat(3) {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("New book available in your area!", fontWeight = FontWeight.Bold)
                        Text("2 hours ago", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTabContent(onLogout: () -> Unit) {
    var username by remember { mutableStateOf("dennis_readz") }
    var bio by remember { mutableStateOf("Avid reader. Loving the Bookora community!") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Text("DM", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape).padding(4.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("My Activity", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ActivityStat("12", "Listings")
                    ActivityStat("45", "Exchanges")
                    ActivityStat("8", "Given")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        TextButton(onClick = onLogout) {
            Icon(Icons.Default.Logout, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout Session", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ActivityStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
