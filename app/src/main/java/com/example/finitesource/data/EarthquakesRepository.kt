package com.example.finitesource.data

import com.example.finitesource.configUrl
import com.example.finitesource.data.local.CatalogConfig
import com.example.finitesource.data.local.EarthquakeUpdates
import com.example.finitesource.data.local.Products
import com.example.finitesource.data.local.database.dao.EarthquakeDao
import com.example.finitesource.data.local.database.dao.ScenarioTypeDao
import com.example.finitesource.data.local.earthquake.Earthquake
import com.example.finitesource.data.local.earthquake.EarthquakeDetails
import com.example.finitesource.data.local.earthquake.focalplane.FiniteSource
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlane
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlaneType
import com.example.finitesource.data.local.earthquake.focalplane.ScenarioType
import com.example.finitesource.data.local.earthquake.focalplane.Scenarios
import com.example.finitesource.data.local.earthquake.toEarthquake
import com.example.finitesource.data.remote.ApiCalls
import com.example.finitesource.data.remote.executeApiCall
import com.example.finitesource.okHttpClient
import com.example.finitesource.paletteUrl
import com.example.finitesource.providersUrl
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.Request
import org.openapitools.client.apis.DefaultApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.Event
import javax.inject.Inject

const val ZIP_FILE_NAME = "data_and_model.zip"

