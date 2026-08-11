package com.dennis.bookora.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dennis.bookora.models.Notification
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notificationId: String,
    onBack: () -> Unit = {},
) {
    val notification = notificationFromId(notificationId)
    var allowed by remember { mutableStateOf(value = false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Notification") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                notification.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                notification.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Received ${notification.timeAgo}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider()

            Text(
                notificationActionMessage(notification),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            if (notification.title.contains("message", ignoreCase = true)) {
                Button(
                    onClick = { /* open chat */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Message")
                }
            } else if (notification.title.contains("request", ignoreCase = true)
                || notification.title.contains("wants", ignoreCase = true)
                || notification.title.contains("exchange", ignoreCase = true)
            ) {
                if (!allowed) {
                    Button(
                        onClick = {
                            allowed = true
                            scope.launch {
                                snackbarHostState.showSnackbar("Contact allowed — contact info shared")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Allow Contact")
                    }
                } else {
                    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Contact information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Email: reader_owner@example.com", style = MaterialTheme.typography.bodyLarge)
                            Text("Phone: +254 700 000 000", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            } else {
                Button(
                    onClick = { /* perform action */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Details")
                }
            }
        }
    }
}

private fun notificationFromId(id: String): Notification {
    return when (id) {
        "1" -> Notification("1", "New listing nearby", "Someone just posted 'The Great Gatsby' near you.", "1h ago", isRead = false)
        "2" -> Notification("2", "Book request accepted", "Your exchange request was approved.", "3h ago", isRead = true)
        "3" -> Notification("3", "Reminder", "Don't forget to confirm the pickup time.", "5h ago", isRead = false)
        "4" -> Notification("4", "New message", "You have a new message from reader_jane.", "7h ago", isRead = true)
        else -> Notification(id, "Notification", "No details available.", "now", isRead = false)
    }
}

private fun notificationActionMessage(notification: Notification): String {
    return when {
        notification.title.contains("listing", ignoreCase = true) ->
            "See the listing details or confirm the pickup."
        notification.title.contains("request accepted", ignoreCase = true) ->
            "Your request has been approved. Tap below to message the owner and finalize the exchange."
        notification.title.contains("reminder", ignoreCase = true) ->
            "This is your reminder to confirm the pickup details and let the other reader know you're ready."
        notification.title.contains("message", ignoreCase = true) ->
            "Open the chat to reply to reader_jane and continue the conversation."
        else ->
            "View the notification action and take the next step."
    }
}
