package com.dennis.bookora.models

enum class ListingCondition { NEW, LIKE_NEW, GOOD, FAIR }
enum class ListingType { EXCHANGE, GIVEAWAY }

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val category: String = "",
    val condition: ListingCondition = ListingCondition.GOOD,
    val location: String = "",
    val postedDate: String = "",
    val postedTimestamp: Long = 0L,
    val coverUrl: String = "",
    val listingType: ListingType = ListingType.GIVEAWAY,
    val description: String = "",
    val ownerId: String = "",
    val ownerUsername: String = "",
    val rating: Double = 0.0,
    val distance: String = "",
    val isFavorite: Boolean = false,
    val coverColor: Long = 0xFFF0F4FF
)
