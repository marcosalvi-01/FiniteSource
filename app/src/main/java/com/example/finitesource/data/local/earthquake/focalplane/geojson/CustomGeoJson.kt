package com.example.finitesource.data.local.earthquake.focalplane.geojson

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
data class CustomGeoJson(
	val features: List<Feature>,
	val type: String
) {
	/**
	 * Converts the GeoJson object into a string.
	 */
	fun stringify(): String =
		Moshi.Builder().build().adapter(CustomGeoJson::class.java).toJson(this)

	companion object {
		/**
		 * Parses a GeoJson string into a GeoJson object.
		 * Throws an exception if the string is not valid GeoJson.
		 */
		fun parseString(string: String): CustomGeoJson = try {
			Moshi.Builder().build().adapter(CustomGeoJson::class.java).fromJson(string)!!
		} catch (e: Exception) {
			e.printStackTrace()
			throw Exception("Invalid GeoJson string")
		}
	}
}

@JsonClass(generateAdapter = true)
data class Feature(
	val geometry: Geometry,
	val properties: Properties,
	val type: String
)

@JsonClass(generateAdapter = true)
data class Geometry(
	val coordinates: List<List<List<Double>>>,
	val type: String
)

@JsonClass(generateAdapter = true)
data class Properties(
	@Json(name = "Rake_d") val rakeD: Double?,
	@Json(name = "Slip_m") val slipM: Double?
)