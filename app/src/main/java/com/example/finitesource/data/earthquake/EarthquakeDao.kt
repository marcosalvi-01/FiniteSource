package com.example.finitesource.data.earthquake

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EarthquakeDao {
	@Query("SELECT * FROM earthquake ORDER BY date ASC")
	fun getAll(): Flow<List<Earthquake>>

	@Query("SELECT * FROM earthquake WHERE id = :id")
	fun getById(id: String): Flow<Earthquake>

	@Upsert
	fun upsertAll(earthquakes: List<Earthquake>)

	@Insert
	fun insert(earthquake: Earthquake)

	@Insert
	fun insertAll(earthquakes: List<Earthquake>)
}