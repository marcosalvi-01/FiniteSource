package com.example.finitesource.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.finitesource.data.earthquake.focalplane.ScenarioType

@Dao
interface ScenarioTypeDao {

	@Upsert
	fun upsertAll(scenarioTypes: List<ScenarioType>)

	@Query("SELECT * FROM scenariotype")
	fun getAll(): List<ScenarioType>

	@Query("SELECT * FROM scenariotype WHERE id = :id")
	fun getById(id: String): ScenarioType
}