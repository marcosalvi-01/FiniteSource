package com.example.finitesource.data.local.earthquake.focalplane

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finitesource.data.local.database.dao.ScenarioTypeDao

@Entity
open class ScenarioType(
	@PrimaryKey var id: String,
	var name: String,
	var url: String
) {
	override fun toString(): String {
		return "$id,$name,$url"
	}

	override fun equals(other: Any?): Boolean {
		if (other == null || other !is ScenarioType) {
			return false
		}
		return id == other.id
	}

	override fun hashCode(): Int {
		return id.hashCode()
	}

	companion object {
		fun parseString(string: String, scenarioTypeDao: ScenarioTypeDao): ScenarioType {
			return scenarioTypeDao.getById(string)
		}
	}
}