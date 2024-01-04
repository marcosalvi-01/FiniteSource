package com.example.finitesource.data.local.database

import androidx.room.TypeConverter
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlaneType
import com.example.finitesource.data.local.earthquake.focalplane.Scenario
import com.example.finitesource.data.local.earthquake.focalplane.ScenarioType
import com.example.finitesource.data.local.earthquake.focalplane.geojson.CustomGeoJson
import java.util.Calendar

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
	fun scenarioToString(scenario: Scenario): String {
		return scenario.toString()
	}

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

	@TypeConverter
	fun focalPlaneTypeToString(focalPlaneType: FocalPlaneType): String = focalPlaneType.name

	@TypeConverter
	fun stringToFocalPlaneType(value: String): FocalPlaneType = FocalPlaneType.valueOf(value)

	@TypeConverter
	fun geoJsonToString(geoJson: CustomGeoJson): String = geoJson.stringify()

	@TypeConverter
	fun stringToGeoJson(value: String): CustomGeoJson = CustomGeoJson.parseString(value)
}