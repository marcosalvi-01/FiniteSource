package com.example.finitesource.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.finitesource.data.local.database.dao.EarthquakeDao
import com.example.finitesource.data.local.database.dao.ScenarioTypeDao
import com.example.finitesource.data.local.earthquake.Earthquake
import com.example.finitesource.data.local.earthquake.focalplane.ScenarioType

@Database(
	entities = [Earthquake::class, ScenarioType::class],
	version = 1,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun earthquakeDao(): EarthquakeDao
	abstract fun scenarioTypeDao(): ScenarioTypeDao

	companion object {
		@Volatile
		private var instance: AppDatabase? = null

		fun getInstance(context: Context) = instance ?: synchronized(this) {
			instance ?: Room.databaseBuilder(
				context,
				AppDatabase::class.java,
				"earthquake_database"
			).fallbackToDestructiveMigration()
				.build().also { instance = it }
		}
	}
}
