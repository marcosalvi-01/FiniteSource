package it.ingv.finitesource.data.local

import it.ingv.finitesource.data.local.earthquake.Earthquake

/**
 * Data class representing updates to the set of earthquakes.
 *
 * @property newEarthquakes A set of earthquakes that are new.
 * @property finiteSourceUpdated A set of earthquakes for which the finite source has been updated.
 * @property newFiniteSource A set of earthquakes for which a finite source has been added.
 * @property removedEarthquakes A set of earthquakes that have been removed.
 */
data class EarthquakeUpdates(
	val newEarthquakes: Set<Earthquake>,
	val finiteSourceUpdated: Set<Earthquake>,
	val newFiniteSource: Set<Earthquake>,
	val removedEarthquakes: Set<Earthquake>,
) {
	/**
	 * Checks if there are any updates to the set of earthquakes.
	 *
	 * @return True if there are any new, updated, or removed earthquakes, or if a finite source has been added to any earthquake. False otherwise.
	 */
	fun hasUpdates(): Boolean {
		return newEarthquakes.isNotEmpty() ||
				finiteSourceUpdated.isNotEmpty() ||
				newFiniteSource.isNotEmpty() ||
				removedEarthquakes.isNotEmpty()
	}
}
