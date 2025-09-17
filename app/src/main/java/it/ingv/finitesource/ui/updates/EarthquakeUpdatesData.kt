package it.ingv.finitesource.ui.updates

import android.os.Parcelable
import it.ingv.finitesource.data.local.EarthquakeUpdates
import it.ingv.finitesource.data.local.earthquake.Earthquake
import kotlinx.parcelize.Parcelize
import java.util.Calendar

@Parcelize
data class EarthquakeData(
	val id: String,
	val name: String,
	val magnitude: Double,
	val depth: Double,
	val date: Calendar,
) : Parcelable {
	companion object {
		fun from(earthquake: Earthquake): EarthquakeData {
			return EarthquakeData(
				earthquake.id,
				earthquake.name,
				earthquake.magnitude,
				earthquake.depth,
				earthquake.date,
			)
		}
	}
}

@Parcelize
data class EarthquakeUpdatesData(
	val newEarthquakes: List<EarthquakeData>,
	val finiteSourceUpdated: List<EarthquakeData>,
	val newFiniteSource: List<EarthquakeData>,
	val removedEarthquakes: List<EarthquakeData>,
) : Parcelable {
	fun hasUpdates(): Boolean {
		return newEarthquakes.isNotEmpty() ||
				finiteSourceUpdated.isNotEmpty() ||
				newFiniteSource.isNotEmpty() ||
				removedEarthquakes.isNotEmpty()
	}

	companion object {
		fun from(earthquakeUpdates: EarthquakeUpdates): EarthquakeUpdatesData {
			return EarthquakeUpdatesData(
				earthquakeUpdates.newEarthquakes.map { EarthquakeData.from(it) },
				earthquakeUpdates.finiteSourceUpdated.map { EarthquakeData.from(it) },
				earthquakeUpdates.newFiniteSource.map { EarthquakeData.from(it) },
				earthquakeUpdates.removedEarthquakes.map { EarthquakeData.from(it) },
			)
		}
	}
}
