package com.example.finitesource.data.earthquake

data class EarthquakeUpdates(
	val newEarthquakes: List<Earthquake>,
	val finiteSourceUpdated: List<Earthquake>,
) {
	// function to check if there are updates
	fun hasUpdates(): Boolean {
		return newEarthquakes.isNotEmpty() || finiteSourceUpdated.isNotEmpty()
	}
}