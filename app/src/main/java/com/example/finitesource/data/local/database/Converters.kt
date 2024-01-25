package com.example.finitesource.data.local.database

import androidx.room.TypeConverter
import com.example.finitesource.data.local.Products
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlaneType
import com.example.finitesource.data.local.earthquake.focalplane.Scenario
import com.example.finitesource.data.local.earthquake.focalplane.ScenarioType
import com.example.finitesource.data.local.earthquake.focalplane.geojson.CustomGeoJson
import org.osmdroid.util.BoundingBox
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

	@TypeConverter
	fun boundingBoxToString(boundingBox: BoundingBox): String = boundingBox.toString()

	@TypeConverter
	fun stringToBoundingBox(value: String): BoundingBox {
		// N:27.459793; E:84.47654; S:28.364096; W:85.931061
		val values = value.split("; ")
		return BoundingBox(
			values[0].substring(2).toDouble(),
			values[1].substring(2).toDouble(),
			values[2].substring(2).toDouble(),
			values[3].substring(2).toDouble()
		)
	}

	@TypeConverter
	fun productListToString(productList: List<Products>): String = productList.joinToString(",")

	@TypeConverter
	fun stringToProductList(value: String): List<Products> {
		val productList = mutableListOf<Products>()
		value.split(",").forEach {
			productList.add(Products.parseString(it))
		}
		return productList
	}
}