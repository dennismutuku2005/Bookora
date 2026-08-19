package com.dennis.bookora.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import coil.compose.AsyncImage
import com.dennis.bookora.BuildConfig
import com.dennis.bookora.models.User
import com.dennis.bookora.models.Book
import com.dennis.bookora.repository.auth.AuthManager
import com.dennis.bookora.repository.auth.AuthSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onMyListingsClick: () -> Unit,
    onFavoritesClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val initialCachedUser = remember { AuthManager.currentUser() }
    var isLoading by remember { mutableStateOf(initialCachedUser == null) }
    var isSaving by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    var currentUser by remember { mutableStateOf<User?>(initialCachedUser) }
    var firstName by rememberSaveable { mutableStateOf(initialCachedUser?.firstName ?: "") }
    var lastName by rememberSaveable { mutableStateOf(initialCachedUser?.lastName ?: "") }
    var username by rememberSaveable { mutableStateOf(initialCachedUser?.username ?: "") }
    var phone by rememberSaveable { mutableStateOf(initialCachedUser?.phone ?: "") }
    var bio by rememberSaveable { mutableStateOf(initialCachedUser?.bio?.ifEmpty { "Book lover and exchange enthusiast 📚" } ?: "") }
    var avatarUrl by rememberSaveable { mutableStateOf(initialCachedUser?.avatarUrl ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var shareContactByEmail by remember { mutableStateOf(initialCachedUser?.shareContactByEmail ?: true) }
    var favorites by remember { mutableStateOf<List<Book>>(emptyList()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    var usernameStatus by remember { mutableStateOf(UsernameStatus.IDLE) }

    LaunchedEffect(Unit) {
        try {
            AuthManager.ensureInitialized(context)
            val uid = AuthManager.currentUser()?.uid
            if (uid != null) {
                val profile = kotlinx.coroutines.withTimeoutOrNull(3000L) {
                    AuthManager.getUserProfile(uid)
                } ?: (initialCachedUser ?: run {
                    val user = AuthManager.currentUser()
                    val names = user?.displayName?.split(" ") ?: emptyList()
                    User(
                        id = uid,
                        firstName = names.getOrNull(0) ?: "Book",
                        lastName = names.drop(1).joinToString(" ").ifBlank { "Reader" },
                        username = user?.email?.substringBefore("@") ?: "reader",
                        email = user?.email ?: "",
                        phone = "",
                        avatarUrl = user?.avatarUrl ?: "",
                        memberSince = "",
                        rating = 0.0,
                        booksPosted = 0,
                        booksShared = 0,
                        favoritesCount = 0,
                        bio = "Book lover and exchange enthusiast 📚"
                    )
                })
                currentUser = profile
                firstName = profile.firstName
                lastName = profile.lastName
                username = profile.username
                phone = profile.phone
                bio = profile.bio.ifEmpty { "Book lover and exchange enthusiast 📚" }
                avatarUrl = profile.avatarUrl
                shareContactByEmail = profile.shareContactByEmail
            }
        } catch (_: Exception) {
        } finally {
            isLoading = false
        }
    }

    // load favorites when profile available
    LaunchedEffect(currentUser?.id) {
        val uid = currentUser?.id ?: return@LaunchedEffect
        try {
            val repo = com.dennis.bookora.repository.ApiBookRepository()
            favorites = repo.getFavorites()
        } catch (_: Exception) {}
    }

    LaunchedEffect(username, isEditing) {
        if (!isEditing) return@LaunchedEffect
        if (username.isBlank() || username == currentUser?.username) {
            usernameStatus = UsernameStatus.IDLE
            return@LaunchedEffect
        }
        usernameStatus = UsernameStatus.CHECKING
        delay(450)
        try {
            val uid = AuthManager.currentUser()?.uid
            val available = uid != null && AuthManager.isUsernameAvailable(username, uid)
            usernameStatus = if (available) UsernameStatus.AVAILABLE else UsernameStatus.TAKEN
        } catch (e: Exception) {
            usernameStatus = UsernameStatus.IDLE
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    fun refreshProfile() {
        scope.launch {
            try {
                isRefreshing = true
                val uid = AuthManager.currentUser()?.id ?: AuthSession.currentUserId()
                if (uid != null) {
                    val profile = AuthManager.getUserProfile(uid, forceRefresh = true)
                    if (profile != null) {
                        currentUser = profile
                        firstName = profile.firstName
                        lastName = profile.lastName
                        username = profile.username
                        phone = profile.phone
                        bio = profile.bio.ifEmpty { "Book lover and exchange enthusiast" }
                        avatarUrl = profile.avatarUrl
                        shareContactByEmail = profile.shareContactByEmail
                    }
                    val repo = com.dennis.bookora.repository.ApiBookRepository()
                    favorites = repo.getFavorites()
                }
            } catch (_: Exception) {
            } finally {
                isRefreshing = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { refreshProfile() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ---------- Header ----------
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(
                        initial = if (firstName.isNotEmpty()) firstName.first().uppercase() else "?",
                        imageUrl = avatarUrl,
                        selectedUri = selectedImageUri,
                        editable = isEditing,
                        isSaving = isSaving,
                        onClick = { if (isEditing) photoPickerLauncher.launch("image/*") }
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(count = (currentUser?.booksPosted ?: 0).toString(), label = "Posts")
                        StatItem(count = (currentUser?.booksShared ?: 0).toString(), label = "Exchanges")
                        StatItem(count = (currentUser?.favoritesCount ?: 0).toString(), label = "Favorites")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isEditing) {
                    Text(
                        text = if (firstName.isNotEmpty() || lastName.isNotEmpty())
                            "$firstName $lastName".trim() else "Add your name",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "@$username",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = bio,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text("Edit Profile", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            }

            // ---------- Edit form ----------
            AnimatedVisibility(
                visible = isEditing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 8.dp)
                ) {
                    ProfileTextField(
                        label = "First name",
                        value = firstName,
                        onValueChange = { firstName = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ProfileTextField(
                        label = "Last name",
                        value = lastName,
                        onValueChange = { lastName = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    UsernameField(
                        value = username,
                        onValueChange = { username = it.trim() },
                        status = usernameStatus
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileTextField(
                        label = "Phone",
                        value = phone,
                        onValueChange = { phone = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileTextField(
                        label = "Bio",
                        value = bio,
                        onValueChange = { if (it.length <= 150) bio = it },
                        singleLine = false,
                        minLines = 3,
                        maxLines = 5,
                        supporting = "${bio.length}/150"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                firstName = currentUser?.firstName ?: ""
                                lastName = currentUser?.lastName ?: ""
                                username = currentUser?.username ?: ""
                                phone = currentUser?.phone ?: ""
                                bio = currentUser?.bio ?: ""
                                avatarUrl = currentUser?.avatarUrl ?: ""
                                shareContactByEmail = currentUser?.shareContactByEmail ?: true
                                selectedImageUri = null
                                usernameStatus = UsernameStatus.IDLE
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                if (usernameStatus == UsernameStatus.TAKEN) {
                                    scope.launch { snackbarHostState.showSnackbar("Username already taken") }
                                    return@Button
                                }
                                if (usernameStatus == UsernameStatus.CHECKING) {
                                    scope.launch { snackbarHostState.showSnackbar("Still checking that username…") }
                                    return@Button
                                }
                                scope.launch {
                                    isSaving = true
                                    try {
                                        AuthManager.ensureInitialized(context)
                                        val uid = AuthManager.currentUser()?.uid
                                        if (uid != null) {
                                            var finalAvatarUrl = avatarUrl
                                            selectedImageUri?.let { uri ->
                                                snackbarHostState.showSnackbar("Uploading image...")
                                                finalAvatarUrl = AuthManager.uploadProfileImage(context, uid, uri)
                                            }

                                            val updates = mapOf(
                                                "firstName" to firstName,
                                                "lastName" to lastName,
                                                "username" to username,
                                                "phone" to phone,
                                                "bio" to bio,
                                                "avatarUrl" to finalAvatarUrl,
                                                "shareContactByEmail" to shareContactByEmail
                                            )
                                            AuthManager.updateUserProfile(uid, updates)
                                            currentUser = AuthManager.getUserProfile(uid, forceRefresh = true)
                                            avatarUrl = finalAvatarUrl
                                            selectedImageUri = null
                                            isEditing = false
                                            snackbarHostState.showSnackbar("Profile updated successfully")
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Update failed: ${e.message}")
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save Changes", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Favorites preview
            if (favorites.isNotEmpty()) {
                Text(
                    "Favorites",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                favorites.forEach { book ->
                    com.dennis.bookora.ui.components.CleanBookCard(book, onBookClick = { onFavoritesClick() }, onFavorite = { id ->
                        // unfavorite then reload favorites
                        scope.launch {
                            try {
                                val repo = com.dennis.bookora.repository.ApiBookRepository()
                                repo.toggleFavorite(id)
                                favorites = repo.getFavorites()
                            } catch (_: Exception) {}
                        }
                    })
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // ---------- Settings ----------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "SETTINGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                SettingsMenuItem(
                    icon = Icons.Outlined.Book,
                    title = "My Listings",
                    onClick = onMyListingsClick
                )

                SettingsMenuItem(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = "My Favorites",
                    onClick = onFavoritesClick
                )

                SettingsMenuItem(
                    icon = Icons.Outlined.Share,
                    title = "Share Contact Info",
                    subtitle = "When someone claims your book",
                    trailing = {
                        Switch(
                            checked = shareContactByEmail,
                            onCheckedChange = { newValue ->
                                shareContactByEmail = newValue
                                scope.launch {
                                    try {
                                        val uid = AuthManager.currentUser()?.uid
                                        if (uid != null) {
                                            AuthManager.updateUserProfile(uid, mapOf("shareContactByEmail" to newValue))
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to update setting")
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                        )
                    }
                )

                SettingsMenuItem(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "Privacy Policy",
                    onClick = onPrivacyClick
                )

                SettingsMenuItem(
                    icon = Icons.Outlined.Description,
                    title = "Terms & Conditions",
                    onClick = onTermsClick
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            TextButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Icon(
                    Icons.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
}

private enum class UsernameStatus { IDLE, CHECKING, AVAILABLE, TAKEN }

@Composable
private fun ProfileAvatar(
    initial: String,
    imageUrl: String,
    selectedUri: Uri?,
    editable: Boolean,
    isSaving: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier.clickable(enabled = editable && !isSaving, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selectedUri != null || imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = selectedUri ?: imageUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initial,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            if (isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
        if (editable) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = "Change photo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun RowScope.StatItem(count: String, label: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    supporting: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        supportingText = supporting?.let { { Text(it, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) } },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsernameField(
    value: String,
    onValueChange: (String) -> Unit,
    status: UsernameStatus
) {
    val (icon, iconTint, helper) = when (status) {
        UsernameStatus.CHECKING -> Triple(null, MaterialTheme.colorScheme.onSurfaceVariant, "Checking availability…")
        UsernameStatus.AVAILABLE -> Triple(Icons.Filled.CheckCircle, MaterialTheme.colorScheme.primary, "Username is available")
        UsernameStatus.TAKEN -> Triple(Icons.Filled.Cancel, MaterialTheme.colorScheme.error, "Username is already taken")
        UsernameStatus.IDLE -> Triple(null, MaterialTheme.colorScheme.onSurfaceVariant, null)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Username") },
        leadingIcon = { Text("@", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium) },
        trailingIcon = {
            when (status) {
                UsernameStatus.CHECKING -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                UsernameStatus.AVAILABLE, UsernameStatus.TAKEN -> icon?.let {
                    Icon(it, contentDescription = null, tint = iconTint)
                }
                UsernameStatus.IDLE -> {}
            }
        },
        supportingText = helper?.let {
            { Text(it, color = iconTint) }
        },
        singleLine = true,
        isError = status == UsernameStatus.TAKEN,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}