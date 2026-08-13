package com.dennis.bookora.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dennis.bookora.R
import com.dennis.bookora.models.Book
import com.dennis.bookora.models.ListingType

// ---------- Shared bits ----------

@Composable
private fun BookCover(book: Book, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        if (book.coverUrl.isNotBlank()) {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = book.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    alpha = 0.35f
                )
            }
        }
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.28f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) Color(0xFFFF5252) else Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ListingBadge(listingType: ListingType) {
    val (bg, fg) = if (listingType == ListingType.GIVEAWAY) {
        Color(0xFFE3F6E8) to Color(0xFF1E7B3C)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) to MaterialTheme.colorScheme.primary
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            text = listingType.name.lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OwnerTag(username: String, tint: Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(11.dp), tint = tint)
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "@$username",
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LocationTag(location: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = location.ifBlank { "Nairobi, KE" },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------- Grid card ----------

@Composable
fun VerticalBookCard(book: Book, onBookClick: (String) -> Unit, onFavorite: (String) -> Unit = {}) {
    Column(
        modifier = Modifier
            .width(158.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onBookClick(book.id) }
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(196.dp)) {
            BookCover(book, modifier = Modifier.fillMaxSize())

            // Soft gradient so the favorite button always reads clearly
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
            )

            FavoriteButton(
                isFavorite = book.isFavorite,
                onClick = { onFavorite(book.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )

            if (book.listingType == ListingType.GIVEAWAY) {
                Box(modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                    ListingBadge(book.listingType)
                }
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (book.ownerUsername.isNotBlank()) {
                OwnerTag(book.ownerUsername)
                Spacer(modifier = Modifier.height(4.dp))
            }

            LocationTag(book.location)
        }
    }
}

// ---------- List card ----------

@Composable
fun CleanBookCard(book: Book, onBookClick: (String) -> Unit, onFavorite: (String) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onBookClick(book.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        BookCover(
            book,
            modifier = Modifier
                .width(84.dp)
                .height(116.dp)
                .clip(RoundedCornerShape(14.dp))
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = book.category.ifBlank { "General" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                val handle = book.ownerUsername.ifBlank {
                    book.ownerId.take(6).ifBlank { "reader" }
                }
                OwnerTag(handle)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListingBadge(book.listingType)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LocationTag(book.location)
                    Spacer(modifier = Modifier.width(10.dp))
                    FavoriteButton(
                        isFavorite = book.isFavorite,
                        onClick = { onFavorite(book.id) },
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}