package com.dennis.bookora.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    onChatClick: (String) -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == MainTab.Home) "Bookora" else selectedTab.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
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
                MainTab.Create -> CreateListingScreen()
                MainTab.Notifications -> NotificationsScreen(
                    onNotificationClick = onNotificationClick,
                    onChatClick = onChatClick
                )
                MainTab.Profile -> ProfileScreen(
                    onLogout = { showLogoutDialog = true },
                    onPrivacyClick = onPrivacyClick,
                    onTermsClick = onTermsClick,
                    onMyListingsClick = onMyListingsClick
                )
            }
        }
    }

    if (showLogoutDialog) {
        Dialog(onDismissRequest = { showLogoutDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Logout", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Are you sure you want to logout from this session?", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }) { Text("Logout") }
                    }
                }
            }
        }
    }
}
