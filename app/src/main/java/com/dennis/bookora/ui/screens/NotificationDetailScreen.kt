package com.dennis.bookora.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dennis.bookora.models.ClaimRequest
import com.dennis.bookora.models.ClaimStatus
import com.dennis.bookora.repository.ApiBookRepository
import com.dennis.bookora.repository.auth.AuthManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notificationId: String,
    onBack: () -> Unit = {},
    onOpenChat: (String) -> Unit = {}
) {
    var title by remember { mutableStateOf("Notification Details") }
    var subtitle by remember { mutableStateOf("") }
    var timeAgo by remember { mutableStateOf("") }
    var claimRequestId by remember { mutableStateOf("") }
    var bookId by remember { mutableStateOf("") }
    var senderId by remember { mutableStateOf("") }
    var conversationId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var claimRequest by remember { mutableStateOf<ClaimRequest?>(null) }
    var isConfirming by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currentUid = AuthManager.currentUser()?.uid ?: ""

    LaunchedEffect(notificationId) {
        try {
            val repo = ApiBookRepository()
            val notifications = repo.getNotifications()
            val notification = notifications.find { it.id == notificationId }

            if (notification != null) {
                title = notification.title
                subtitle = notification.subtitle
                timeAgo = notification.timeAgo
                claimRequestId = notification.claimRequestId
                bookId = notification.bookId
                senderId = notification.senderId
                conversationId = notification.conversationId

                if (claimRequestId.isNotBlank()) {
                    claimRequest = repo.getClaimRequest(claimRequestId)
                }
            }
        } catch (_: Exception) {
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Alert Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (timeAgo.isNotBlank()) {
                        Text(
                            text = "Received $timeAgo",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider()

                    // Claim Request Details & Double-Confirmation
                    if (claimRequest != null) {
                        val claim = claimRequest!!
                        val isIClaimer = (currentUid == claim.claimerId)
                        val isIOwner = (currentUid == claim.ownerId)

                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Claim Request Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Book: ${claim.bookTitle}", style = MaterialTheme.typography.bodyMedium)
                                Text("Claimer: ${claim.claimerName}", style = MaterialTheme.typography.bodyMedium)
                                Text("Owner: ${claim.ownerName}", style = MaterialTheme.typography.bodyMedium)
                                if (claim.claimerEmail.isNotBlank()) Text("Claimer Email: ${claim.claimerEmail}", style = MaterialTheme.typography.bodySmall)
                                if (claim.claimerPhone.isNotBlank()) Text("Claimer Phone: ${claim.claimerPhone}", style = MaterialTheme.typography.bodySmall)

                                Spacer(modifier = Modifier.height(4.dp))

                                val statusText = when (claim.status) {
                                    ClaimStatus.PENDING -> "Status: Pending Confirmation"
                                    ClaimStatus.CONFIRMED_CLAIMER -> "Status: Claimer confirmed pickup"
                                    ClaimStatus.CONFIRMED_OWNER -> "Status: Owner confirmed handed over"
                                    ClaimStatus.COMPLETED -> "Status: ✅ Completed & Confirmed by Both!"
                                    else -> "Status: ${claim.status.name}"
                                }
                                Text(statusText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (claim.status != ClaimStatus.COMPLETED) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isConfirming = true
                                        try {
                                            val repo = ApiBookRepository()
                                            if (isIClaimer) {
                                                repo.confirmBookReceived(claim.id)
                                            }
                                            if (isIOwner) {
                                                repo.confirmBookShared(claim.id)
                                            }

                                            // Refresh
                                            claimRequest = repo.getClaimRequest(claim.id)
                                            
                                            if (claimRequest?.status == ClaimStatus.COMPLETED) {
                                                snackbarHostState.showSnackbar("Both confirmed! Book exchange completed.")
                                            } else {
                                                snackbarHostState.showSnackbar("You confirmed! Waiting for the other party.")
                                            }
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar(e.message ?: "Error confirming")
                                        } finally {
                                            isConfirming = false
                                        }
                                    }
                                },
                                enabled = !isConfirming &&
                                        ((isIClaimer && !claim.confirmedByClaimer) || (isIOwner && !claim.confirmedByOwner)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large
                            ) {
                                if (isConfirming) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (isIClaimer) {
                                            if (claim.confirmedByClaimer) "You Have Confirmed Pickup" else "Confirm — I Received The Book"
                                        } else {
                                            if (claim.confirmedByOwner) "You Have Confirmed Handover" else "Confirm — I Shared The Book"
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
