package com.dennis.bookora.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    onTermsClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(MainTab.Home) }

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
                    if (selectedTab == MainTab.Profile) {
                        IconButton(onClick = onLogout) {
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
                MainTab.Notifications -> NotificationsScreen()
                MainTab.Profile -> ProfileScreen(onLogout, onPrivacyClick, onTermsClick)
            }
        }
    }
}
