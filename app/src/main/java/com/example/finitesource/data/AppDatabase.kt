package com.example.finitesource.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.EarthquakeDao
import com.example.finitesource.data.earthquake.focalplane.ScenarioType

@Database(
	entities = [Earthquake::class, ScenarioType::class],
	version = 3,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun earthquakeDao(): EarthquakeDao

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
