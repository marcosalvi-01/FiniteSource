package it.ingv.finitesource.data.remote

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import it.ingv.finitesource.data.EarthquakesRepository
import it.ingv.finitesource.data.local.Products
import it.ingv.finitesource.data.local.earthquake.Earthquake
import it.ingv.finitesource.data.local.earthquake.Footprints
import it.ingv.finitesource.data.local.earthquake.focalplane.FiniteSource
import it.ingv.finitesource.data.local.earthquake.focalplane.FocalPlaneType
import it.ingv.finitesource.data.local.earthquake.focalplane.Scenario
import it.ingv.finitesource.data.local.earthquake.focalplane.ScenarioType
import it.ingv.finitesource.data.local.earthquake.focalplane.Scenarios
import it.ingv.finitesource.data.local.earthquake.focalplane.geojson.CustomGeoJson
import it.ingv.finitesource.getLocaleSuffix
import it.ingv.finitesource.okHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.openapitools.client.apis.DefaultApi
import org.openapitools.client.infrastructure.ApiClient
import retrofit2.Call
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
                    if (scenario.providerName!! == scenarioType.name) {
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

    fun copyZipUrlToClipboard(
        earthquakeId: String,
        focalPlaneType: FocalPlaneType,
        context: Context
    ): Boolean = try {
        val events = service.queryGet(id = earthquakeId).executeApiCall()
        if (events.eventCount != 1.toLong()) {
            throw Exception("The service returned a wrong number of events: " + events.eventCount)
        }
        val event = events.events[0]

        // Get the zip URL
        val zipUrl = event.finiteSource?.first {
            it.focalPlane!!.intValueExact() == focalPlaneType.ordinal + 1
        }?.productLink ?: throw Exception("Error getting zip url")

        // Copy URL to clipboard
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Zip File URL", zipUrl)
        clipboardManager.setPrimaryClip(clipData)

        // Return true to indicate that the URL was successfully copied to clipboard
        true
    } catch (e: Exception) {
        // Print the stack trace of the exception and return false to indicate that copying failed
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
