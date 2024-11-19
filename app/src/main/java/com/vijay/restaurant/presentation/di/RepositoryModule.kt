package com.vijay.restaurant.presentation.di

import com.vijay.restaurant.domain.APIRepository
import com.vijay.restaurant.data.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRepository(apiService: ApiService): APIRepository {
        return APIRepository(apiService)
    }
}
