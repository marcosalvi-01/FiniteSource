package com.example.finitesource.data

import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.EarthquakeDao
import com.example.finitesource.data.earthquake.EarthquakeUpdates
import com.example.finitesource.data.earthquake.toEarthquake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.FiniteSourceAndroidAppApi
import org.openapitools.client.infrastructure.ApiClient
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class EarthquakesRepository @Inject constructor(private val earthquakeDao: EarthquakeDao) {

	fun getAll() = earthquakeDao.getAll()
	fun getById(id: String) = earthquakeDao.getById(id)

	// loads the latest data from the finite source api and compares it to the saved data
	// returns the differences that are supposed to be shown to the user
	suspend fun updateEarthquakes(): EarthquakeUpdates? {
		// build the request
		val request = ApiClient()
			.createService(FiniteSourceAndroidAppApi::class.java)
			.finiteSourceAppAppJsonGet()
		// make a network call to get the latest data
		val response = safeApiCall {
			request.execute()
		}
		// initialize the updates
		var updates: EarthquakeUpdates? = null
		// use the response
		when (response) {
			// if the network call was successful, compare the data
			is Resource.Success -> {
				// get the saved earthquakes
				earthquakeDao.getAll().collectLatest { savedEarthquakes ->
					// map the loaded earthquakes to the database model
					val loadedEarthquakes = response.data!!.mapNotNull { toEarthquake(it) }
					// get the differences
					updates = getDifferences(loadedEarthquakes, savedEarthquakes)
					// if there are any updates
					if (updates!!.hasUpdates())
					// update the database
						earthquakeDao.upsertAll(loadedEarthquakes)
				}
			}

			else -> {
				// if the network call failed, return null
			}
		}
		return updates
	}

	companion object {
		@Volatile
		private var instance: EarthquakesRepository? = null

		fun getInstance(earthquakeDao: EarthquakeDao) =
			instance ?: synchronized(this) {
				instance ?: EarthquakesRepository(earthquakeDao).also { instance = it }
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

	// Check for wich events the finite source has been updated
	val updatedEarthquakes = loadedEarthquakes.filter { new ->
		savedEarthquakes.any { old ->
			new.id == old.id && new.finiteSourceLastUpdate?.after(
				old.finiteSourceLastUpdate
			) ?: false
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

// Function to make a safe api call
private suspend fun <T> safeApiCall(apiToBeCalled: suspend () -> Response<T>): Resource<T> {
	return withContext(Dispatchers.IO) {
		try {
			val response: Response<T> = apiToBeCalled()
			if (response.isSuccessful)
				Resource.Success(data = response.body()!!)
			else
				Resource.Error(errorMessage = "Something went wrong")
		} catch (e: HttpException) {
			Resource.Error(errorMessage = e.message ?: "Something went wrong")
		} catch (e: IOException) {
			Resource.Error("Please check your network connection")
		} catch (e: Exception) {
			Resource.Error(errorMessage = "Something went wrong")
		}
	}
}