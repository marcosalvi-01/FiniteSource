package com.example.finitesource.di

import android.content.Context
import com.example.finitesource.data.EarthquakesRepository
import com.example.finitesource.data.database.AppDatabase
import com.example.finitesource.data.database.dao.EarthquakeDao
import com.example.finitesource.data.database.dao.ScenarioTypeDao
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

	@Provides
	@Singleton
	fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
		return AppDatabase.getInstance(context)
	}

	@Provides
	fun provideEarthquakeDao(appDatabase: AppDatabase): EarthquakeDao {
		return appDatabase.earthquakeDao()
	}

	@Provides
	fun provideScenarioTypeDao(appDatabase: AppDatabase): ScenarioTypeDao {
		return appDatabase.scenarioTypeDao()
	}

	@Provides
	@Singleton
	fun provideApiClient(): ApiClient {
		return ApiClient()
	}

	@Provides
	@Singleton
	fun provideRepository(
		earthquakeDao: EarthquakeDao,
		scenarioTypeDao: ScenarioTypeDao,
		apiClient: ApiClient
	): EarthquakesRepository {
		return EarthquakesRepository.getInstance(earthquakeDao, scenarioTypeDao, apiClient)
	}
}