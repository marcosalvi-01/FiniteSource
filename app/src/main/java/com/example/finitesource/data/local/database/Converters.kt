package com.example.finitesource.data.local.database

import androidx.room.TypeConverter
import com.example.finitesource.data.local.Products
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlaneType
import com.example.finitesource.data.local.earthquake.focalplane.Scenario
import com.example.finitesource.data.local.earthquake.focalplane.ScenarioType
import com.example.finitesource.data.local.earthquake.focalplane.geojson.CustomGeoJson
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import org.osmdroid.util.BoundingBox
import java.io.StringReader
import java.io.StringWriter
import java.util.Calendar

class Converters {
	@TypeConverter
	fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

	@TypeConverter
	fun datestampToCalendar(value: Long): Calendar =
		Calendar.getInstance().apply { timeInMillis = value }

	@TypeConverter
	fun scenariosToString(scenarios: List<Scenario>): String {
		val stringWriter = StringWriter()
		CSVWriter(stringWriter).use { writer ->
			for (scenario in scenarios) {
				writer.writeNext(
					arrayOf(
						scenario.id,
						scenario.name,
						scenario.url,
						scenario.displacementMapDescription,
						scenario.displacementMapUrl,
						scenario.predictedFringesDescription,
						scenario.predictedFringesUrl
					)
				)
			}
		}
		return stringWriter.toString()
	}

	@TypeConverter
	fun stringToScenarios(value: String): List<Scenario> {
		val scenarios = mutableListOf<Scenario>()
		CSVReader(StringReader(value)).use { reader ->
			var nextLine: Array<String>?
			while (reader.readNext().also { nextLine = it } != null) {
				scenarios.add(
					Scenario(
						ScenarioType(nextLine!![0], nextLine!![1], nextLine!![2]),
						nextLine!![3],
						nextLine!![4],
						nextLine!![5],
						nextLine!![6]
					)
				)
			}
		}
		return scenarios
	}

	@TypeConverter
	fun focalPlaneTypeToString(focalPlaneType: FocalPlaneType): String {
		val stringWriter = StringWriter()
		CSVWriter(stringWriter).use { writer ->
			writer.writeNext(arrayOf(focalPlaneType.name))
		}
		return stringWriter.toString()
	}

	@TypeConverter
	fun stringToFocalPlaneType(value: String): FocalPlaneType {
		val reader = CSVReader(StringReader(value))
		val nextLine = reader.readNext()
		return FocalPlaneType.valueOf(nextLine[0])
	}

	@TypeConverter
	fun geoJsonToString(geoJson: CustomGeoJson): String {
		val stringWriter = StringWriter()
		CSVWriter(stringWriter).use { writer ->
			writer.writeNext(arrayOf(geoJson.stringify()))
		}
		return stringWriter.toString()
	}

	@TypeConverter
	fun stringToGeoJson(value: String): CustomGeoJson {
		val reader = CSVReader(StringReader(value))
		val nextLine = reader.readNext()
		return CustomGeoJson.parseString(nextLine[0])
	}

	@TypeConverter
	fun boundingBoxToString(boundingBox: BoundingBox): String {
		val stringWriter = StringWriter()
		CSVWriter(stringWriter).use { writer ->
			writer.writeNext(
				arrayOf(
					boundingBox.latNorth.toString(),
					boundingBox.lonWest.toString(),
					boundingBox.latSouth.toString(),
					boundingBox.lonEast.toString()
				)
			)
		}
		return stringWriter.toString()
	}

	@TypeConverter
	fun stringToBoundingBox(value: String): BoundingBox {
		val reader = CSVReader(StringReader(value))
		val nextLine = reader.readNext()
		return BoundingBox(
			nextLine[0].toDouble(),
			nextLine[3].toDouble(),
			nextLine[2].toDouble(),
			nextLine[1].toDouble()
		)
	}

	@TypeConverter
	fun productListToString(productList: List<Products>): String {
		val stringWriter = StringWriter()
		CSVWriter(stringWriter).use { writer ->
			productList.forEach { product ->
				writer.writeNext(arrayOf(product.toString()))
			}
		}
		return stringWriter.toString()
	}

	@TypeConverter
	fun stringToProductList(value: String): List<Products> {
		val productList = mutableListOf<Products>()
		val reader = CSVReader(StringReader(value))
		var nextLine: Array<String>?
		while (reader.readNext().also { nextLine = it } != null) {
			productList.add(Products.parseString(nextLine!![0]))
		}
		return productList
	}
}