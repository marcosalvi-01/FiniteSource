package com.example.finitesource.di

import android.content.Context
import com.example.finitesource.data.AppDatabase
import com.example.finitesource.data.earthquake.EarthquakeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.openapitools.client.infrastructure.ApiClient
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

	@Singleton
	@Provides
	fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
		return AppDatabase.getInstance(context)
	}

	@Provides
	fun provideEarthquakeDao(appDatabase: AppDatabase): EarthquakeDao {
		return appDatabase.earthquakeDao()
	}

	@Provides
	fun provideApiClient(): ApiClient {
		return ApiClient()
	}
}