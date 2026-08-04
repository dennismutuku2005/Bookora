package com.dennis.bookora.models

import java.time.LocalDate

enum class ListingCondition { NEW, LIKE_NEW, GOOD, FAIR }
enum class ListingType { EXCHANGE, GIVEAWAY }

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val category: String,
    val condition: ListingCondition,
    val location: String,
    val postedDate: String,
    val coverUrl: String,
    val listingType: ListingType,
    val description: String,
    val ownerId: String,
    val rating: Double,
    val distance: String,
    val isFavorite: Boolean = false
)