class EarthquakesRepository @Inject constructor(
    private val earthquakeDao: EarthquakeDao,
    private val scenarioTypeDao: ScenarioTypeDao,
    private val apiClient: ApiClient,
    private val apiCalls: ApiCalls,
) {

    fun getAll() = earthquakeDao.getAll()

    fun getById(id: String) = earthquakeDao.getById(id)

    // save the updates done this time to show them to the user
    var catalogUpdates: EarthquakeUpdates? = null

    suspend fun getUpdatesFromRemote(): EarthquakeUpdates? = try {
        updateConfig()
        catalogUpdates = updateEarthquakes()
        catalogUpdates
    } catch (e: Exception) { // TODO handle errors using the correct error type
        e.printStackTrace()
        null
    }

    @JsonClass(generateAdapter = true)
    internal data class Config(
        @Json(name = "server_ingv_it")
        val serverIngvIt: String,

        @Json(name = "server_ingv_en")
        val serverIngvEn: String,

        @Json(name = "palette_version")
        val paletteVersion: Int
    )

    @JsonClass(generateAdapter = true)
    data class Provider(
        @Json(name = "provider_dir")
        val providerDir: String,

        @Json(name = "provider_name")
        val providerName: String,

        @Json(name = "provider_url")
        val providerUrl: String
    )

    private suspend fun updateConfig() {
        // build the request
        val generalConfigResponse = okHttpClient.newCall(
            Request.Builder()
                .url(configUrl)
                .build()
        ).execute().let {
            if (it.isSuccessful) {
                it.body?.string()
            } else {
                throw Exception("Error getting")
            }
        }
        val moshi = Moshi.Builder().build()
        val adapterConfig = moshi.adapter(Config::class.java)
        val serverConfig: Config? = adapterConfig.fromJson(generalConfigResponse!!)

        val providersResponse = okHttpClient.newCall(
            Request.Builder()
                .url(providersUrl)
                .build()
        ).execute().let {
            if (it.isSuccessful) {
                it.body?.string()
            } else {
                throw Exception("Error getting")
            }
        }
        val listTypeParameters = Types.newParameterizedType(List::class.java, Provider::class.java)
        val adapterParameters = moshi.adapter<List<Provider>>(listTypeParameters)

        val providers = adapterParameters.fromJson(providersResponse!!)

        // update the ScenarioType table
        val scenarioTypes = providers?.map {
            ScenarioType(it.providerDir, it.providerName, it.providerUrl)
        } ?: throw Exception("Error parsing scenario providers")
        scenarioTypeDao.upsertAll(scenarioTypes)

        // update the general configs, if for some reason it doesn't work, it will use the old values
        val newSlipPalette = CatalogConfig.update(
            serverConfig!!.serverIngvEn,
            serverConfig.serverIngvIt,
            serverConfig.paletteVersion
        )
        // if the slip color palette has been updated, update the palette
        if (newSlipPalette) {
            val paletteResponse = okHttpClient.newCall(
                Request.Builder()
                    .url(paletteUrl)
                    .build()
            ).execute().let {
                if (it.isSuccessful) {
                    it.body?.string()
                } else {
                    throw Exception("Error getting")
                }
            }
            val listType = Types.newParameterizedType(List::class.java, String::class.java)
            val adapter = moshi.adapter<List<String>>(listType)
            // update the general configs, if for some reason it doesn't work, it will use the old values
            CatalogConfig.updateSlipColorPalette(adapter.fromJson(paletteResponse!!)!!)
        }
    }

    // loads the latest data from the finite source api and compares it to the saved data
    // returns the differences that are supposed to be shown to the user
    private suspend fun updateEarthquakes(): EarthquakeUpdates {
        // TODO remove this
//		earthquakeDao.clearDatabase()
        // build the request
        val eventMetadata = apiClient
            .createService(DefaultApi::class.java)
            .queryGet().executeApiCall()
        // execute the request, return null if it fails
        val response: List<Event>?
        response = eventMetadata.events
        // get the saved earthquakes
        val savedEarthquakes = earthquakeDao.getAll().first().toSet()
        // map the loaded earthquakes to the database model
        val loadedEarthquakes = response.mapNotNull { toEarthquake(it) }.toSet()
        // get the differences
        val updates = getDifferences(loadedEarthquakes, savedEarthquakes)

        // update the database
        updateDatabase(updates)

        return updates
    }

    /**
     * Updates the database with the latest earthquake data.
     *
     * @param updates An instance of the EarthquakeUpdates data class which contains information about new and updated earthquakes.
     * @throws Exception This function will throw an exception if there is an error loading the details of an earthquake.
     */
    private suspend fun updateDatabase(updates: EarthquakeUpdates) {
        // remove the earthquakes that have been removed
        earthquakeDao.deleteAll(updates.removedEarthquakes)

        // for these earthquakes, the details might be loaded, so they might need to be re-downloaded
        val updatedEarthquakes = updates.finiteSourceUpdated + updates.newFiniteSource

        // Update the database with the new finite source earthquakes
        earthquakeDao.upsertAll(updates.newFiniteSource)

        // Iterate over each updated earthquake
        for (updatedEarthquake in updatedEarthquakes) {
            // If the earthquake is not loaded (i.e., it is not present in the database), skip the current iteration
            val loadedEarthquake = getById(updatedEarthquake.id).firstOrNull() ?: continue

            // If the details of the loaded earthquake are null, skip the current iteration
            if (loadedEarthquake.details == null)
                continue

            // Download the details of the updated earthquake and update the database
            loadEarthquakeDetails(updatedEarthquake.id)
        }

        // Update the database with the new earthquakes
        earthquakeDao.upsertAll(updates.newEarthquakes)
    }


    /**
     * Loads the details of the earthquake with the given id and updates the database with the new details.
     * Returns null if there is an error loading the details.
     */
    suspend fun loadEarthquakeDetails(id: String): Earthquake? {
        // TODO load the products in parallel
        try {
            val earthquake = getById(id).first()
            // if this earthquake is already loaded, return
            if (earthquake.details != null)
                return earthquake

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
                if (eventDetailsResponse.idIngv != 0L) eventDetailsResponse.idIngv else null,
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

    /**
     * Initiates the download of a zip file for a specific earthquake and focal plane type.
     *
     * This function uses the `apiCalls` object to start the download of a zip file for the given earthquake and focal plane type.
     * The zip file is saved with a name that includes the id of the earthquake.
     *
     * @param earthquake The earthquake for which the zip file is to be downloaded.
     * @param focalPlaneType The type of the focal plane for which the zip file is to be downloaded.
     * @return A Boolean indicating whether the download was successful. Returns true if the download was successful, false otherwise.
     */
    fun downloadZipToFile(earthquake: Earthquake, focalPlaneType: FocalPlaneType): Boolean {
        val destinationFileName = ZIP_FILE_NAME.replace(".zip", "_${earthquake.id}.zip")
        return apiCalls.downloadZipToFile(earthquake.id, focalPlaneType, destinationFileName)
    }

    fun isFirstRun(firstRun: Boolean) {
        if (firstRun)
            catalogUpdates = null
    }

    companion object {
        @Volatile
        private var instance: EarthquakesRepository? = null

        fun getInstance(
            earthquakeDao: EarthquakeDao,
            scenarioTypeDao: ScenarioTypeDao,
            apiClient: ApiClient,
            apiCalls: ApiCalls,
        ) =
            instance ?: synchronized(this) {
                instance ?: EarthquakesRepository(
                    earthquakeDao,
                    scenarioTypeDao,
                    apiClient,
                    apiCalls,
                ).also { instance = it }
            }

        // TODO find a better way to do this
        fun parseScenarioType(id: String): ScenarioType {
            return instance!!.scenarioTypeDao.getById(id)
        }
    }
}

// helper function to load the products of an earthquake in a specific focal plane
// TODO load the products in parallel
private fun buildFocalPlane(
    earthquake: Earthquake,
    focalPlaneType: FocalPlaneType,
    apiCalls: ApiCalls,
    availableProducts: List<Products>,
    availableScenarios: List<ScenarioType>?
): FocalPlane {
    var scenarios: Scenarios? = null
    var finiteSource: FiniteSource? = null

    for (product in availableProducts) {
        when (product) {
            Products.SCENARIOS -> {
                if (availableScenarios == null) {
                    throw Exception("No scenarios found even though they should be available")
                }
                scenarios = apiCalls.getScenarios(earthquake, focalPlaneType, availableScenarios)
//                if (scenarios == null)
//                    throw Exception("Error loading the scenarios")
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
        availableProducts,
    )
}

// function to get the differences between the old and the new data
private fun getDifferences(
    loadedEarthquakes: Set<Earthquake>,
    savedEarthquakes: Set<Earthquake>
): EarthquakeUpdates {
    // TODO test this function
    // Check if there are any new earthquakes
    val newEarthquakes = loadedEarthquakes - savedEarthquakes
    // Check if there are any earthquakes that have been removed
    val removedEarthquakes = savedEarthquakes - loadedEarthquakes

    // the events that had their finite source updated
    val updatedEarthquakes = mutableSetOf<Earthquake>()
    // the events that had their finite source added
    val newFiniteSource = mutableSetOf<Earthquake>()

    // for each new earthquake, check it
    for (new in loadedEarthquakes) {
        // find its saved version
        val savedEarthquake = savedEarthquakes.find { it.id == new.id } ?: continue

        // if the old event has no finite source, but the new one has one, add it to the new finite source set
        if (new.finiteSourceLastUpdate != null && savedEarthquake.finiteSourceLastUpdate == null)
            newFiniteSource.add(new)
        // if the new event has a finite source more recent than the old one, add it to the updated earthquakes set
        else if (new.finiteSourceLastUpdate?.after(savedEarthquake.finiteSourceLastUpdate) == true)
            updatedEarthquakes.add(new)
    }

    return EarthquakeUpdates(
        newEarthquakes,
        updatedEarthquakes,
        newFiniteSource,
        removedEarthquakes,
    )
}