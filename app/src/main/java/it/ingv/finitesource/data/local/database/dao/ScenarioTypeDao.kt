package it.ingv.finitesource.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.ingv.finitesource.data.local.earthquake.focalplane.ScenarioType

@Dao
interface ScenarioTypeDao {

	@Upsert
	fun upsertAll(scenarioTypes: List<ScenarioType>)

	@Query("SELECT * FROM scenariotype")
	fun getAll(): List<ScenarioType>

	@Query("SELECT * FROM scenariotype WHERE dir = :id")
	fun getById(id: String): ScenarioType
}