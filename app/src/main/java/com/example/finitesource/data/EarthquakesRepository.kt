package com.example.finitesource.data

import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.EarthquakeDao
import com.example.finitesource.data.earthquake.EarthquakeUpdates
import com.example.finitesource.data.earthquake.toEarthquake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.FiniteSourceAndroidAppApi
import org.openapitools.client.infrastructure.ApiClient
import retrofit2.Response
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
		val response = apiCall {
			request.execute()
		}
		// use the response
		when (response) {
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

// function to make a network call and return a Resource in a coroutine
private suspend fun <T> safeApiCall(apiToBeCalled: () -> Response<T>): Resource<T> {
	return withContext(Dispatchers.IO) {
		apiCall(apiToBeCalled)
	}
}

// function to make a network call and return a Resource
private fun <T> apiCall(apiToBeCalled: () -> Response<T>): Resource<T> {
	return try {
		val response: Response<T> = apiToBeCalled()
		if (response.isSuccessful)
			Resource.Success(data = response.body()!!)
		else
			Resource.Error(errorMessage = "Something went wrong")
		// TODO handle errors
	} catch (e: Exception) {
		Resource.Error(errorMessage = "Something went wrong")
	}
}