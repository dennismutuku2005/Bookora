package com.dennis.bookora.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dennis.bookora.models.Book
import com.dennis.bookora.models.ListingCondition
import com.dennis.bookora.models.ListingType
import com.dennis.bookora.repository.ApiBookRepository
import com.dennis.bookora.repository.auth.AuthManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    bookId: String? = null,
    onBack: (() -> Unit)? = null,
    onSuccess: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fiction") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isExchange by remember { mutableStateOf(true) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingCoverUrl by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPublishing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(bookId != null) }
    var condition by remember { mutableStateOf("Like New") }
    var categoryExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val categoriesList = listOf("Fiction", "Non-Fiction", "Self-Help", "Technology", "Science", "History", "Biography", "Children", "Romance", "Other")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    val repo = remember { ApiBookRepository() }

    LaunchedEffect(bookId) {
        if (bookId != null) {
            try {
                val existingBook = repo.getBookById(bookId)
                if (existingBook != null) {
                    title = existingBook.title
                    author = existingBook.author
                    category = existingBook.category.ifBlank { "Fiction" }
                    description = existingBook.description
                    location = existingBook.location
                    isExchange = existingBook.listingType == ListingType.EXCHANGE
                    existingCoverUrl = existingBook.coverUrl
                    condition = when (existingBook.condition) {
                        ListingCondition.NEW -> "New"
                        ListingCondition.LIKE_NEW -> "Like New"
                        ListingCondition.GOOD -> "Good"
                        ListingCondition.FAIR -> "Fair"
                    }
                }
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("❌ Failed to load book: ${e.message}") }
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.padding(bottom = 8.dp)) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        }
        Text(
            text = if (bookId == null) "List a New Book" else "Edit Listing",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Share the joy of reading with others",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // Image Picker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null || existingCoverUrl.isNotEmpty()) {
                AsyncImage(
                    model = selectedImageUri ?: existingCoverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Rounded.Edit,
                        null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp),
                        tint = Color.White
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.AddPhotoAlternate,
                                null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Add Book Cover Photo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Make it look good!",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Book Title") },
            placeholder = { Text("e.g. The Alchemist") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Rounded.MenuBook, null, tint = MaterialTheme.colorScheme.primary) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Author Name") },
            placeholder = { Text("e.g. Paulo Coelho") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Rounded.PersonOutline, null, tint = MaterialTheme.colorScheme.primary) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                leadingIcon = { Icon(Icons.Rounded.Category, null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categoriesList.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            category = cat
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val conditions = listOf("Like New", "Good", "Fair", "Used")

        Text(
            text = "Condition",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            conditions.forEach { option ->
                FilterChip(
                    selected = condition == option,
                    onClick = { condition = option },
                    label = { Text(option) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            placeholder = { Text("Describe the book's cover, condition, and story.") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Your Location") },
            placeholder = { Text("e.g. Westlands, Nairobi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Rounded.LocationOn, null, tint = MaterialTheme.colorScheme.primary) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Listing Type",
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        ) {
            FilterChip(
                selected = isExchange,
                onClick = { isExchange = true },
                label = { Text("Exchange", modifier = Modifier.padding(8.dp)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = if (isExchange) { { Icon(Icons.Rounded.Check, null, Modifier.size(20.dp)) } } else null
            )
            Spacer(modifier = Modifier.width(12.dp))
            FilterChip(
                selected = !isExchange,
                onClick = { isExchange = false },
                label = { Text("Giveaway", modifier = Modifier.padding(8.dp)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = if (!isExchange) { { Icon(Icons.Rounded.Check, null, Modifier.size(20.dp)) } } else null
            )
        }
        
        Spacer(modifier = Modifier.height(36.dp))
        
        Button(
            onClick = {
                scope.launch {
                    try {
                        isPublishing = true
                        val user = AuthManager.currentUser() ?: run {
                            snackbarHostState.showSnackbar("Please sign in to publish")
                            return@launch
                        }

                        var coverUrl = existingCoverUrl
                        
                        // Upload new image if selected
                        if (selectedImageUri != null) {
                            snackbarHostState.showSnackbar("Uploading book cover...")
                            coverUrl = repo.uploadBookCoverImage(context, selectedImageUri!!)
                            snackbarHostState.showSnackbar("Cover uploaded")
                        }

                        snackbarHostState.showSnackbar("Publishing your listing...")

                        val book = Book(
                            id = bookId ?: "",
                            title = title.trim(),
                            author = author.trim(),
                            category = category,
                            description = description.trim(),
                            location = location.trim(),
                            condition = when (condition) {
                                "New" -> ListingCondition.NEW
                                "Like New" -> ListingCondition.LIKE_NEW
                                "Fair" -> ListingCondition.FAIR
                                else -> ListingCondition.GOOD
                            },
                            coverUrl = coverUrl,
                            listingType = if (isExchange) ListingType.EXCHANGE else ListingType.GIVEAWAY,
                            ownerId = user.id,
                            ownerUsername = user.username,
                            postedTimestamp = System.currentTimeMillis(),
                            postedDate = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(java.util.Date())
                        )

                        if (bookId == null) {
                            repo.saveBook(book)
                            title = ""
                            author = ""
                            description = ""
                            location = ""
                            selectedImageUri = null
                            snackbarHostState.showSnackbar("Listing published successfully!")
                        } else {
                            repo.updateBook(bookId, book)
                            snackbarHostState.showSnackbar("Listing updated successfully!")
                            onSuccess?.invoke()
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("${e.message ?: "Publish failed"}")
                    } finally {
                        isPublishing = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (isPublishing) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Icon(if (bookId == null) Icons.Rounded.Publish else Icons.Rounded.Save, null)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(if (bookId == null) "Publish Listing" else "Save Changes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
