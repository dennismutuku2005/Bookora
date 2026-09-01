package com.dennis.bookora.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dennis.bookora.models.Book
import com.dennis.bookora.models.ListingCondition
import com.dennis.bookora.models.ListingType

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val category: String,
    val condition: String,
    val location: String,
    val postedDate: String,
    val postedTimestamp: Long,
    val coverUrl: String,
    val listingType: String,
    val description: String,
    val ownerId: String,
    val ownerUsername: String,
    val rating: Double,
    val distance: String,
    val isFavorite: Boolean,
    val coverColor: Long
)

fun BookEntity.toBook(): Book {
    return Book(
        id = id,
        title = title,
        author = author,
        category = category,
        condition = try { ListingCondition.valueOf(condition) } catch (_: Exception) { ListingCondition.GOOD },
        location = location,
        postedDate = postedDate,
        postedTimestamp = postedTimestamp,
        coverUrl = coverUrl,
        listingType = try { ListingType.valueOf(listingType) } catch (_: Exception) { ListingType.GIVEAWAY },
        description = description,
        ownerId = ownerId,
        ownerUsername = ownerUsername,
        rating = rating,
        distance = distance,
        isFavorite = isFavorite,
        coverColor = coverColor
    )
}

fun Book.toEntity(): BookEntity {
    return BookEntity(
        id = id,
        title = title,
        author = author,
        category = category,
        condition = condition.name,
        location = location,
        postedDate = postedDate,
        postedTimestamp = postedTimestamp,
        coverUrl = coverUrl,
        listingType = listingType.name,
        description = description,
        ownerId = ownerId,
        ownerUsername = ownerUsername,
        rating = rating,
        distance = distance,
        isFavorite = isFavorite,
        coverColor = coverColor
    )
}
