package com.example.finitesource.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Calendar

@Database(entities = [Earthquake::class, FocalPlane::class, Footprint::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun earthquakeDao(): EarthquakeDao

	private class EarthquakeDatabaseCallback : Callback() {
		override fun onOpen(db: SupportSQLiteDatabase) {
			super.onOpen(db)
			// Update the database using the data from the API
			instance?.let { database ->
				// TODO
				database.earthquakeDao().insert(
					Earthquake(
						"1",
						"2021-01-01T00:00:00.000Z",
						Calendar.getInstance(),
						FP1("1"),
						null,
						Footprint("1")
					)
				)
			}
		}
	}

	companion object {
		@Volatile
		private var instance: AppDatabase? = null

		fun getInstance(context: Context) = instance ?: synchronized(this) {
			instance ?: Room.databaseBuilder(
				context,
				AppDatabase::class.java,
				"earthquake_database"
			).addCallback(EarthquakeDatabaseCallback())
				.build().also { instance = it }
		}
	}
}