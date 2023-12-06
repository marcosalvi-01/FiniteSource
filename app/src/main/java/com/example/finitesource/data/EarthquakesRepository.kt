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
import org.openapitools.client.models.FiniteSourceAppAppJsonGet200ResponseInner
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
		// execute the request, return null if it fails
		val response: List<FiniteSourceAppAppJsonGet200ResponseInner>?
		try {
			response = request.executeApiCall()
		} catch (e: Exception) {
			return null
		}
		// get the saved earthquakes
		val savedEarthquakes = earthquakeDao.getAll().first()
		// map the loaded earthquakes to the database model
		val loadedEarthquakes = response.mapNotNull { toEarthquake(it) }
		// get the differences
		val updates = getDifferences(loadedEarthquakes, savedEarthquakes)
		// if there are any updates
		if (updates.hasUpdates())
		// update the database
			earthquakeDao.upsertAll(loadedEarthquakes)
		return updates
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
		val eventDetailsResponse = apiCalls.getEventDetails(earthquake.id)
		val eventDetails = eventDetailsResponse!!
		// load the focal planes
		when (eventDetails.focalPlane) {
			0 -> {    // FP1 and FP2
				fp1 = buildFocalPlane(earthquake, FocalPlaneType.FP1, apiCalls)
				fp2 = buildFocalPlane(earthquake, FocalPlaneType.FP2, apiCalls)
			}

			1 -> {    // FP1
				fp1 = buildFocalPlane(earthquake, FocalPlaneType.FP1, apiCalls)
			}

			2 -> {    // FP2
				fp2 = buildFocalPlane(earthquake, FocalPlaneType.FP2, apiCalls)
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

// helper function to load the products of an earthquake in a specific focal plane
private fun buildFocalPlane(
	earthquake: Earthquake,
	focalPlaneType: FocalPlaneType,
	apiCalls: ApiCalls
): FocalPlane {
	return FocalPlane(
		focalPlaneType,
		apiCalls.getScenarios(earthquake, focalPlaneType),
		apiCalls.getFiniteSource(earthquake, focalPlaneType)
	)
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