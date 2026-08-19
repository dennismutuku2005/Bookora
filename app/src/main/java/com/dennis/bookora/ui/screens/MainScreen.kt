package com.dennis.bookora.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dennis.bookora.repository.auth.AuthManager

private enum class MainTab(val title: String) {
    Home("Home"),
    Search("Search"),
    Create("Create"),
    Notifications("Alerts"),
    Profile("Profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    onBookClick: (String) -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onNotificationClick: (String) -> Unit = {},
    onMyListingsClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onChatClick: (String) -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == MainTab.Home) "Bookora" else selectedTab.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                actions = {
                    if (selectedTab == MainTab.Home) {
                        Image(
                            painter = painterResource(id = com.dennis.bookora.R.drawable.logo),
                            contentDescription = "Bookora logo",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 12.dp)
                        )
                    }
                    if (selectedTab == MainTab.Profile) {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, "Logout", tint = MaterialTheme.colorScheme.error)
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
                        onClick = { selectedTab = tab },
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
                MainTab.Home -> HomeScreen(onBookClick)
                MainTab.Search -> SearchScreen(onBookClick)
                MainTab.Create -> CreateListingScreen(
                    onSuccess = { selectedTab = MainTab.Home }
                )
                MainTab.Notifications -> NotificationsScreen(
                    onNotificationClick = onNotificationClick,
                    onChatClick = onChatClick
                )
                MainTab.Profile -> ProfileScreen(
                    onLogout = { showLogoutDialog = true },
                    onPrivacyClick = onPrivacyClick,
                    onTermsClick = onTermsClick,
                    onMyListingsClick = onMyListingsClick,
                    onFavoritesClick = onFavoritesClick
                )
            }
        }
    }

    if (showLogoutDialog) {
        Dialog(onDismissRequest = { showLogoutDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Logout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Are you sure you want to logout from this session?", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                AuthManager.logout()
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                onLogout()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Logout") }
                    }
                }
            }
        }
    }
}
