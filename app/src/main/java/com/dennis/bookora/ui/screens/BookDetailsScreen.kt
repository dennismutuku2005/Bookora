package com.dennis.bookora.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dennis.bookora.R
import com.dennis.bookora.models.ListingType
import com.dennis.bookora.ui.viewmodels.BookDetailViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    bookId: String,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit = {}
) {
    val vm: BookDetailViewModel = hiltViewModel()
    val book by vm.book
    val ownerUser by vm.ownerUser
    val isOwner by vm.isOwner
    val isLoading by vm.isLoading
    val error by vm.error
    val isClaiming by vm.isClaiming

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showContactDialog by remember { mutableStateOf(false) }

    val formattedDate = remember(book?.postedDate, book?.postedTimestamp) {
        val b = book ?: return@remember ""
        if (b.postedTimestamp > 0) {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(b.postedTimestamp))
        } else if (b.postedDate.isNotBlank()) {
            try {
                if (b.postedDate.contains("T")) {
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    val date = parser.parse(b.postedDate)
                    if (date != null) SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date) else b.postedDate
                } else {
                    b.postedDate
                }
            } catch (_: Exception) {
                b.postedDate
            }
        } else {
            "Recently"
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Book Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var isFav by remember { mutableStateOf(false) }
                    IconButton(onClick = {
                        isFav = !isFav
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isFav) "Added to favorites" else "Removed from favorites"
                            )
                        }
                    }) {
                        Icon(
                            if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Favorite",
                            tint = if (isFav) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        scope.launch { snackbarHostState.showSnackbar("Share link copied") }
                    }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            )
        },
        bottomBar = {
            if (book != null) {
                Surface(
                    tonalElevation = 12.dp,
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isOwner) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "📖 You are the owner of this listing",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            // Message button
                            OutlinedButton(
                                onClick = {
                                    vm.startChatWithOwner(
                                        onConversationReady = { convoId -> onOpenChat(convoId) },
                                        onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Message")
                            }

                            // Claim button
                            Button(
                                onClick = {
                                    vm.claimBook(
                                        onSuccess = { claim, convoId ->
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Request sent! Private chat opened.",
                                                    actionLabel = "Open Chat"
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    onOpenChat(convoId)
                                                }
                                            }
                                        },
                                        onError = { err ->
                                            scope.launch { snackbarHostState.showSnackbar(err) }
                                        }
                                    )
                                },
                                enabled = !isClaiming,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (isClaiming) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (book!!.listingType == ListingType.GIVEAWAY) "Claim Now" else "Request Exchange")
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "⚠️ Could not load book",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { vm.fetchBook() }) {
                            Text("Retry")
                        }
                    }
                }

                book != null -> {
                    val b = book!!
                    val ownerDisplayName = ownerUser?.fullName?.ifBlank { ownerUser?.username }
                        ?: b.ownerUsername.ifBlank { "Bookora Member" }
                    val ownerAvatar = ownerUser?.avatarUrl ?: ""

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Cover Header Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (b.coverUrl.isNotBlank()) {
                                AsyncImage(
                                    model = b.coverUrl,
                                    contentDescription = b.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp),
                                    alpha = 0.4f
                                )
                            }

                            // Listing Type Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (b.listingType == ListingType.GIVEAWAY) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = b.listingType.name,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(24.dp)) {
                            // Title and Author
                            Text(
                                text = b.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "by ${b.author}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Key Info Cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                InfoChip(
                                    label = "Condition",
                                    value = b.condition.name.replace("_", " "),
                                    icon = Icons.Default.ThumbUp,
                                    modifier = Modifier.weight(1f)
                                )
                                InfoChip(
                                    label = "Category",
                                    value = b.category.ifBlank { "General" },
                                    icon = Icons.Default.Category,
                                    modifier = Modifier.weight(1f)
                                )
                                InfoChip(
                                    label = "Posted",
                                    value = formattedDate,
                                    icon = Icons.Default.DateRange,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Spacer(modifier = Modifier.height(24.dp))

                            // About Section
                            Text(
                                text = "About this book",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = b.description.ifBlank { "No description provided." },
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Spacer(modifier = Modifier.height(24.dp))

                            // Owner Card
                            Text(
                                text = "Listed by",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (ownerAvatar.isNotBlank()) {
                                        AsyncImage(
                                            model = ownerAvatar,
                                            contentDescription = ownerDisplayName,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ownerDisplayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ownerDisplayName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        if (ownerUser?.username?.isNotBlank() == true) {
                                            Text(
                                                text = "@${ownerUser?.username}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                null,
                                                modifier = Modifier.size(13.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = b.location.ifBlank { "Location not specified" },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Share Contact Button
                                    if (!isOwner && ownerUser != null) {
                                        IconButton(
                                            onClick = { showContactDialog = true },
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                        ) {
                                            Icon(
                                                Icons.Default.ContactPhone,
                                                contentDescription = "Contact Info",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }

    // Contact Dialog
    if (showContactDialog && ownerUser != null) {
        val u = ownerUser!!
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = {
                Text(
                    text = "Owner Contact Info",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Name: ${u.fullName.ifBlank { u.username }}", style = MaterialTheme.typography.bodyMedium)
                    if (u.email.isNotBlank()) {
                        Text(text = "Email: ${u.email}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (u.phone.isNotBlank()) {
                        Text(text = "Phone: ${u.phone}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun InfoChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
