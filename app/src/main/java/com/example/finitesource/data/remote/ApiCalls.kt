package com.example.finitesource.data.remote

import android.os.Environment
import com.example.finitesource.data.EarthquakesRepository
import com.example.finitesource.data.local.Products
import com.example.finitesource.data.local.earthquake.Earthquake
import com.example.finitesource.data.local.earthquake.Footprints
import com.example.finitesource.data.local.earthquake.focalplane.FiniteSource
import com.example.finitesource.data.local.earthquake.focalplane.FocalPlaneType
import com.example.finitesource.data.local.earthquake.focalplane.Scenario
import com.example.finitesource.data.local.earthquake.focalplane.ScenarioType
import com.example.finitesource.data.local.earthquake.focalplane.Scenarios
import com.example.finitesource.data.local.earthquake.focalplane.geojson.CustomGeoJson
import com.example.finitesource.getLocaleSuffix
import com.example.finitesource.okHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okio.BufferedSink
import okio.buffer
import okio.sink
import org.openapitools.client.apis.DefaultApi
import org.openapitools.client.infrastructure.ApiClient
import retrofit2.Call
import java.io.File
import javax.inject.Inject

class ApiCalls @Inject constructor(
    private val apiClient: ApiClient,
) {
    // TODO
    // the gets for the products return null if the product is not available but also
    // if there is an error.
    // In the app, in both cases the product is shown as not available instead of showing an error
    // The only exception is the Finite Source, because its availability is shown to the user through
    // the color of the markers, so if there is an error, the user should be notified somehow

    private val service = apiClient.createService(DefaultApi::class.java)

    // loads the finite source
    fun getFiniteSource(earthquake: Earthquake, focalPlaneType: FocalPlaneType): FiniteSource? =
        try {
            val events = service.queryGet(id = earthquake.id).executeApiCall()
            if (events.eventCount != 1.toLong()) {
                throw Exception("The service returned a wrong number of events: " + events.eventCount)
            }
            val event = events.events[0]
            val finiteSource = event.finiteSource?.first {
                println(it.focalPlane?.intValueExact())
                it.focalPlane != null && it.focalPlane.intValueExact() == focalPlaneType.ordinal + 1
            }

            if (finiteSource == null) {
                throw Exception("No finite source found")
            }

            val inversionDescription = if (getLocaleSuffix() == "it")
                finiteSource.inversionDescriptionCaptionIt
            else finiteSource.inversionDescriptionCaptionEn

            val resultDescription =
                if (getLocaleSuffix() == "it")
                    finiteSource.sourceGraphicCaptionIt
                else finiteSource.sourceGraphicCaptionEn

            val mainInversionMapImageUrl = finiteSource.sourceGraphicsMap

            val slipDistributionImageUrl = finiteSource.slipDistributionGraphic

            val source = okHttpClient.newCall(
                Request.Builder()
                    .url(finiteSource.sourceJson!!)
                    .build()
            ).execute().let {
                if (it.isSuccessful) {
                    it.body?.string()
                } else {
                    throw Exception("Error getting geojson")
                }
            }

            FiniteSource(
                inversionDescription!!,
                resultDescription!!,
                mainInversionMapImageUrl!!,
                slipDistributionImageUrl!!,
                CustomGeoJson.parseString(source!!)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    fun getScenarios(
        earthquake: Earthquake,
        focalPlaneType: FocalPlaneType,
        availableScenarios: List<ScenarioType>
    ): Scenarios? =
        try {
            val events = service.queryGet(id = earthquake.id).executeApiCall()
            if (events.eventCount != 1.toLong()) {
                throw Exception("The service returned a wrong number of events: " + events.eventCount)
            }
            val event = events.events[0]

            val scenariosDescription = try {
                if (getLocaleSuffix() == "it")
                    event.scenarios!!.scenarioOverallCaptionIt
                else
                    event.scenarios!!.scenarioOverallCaptionEn
            } catch (e: Exception) {
                null
            }

            val scenarios: MutableList<Scenario> = mutableListOf()
            for (scenarioType in availableScenarios) {
                for (scenario in event.scenarios!!.providerDetails) {
                    if (scenario.providerName!! == scenarioType.id) {
                        for (product in scenario.products!!) {
                            if (product.focalPlane!!.intValueExact() == focalPlaneType.ordinal + 1) {
                                val displacementMapDescription =
                                    if (getLocaleSuffix() == "it") product.predictedDisplacementCaptionIt else product.predictedDisplacementCaptionEn

                                val predictedFringesDescription =
                                    if (getLocaleSuffix() == "it") product.predictedFringesCaptionIt else product.predictedFringesCaptionEn
                                val displacementMapImageUrl = product.predictedDisplacementGraphics

                                val predictedFringesImageUrl = product.predictedFringesGraphics
                                scenarios.add(
                                    Scenario(
                                        scenarioType,
                                        displacementMapDescription!!,
                                        displacementMapImageUrl!!,
                                        predictedFringesDescription,
                                        predictedFringesImageUrl
                                    )
                                )
                            }
                        }
                    }
                }
            }
            if (scenarios.isEmpty())
                null    // should not happen
            else
                Scenarios(scenarios, scenariosDescription)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    fun getFootprints(earthquake: Earthquake): Footprints? = try {
//        val sentinelFootprintUrl = footprintsService.catalogEventIdANCILLARYSentinelFootprintJpgGet(
//            earthquake.id
//        ).request().url.toString()
        val events = service.queryGet(id = earthquake.id).executeApiCall()
        if (events.eventCount != 1.toLong()) {
            throw Exception("The service returned a wrong number of events: " + events.eventCount)
        }

        val event = events.events[0]
        val sentinelFootprintUrl = event.footprints?.footprintJpg

        val sentinelFootprintDescription =
            if (getLocaleSuffix() == "it") event.footprints?.footprintCaptionIt
            else event.footprints?.footprintCaptionEn
//        try {
//            footprintsService.catalogEventIdANCILLARYFootprintDescriptionLanguageTxtGet(
//                earthquake.id,
//                getLocaleSuffix()
//            ).executeApiCall().string()
//        } catch (e: Exception) {
//            null
//        }

        Footprints(
            sentinelFootprintUrl!!,
            sentinelFootprintDescription,
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun getEventDetails(id: String): EventDetails {
        val events = service.queryGet(id = id).executeApiCall()
        if (events.eventCount != 1.toLong()) {
            throw Exception("The service returned a wrong number of events: " + events.eventCount)
        }
        val event = events.events[0]
        return EventDetails(
            idIngv = event.idIngv,
            products = event.availableProducts.map { it.value },
            focalPlane = event.focalPlane.intValueExact()
        )
    }

    class EventDetails(
        val products: List<String>?,
        val focalPlane: Int,
        val idIngv: Long?,
    )

    fun getAvailableProducts(id: String): List<Products> {
        return getEventDetails(id).products?.map {
            Products.parseString(it)
        } ?: throw Exception("Error loading the event details")
    }

    fun getAvailableScenarios(id: String): List<ScenarioType>? {
        val events = service.queryGet(id = id).executeApiCall()
        if (events.eventCount != 1.toLong()) {
            throw Exception("The service returned a wrong number of events: " + events.eventCount)
        }
        val event = events.events[0]
        return event.scenarios?.providers?.map {
            EarthquakesRepository.parseScenarioType(it)
        }
//        return getScenarioDetails(id).providers?.map {
//            EarthquakesRepository.parseScenarioType(it)
//        } ?: throw Exception("Error loading the scenario details")
    }

    /**
     * Downloads the zip file containing data and model for a specific earthquake and focal plane.
     *
     * This function uses the `FiniteSourceApi` service to start the download of the zip file.
     * The zip file is saved in the public downloads directory of the device with the provided destination file name.
     *
     * @param earthquakeId The id of the earthquake for which the zip file is to be downloaded.
     * @param focalPlaneType The type of the focal plane for which the zip file is to be downloaded. See [FocalPlaneType].
     * @param destinationFileName The name of the file in which the downloaded zip file is to be saved.
     * @return A Boolean indicating whether the download was successful. Returns true if the download was successful, false otherwise.
     */
    fun downloadZipToFile(
        earthquakeId: String,
        focalPlaneType: FocalPlaneType,
        destinationFileName: String
    ): Boolean = try {
        // Create a file in the public downloads directory with the provided destination file name
        val destinationFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            destinationFileName
        )

        val events = service.queryGet(id = earthquakeId).executeApiCall()
        if (events.eventCount != 1.toLong()) {
            throw Exception("The service returned a wrong number of events: " + events.eventCount)
        }
        val event = events.events[0]
        // Start the download of the zip file
        val response = okHttpClient.newCall(
            Request.Builder()
                .url(event.finiteSource?.first {
                    it.focalPlane!!.intValueExact() == focalPlaneType.ordinal + 1
                }?.productLink ?: throw Exception("Error getting zip url"))
                .build()
        ).execute().let {
            if (it.isSuccessful) {
                it.body
            } else {
                throw Exception("Error getting zip")
            }
        }

        // Write the downloaded data to the destination file
        val sink: BufferedSink = destinationFile.sink().buffer()
        sink.writeAll(response!!.source())
        sink.close()

        // Return true to indicate that the download was successful
        true
    } catch (e: Exception) {
        // Print the stack trace of the exception and return false to indicate that the download failed
        e.printStackTrace()
        false
    }
}


suspend fun <T> Call<T>.executeSafeApiCall(): T? {
    return withContext(Dispatchers.IO) {
        this@executeSafeApiCall.executeApiCall()
    }
}

/**
 * Executes the call and returns the body if the response is successful, otherwise returns null
 */
fun <T> Call<T>.executeApiCall(): T {
    return try {
        val response = this.execute()
        if (response.isSuccessful)
            response.body()!!
        else
            throw Exception(
                "\nCall: " + this.request().url + "\nunsuccessful: " + response.errorBody()
                    ?.string()
            )
    } catch (e: Exception) {
        // TODO handle the error
        e.printStackTrace()
        throw e
    }
}
