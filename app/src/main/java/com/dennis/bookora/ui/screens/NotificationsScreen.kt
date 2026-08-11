package com.dennis.bookora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dennis.bookora.models.Notification

@Composable
fun NotificationsScreen() {
    val notifications = listOf(
        Notification("1", "New listing nearby", "Someone just posted 'The Great Gatsby' near you.", "1h ago", false),
        Notification("2", "Book request accepted", "Your exchange request was approved.", "3h ago", true),
        Notification("3", "Reminder", "Don't forget to confirm the pickup time.", "5h ago", false),
        Notification("4", "New message", "You have a new message from reader_jane.", "7h ago", true)
    )

    var selectedNotification by remember { mutableStateOf<Notification?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Alerts",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        notifications.forEach { notification ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { selectedNotification = notification },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Notifications,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notification.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(notification.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(notification.timeAgo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (selectedNotification != null) {
        AlertDialog(
            onDismissRequest = { selectedNotification = null },
            confirmButton = {
                TextButton(onClick = { selectedNotification = null }) {
                    Text("Close")
                }
            },
            title = {
                Text(selectedNotification?.title.orEmpty(), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(selectedNotification?.subtitle.orEmpty())
            }
        )
    }
}
