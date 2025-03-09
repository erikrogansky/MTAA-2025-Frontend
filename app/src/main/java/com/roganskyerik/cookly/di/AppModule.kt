package com.roganskyerik.cookly.di

import android.content.Context
import com.roganskyerik.cookly.network.ApiService
import com.roganskyerik.cookly.repository.ApiRepository
import com.roganskyerik.cookly.utils.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context,
        apiService: dagger.Lazy<ApiService>
    ): TokenManager {
        return TokenManager(context, apiService)
    }

    @Provides
    @Singleton
    fun provideApiRepository(apiService: ApiService, @ApplicationContext context: Context): ApiRepository {
        return ApiRepository(apiService, context)
    }
}