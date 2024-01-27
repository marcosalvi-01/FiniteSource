package com.example.finitesource.data.local

import com.example.finitesource.data.local.earthquake.Earthquake

data class EarthquakeUpdates(
	val newEarthquakes: Set<Earthquake>,
	val finiteSourceUpdated: Set<Earthquake>,
	val newProducts: Map<Earthquake, List<Products>>
) {
	// function to check if there are updates
	fun hasUpdates(): Boolean {
		return newEarthquakes.isNotEmpty() || finiteSourceUpdated.isNotEmpty() || newProducts.isNotEmpty()
	}
}