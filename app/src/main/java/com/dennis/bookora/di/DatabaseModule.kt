package com.dennis.bookora.di

import android.content.Context
import androidx.room.Room
import com.dennis.bookora.data.local.BookoraDatabase
import com.dennis.bookora.data.local.dao.BookDao
import com.dennis.bookora.data.local.dao.CategoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBookoraDatabase(
        @ApplicationContext context: Context
    ): BookoraDatabase {
        return Room.databaseBuilder(
            context,
            BookoraDatabase::class.java,
            "bookora_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideBookDao(database: BookoraDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: BookoraDatabase): CategoryDao {
        return database.categoryDao()
    }
}
