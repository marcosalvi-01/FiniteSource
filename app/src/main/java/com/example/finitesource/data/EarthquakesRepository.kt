package com.example.finitesource.data

import com.example.finitesource.data.database.dao.EarthquakeDao
import com.example.finitesource.data.database.dao.ScenarioTypeDao
import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.EarthquakeDetails
import com.example.finitesource.data.earthquake.EarthquakeUpdates
import com.example.finitesource.data.earthquake.focalplane.FiniteSource
import com.example.finitesource.data.earthquake.focalplane.FocalPlane
import com.example.finitesource.data.earthquake.focalplane.FocalPlaneType
import com.example.finitesource.data.earthquake.focalplane.ScenarioType
import com.example.finitesource.data.earthquake.focalplane.Scenarios
import com.example.finitesource.data.earthquake.toEarthquake
import kotlinx.coroutines.flow.first
import org.openapitools.client.apis.ConfigurationFilesApi
import org.openapitools.client.apis.FiniteSourceAndroidAppApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.FiniteSourceAppAppJsonGet200ResponseInner
import javax.inject.Inject

class EarthquakesRepository @Inject constructor(
	private val earthquakeDao: EarthquakeDao,
	private val scenarioTypeDao: ScenarioTypeDao,
	private val apiClient: ApiClient
) {
	fun getAll() = earthquakeDao.getAll()

	fun getById(id: String) = earthquakeDao.getById(id)

	suspend fun getUpdates(): EarthquakeUpdates? = try {
		updateConfig()
		updateEarthquakes()
	} catch (e: Exception) { // TODO handle errors using the correct error type
		null
	}

	private suspend fun updateConfig() {
		// build the request
		val generalConfigResponse = apiClient
			.createService(ConfigurationFilesApi::class.java)
			.configGeneralConfigJsonGet().executeApiCall()
		val scenariosProvidersResponse = apiClient
			.createService(ConfigurationFilesApi::class.java)
			.configFocalMechanismProvidersJsonGet().executeApiCall()

		// update the ScenarioType table
		val scenarioTypes = scenariosProvidersResponse.map {
			ScenarioType(it.providerDir!!, it.providerName!!, it.providerUrl!!)
		}
		scenarioTypeDao.upsertAll(scenarioTypes)

		// update the general configs, if for some reason it doesn't work, it will use the old values
		CatalogConfig.update(generalConfigResponse)
	}

	// loads the latest data from the finite source api and compares it to the saved data
	// returns the differences that are supposed to be shown to the user
	private suspend fun updateEarthquakes(): EarthquakeUpdates {
		// TODO remove this
		earthquakeDao.deleteAll()
		// build the request
		val request = apiClient
			.createService(FiniteSourceAndroidAppApi::class.java)
			.finiteSourceAppAppJsonGet()
		// execute the request, return null if it fails
		val response: List<FiniteSourceAppAppJsonGet200ResponseInner>?
		response = request.executeApiCall()
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

	/**
	 * Loads the details of the earthquake with the given id.
	 * Returns null if there is an error loading the details.
	 */
	suspend fun loadEarthquakeDetails(id: String): Earthquake? {
		// TODO load the products in parallel
		try {
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
			val availableProducts = apiCalls.getAvailableProducts(earthquake.id)
			val availableScenarios = apiCalls.getAvailableScenarios(earthquake.id)
			// load the focal planes
			when (eventDetailsResponse.focalPlane) {
				0 -> {    // FP1 and FP2
					fp1 =
						buildFocalPlane(
							earthquake,
							FocalPlaneType.FP1,
							apiCalls,
							availableProducts,
							availableScenarios
						)
					fp2 =
						buildFocalPlane(
							earthquake,
							FocalPlaneType.FP2,
							apiCalls,
							availableProducts,
							availableScenarios
						)
				}

				// FP1
				1 -> fp1 =
					buildFocalPlane(
						earthquake,
						FocalPlaneType.FP1,
						apiCalls,
						availableProducts,
						availableScenarios
					)

				// FP2
				2 -> fp2 =
					buildFocalPlane(
						earthquake,
						FocalPlaneType.FP2,
						apiCalls,
						availableProducts,
						availableScenarios
					)

				else -> {
					throw Exception("Error, invalid focal plane number: ${eventDetailsResponse.focalPlane}")
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
		} catch (e: Exception) {
			e.printStackTrace()
			return null
		}
	}

	companion object {
		@Volatile
		private var instance: EarthquakesRepository? = null

		fun getInstance(
			earthquakeDao: EarthquakeDao,
			scenarioTypeDao: ScenarioTypeDao,
			apiClient: ApiClient
		) =
			instance ?: synchronized(this) {
				instance ?: EarthquakesRepository(
					earthquakeDao,
					scenarioTypeDao,
					apiClient,
				).also { instance = it }
			}
	}
}

// helper function to load the products of an earthquake in a specific focal plane
// TODO load the products in parallel
private fun buildFocalPlane(
	earthquake: Earthquake,
	focalPlaneType: FocalPlaneType,
	apiCalls: ApiCalls,
	availableProducts: List<Product>,
	availableScenarios: List<ScenarioType>
): FocalPlane {
	var scenarios: Scenarios? = null
	var finiteSource: FiniteSource? = null

	for (product in availableProducts) {
		when (product) {
			Products.SCENARIOS -> {
				scenarios = apiCalls.getScenarios(earthquake, focalPlaneType, availableScenarios)
				if (scenarios == null)
					throw Exception("Error loading the scenarios")
			}

			Products.FINITE_SOURCE -> {
				finiteSource =
					apiCalls.getFiniteSource(earthquake, focalPlaneType)
				if (finiteSource == null)
					throw Exception("Error loading the finite source")
			}

			else -> {
				// do nothing
			}
		}
	}

	return FocalPlane(
		focalPlaneType,
		scenarios,
		finiteSource,
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