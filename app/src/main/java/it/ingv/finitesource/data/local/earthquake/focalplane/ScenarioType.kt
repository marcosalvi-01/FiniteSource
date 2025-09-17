package it.ingv.finitesource.data.local.earthquake.focalplane

import androidx.room.Entity
import androidx.room.PrimaryKey
import it.ingv.finitesource.data.local.database.dao.ScenarioTypeDao

@Entity
open class ScenarioType(
	@PrimaryKey val name: String,
	val dir: String,
	val url: String
) {
	override fun toString(): String {
		return "$dir,$name,$url"
	}

	override fun equals(other: Any?): Boolean {
		if (other == null || other !is ScenarioType) {
			return false
		}
		return dir == other.dir
	}

	override fun hashCode(): Int {
		return dir.hashCode()
	}

	companion object {
		fun parseString(string: String, scenarioTypeDao: ScenarioTypeDao): ScenarioType {
			return scenarioTypeDao.getById(string)
		}
	}
}