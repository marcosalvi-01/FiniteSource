package com.example.finitesource.data

import java.util.Calendar
import androidx.room.TypeConverter
import com.example.finitesource.data.earthquake.focalplane.Scenario
import com.example.finitesource.data.earthquake.focalplane.ScenarioType

class Converters {
	@TypeConverter
	fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

	@TypeConverter
	fun datestampToCalendar(value: Long): Calendar =
		Calendar.getInstance().apply { timeInMillis = value }

	@TypeConverter
	fun scenariosToString(scenarios: List<Scenario>): String = scenarios.joinToString(",")

	@TypeConverter
	fun stringToScenarios(value: String): List<Scenario> {
		val scenarios = mutableListOf<Scenario>()
		value.split(";").forEach {
			scenarios.add(stringToScenario(it))
		}
		return scenarios
	}

	@TypeConverter
	fun scenarioToString(scenario: Scenario): String = scenario.toString()

	@TypeConverter
	fun stringToScenario(value: String): Scenario {
		val values = value.split(",")
		return Scenario(
			ScenarioType(
				values[0],
				values[1],
				values[2]
			),
			values[3],
			values[4],
			values[5],
			values[6]
		)
	}
}