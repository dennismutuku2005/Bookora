package com.dennis.bookora.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dennis.bookora.models.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val title: String
)

fun CategoryEntity.toCategory(): Category {
    return Category(
        id = id,
        title = title
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        title = title
    )
}
