package com.dennis.bookora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dennis.bookora.data.local.dao.BookDao
import com.dennis.bookora.data.local.dao.CategoryDao
import com.dennis.bookora.data.local.entities.BookEntity
import com.dennis.bookora.data.local.entities.CategoryEntity

@Database(
    entities = [BookEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BookoraDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: BookoraDatabase? = null

        fun getInstance(context: android.content.Context): BookoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    BookoraDatabase::class.java,
                    "bookora_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
