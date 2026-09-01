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

import androidx.hilt.navigation.compose.hiltViewModel
import com.dennis.bookora.ui.viewmodels.NotificationDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notificationId: String,
    onBack: () -> Unit = {},
    onOpenChat: (String) -> Unit = {},
    viewModel: NotificationDetailViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUid = AuthManager.currentUser()?.uid ?: ""

    LaunchedEffect(notificationId) {
        viewModel.loadNotification(notificationId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is NotificationDetailViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
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
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = viewModel.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = viewModel.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (viewModel.timeAgo.isNotBlank()) {
                        Text(
                            text = "Received ${viewModel.timeAgo}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider()

                    // Claim Request Details & Double-Confirmation
                    if (viewModel.claimRequest != null) {
                        val claim = viewModel.claimRequest!!
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
                                    viewModel.confirmAction(isIClaimer, isIOwner)
                                },
                                enabled = !viewModel.isConfirming &&
                                        ((isIClaimer && !claim.confirmedByClaimer) || (isIOwner && !claim.confirmedByOwner)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large
                            ) {
                                if (viewModel.isConfirming) {
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
