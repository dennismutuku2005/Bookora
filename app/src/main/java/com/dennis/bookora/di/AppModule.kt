package com.dennis.bookora.di

import com.dennis.bookora.repository.BookRepository
import com.dennis.bookora.repository.FirebaseBookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(
        firebaseRepository: FirebaseBookRepository
    ): BookRepository
}
