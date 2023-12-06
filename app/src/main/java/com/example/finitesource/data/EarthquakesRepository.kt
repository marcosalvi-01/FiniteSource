package com.example.finitesource.data

import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.EarthquakeDao
import com.example.finitesource.data.earthquake.EarthquakeDetails
import com.example.finitesource.data.earthquake.EarthquakeUpdates
import com.example.finitesource.data.earthquake.focalplane.FocalPlane
import com.example.finitesource.data.earthquake.focalplane.FocalPlaneType
import com.example.finitesource.data.earthquake.toEarthquake
import kotlinx.coroutines.flow.first
import org.openapitools.client.apis.FiniteSourceAndroidAppApi
import org.openapitools.client.infrastructure.ApiClient
import javax.inject.Inject

class EarthquakesRepository @Inject constructor(
	private val earthquakeDao: EarthquakeDao,
	private val apiClient: ApiClient
) {
	fun getAll() = earthquakeDao.getAll()

	fun getById(id: String) = earthquakeDao.getById(id)

	// loads the latest data from the finite source api and compares it to the saved data
	// returns the differences that are supposed to be shown to the user
	suspend fun updateEarthquakes(): EarthquakeUpdates? {
		// build the request
		val request = apiClient
			.createService(FiniteSourceAndroidAppApi::class.java)
			.finiteSourceAppAppJsonGet()
		// make a network call to get the latest data
		// use the response
		when (val response = request.executeApiCall()) {
			// if the network call was successful, compare the data
			is Resource.Success -> {
				// get the saved earthquakes
				val savedEarthquakes = earthquakeDao.getAll().first()
				// map the loaded earthquakes to the database model
				val loadedEarthquakes = response.data!!.mapNotNull { toEarthquake(it) }
				// get the differences
				val updates = getDifferences(loadedEarthquakes, savedEarthquakes)
				// if there are any updates
				if (updates.hasUpdates())
				// update the database
					earthquakeDao.upsertAll(loadedEarthquakes)
				return updates
			}

			else -> {
				// if the network call failed, return null
				return null
			}
		}
	}

	suspend fun loadEarthquakeDetails(id: String): Earthquake {
		val earthquake = getById(id).first()
		// if this earthquake is already loaded, return
		if (earthquake.details != null)
			return earthquake

		val apiCalls = ApiCalls(apiClient)

		// initialize the focal planes
		var fp1: FocalPlane? = null
		var fp2: FocalPlane? = null

		// load the event details
		when (val eventDetailsResponse = apiCalls.getEventDetails(earthquake.id)) {
			is Resource.Success -> {
				val eventDetails = eventDetailsResponse.data!!
				// load the focal planes
				when (eventDetails.focalPlane) {
					0 -> {    // both focal planes
						// load both focal planes
						fp1 = FocalPlane(
							FocalPlaneType.FP1,
							apiCalls.getScenarios(earthquake, FocalPlaneType.FP1),
							apiCalls.getFiniteSource(earthquake, FocalPlaneType.FP1)
						)
						fp2 = FocalPlane(
							FocalPlaneType.FP2,
							apiCalls.getScenarios(earthquake, FocalPlaneType.FP2),
							apiCalls.getFiniteSource(earthquake, FocalPlaneType.FP2)
						)
					}

					1 -> {    // FP1
						// load only FP1
						fp1 = FocalPlane(
							FocalPlaneType.FP1,
							apiCalls.getScenarios(earthquake, FocalPlaneType.FP1),
							apiCalls.getFiniteSource(earthquake, FocalPlaneType.FP1)
						)
					}

					2 -> {    // FP2
						// load only FP2
						fp2 = FocalPlane(
							FocalPlaneType.FP2,
							apiCalls.getScenarios(earthquake, FocalPlaneType.FP2),
							apiCalls.getFiniteSource(earthquake, FocalPlaneType.FP2)
						)
					}

					else -> {
						// TODO handle errors
					}
				}
			}

			else -> {
				// TODO handle errors
			}
		}

		// load the footprints and add them to the earthquake details
		earthquake.details = EarthquakeDetails(
			fp1 = fp1,
			fp2 = fp2,
			apiCalls.getFootprints(earthquake),
		)

		// update the database
		earthquakeDao.upsert(earthquake)

		// return the updated earthquake
		return earthquake
	}

	companion object {
		@Volatile
		private var instance: EarthquakesRepository? = null

		fun getInstance(earthquakeDao: EarthquakeDao, apiClient: ApiClient) =
			instance ?: synchronized(this) {
				instance ?: EarthquakesRepository(earthquakeDao, apiClient).also { instance = it }
			}
	}
}

// function to get the differences between the old and the new data
private fun getDifferences(
	loadedEarthquakes: List<Earthquake>,
	savedEarthquakes: List<Earthquake>
): EarthquakeUpdates {
	// Check if there are any new earthquakes
	val newIds = loadedEarthquakes.map { it.id }
	val oldIds = savedEarthquakes.map { it.id }
	val newEarthquakes = newIds.filter { it !in oldIds }

	// Check for which events the finite source has been updated
	val updatedEarthquakes = loadedEarthquakes.filter { new ->
		savedEarthquakes.any { old ->
			// if the ids are not the same, skip
			if (new.id != old.id) return@any false
			// if the old event has no finite source, but the new one has one, return true
			if (old.finiteSourceLastUpdate == null && new.finiteSourceLastUpdate != null) return@any true
			// if the new event has a finite source more recent than the old one, return true, false otherwise
			new.finiteSourceLastUpdate?.after(old.finiteSourceLastUpdate) ?: false
		}
	}

	// return the updates
	return EarthquakeUpdates(
		newEarthquakes = newEarthquakes.mapNotNull { id ->
			loadedEarthquakes.find { it.id == id }
		},
		finiteSourceUpdated = updatedEarthquakes,
	)
}