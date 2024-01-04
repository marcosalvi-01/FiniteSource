package com.example.finitesource.data.remote

import com.example.finitesource.data.local.CatalogConfig
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.EventApi
import org.openapitools.client.apis.FiniteSourceApi
import org.openapitools.client.apis.FootprintApi
import org.openapitools.client.apis.ScenariosApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.CatalogEventIdEventDetailsJsonGet200Response
import org.openapitools.client.models.CatalogEventIdFOCMECHFWDScenariosDetailsJsonGet200Response
import retrofit2.Call
import javax.inject.Inject

class ApiCalls @Inject constructor(private val apiClient: ApiClient) {
	// TODO
	// the gets for the products return null if the product is not available but also
	// if there is an error.
	// In the app, in both cases the product is shown as not available instead of showing an error
	// The only exception is the Finite Source, because its availability is shown to the user through
	// the color of the markers, so if there is an error, the user should be notified somehow

	private val finiteSourceService = apiClient.createService(FiniteSourceApi::class.java)
	private val scenariosService = apiClient.createService(ScenariosApi::class.java)
	private val footprintsService = apiClient.createService(FootprintApi::class.java)

	// loads the finite source
	fun getFiniteSource(earthquake: Earthquake, focalPlaneType: FocalPlaneType): FiniteSource? =
		try {
			val inversionDescription =
				finiteSourceService.catalogEventIdINVERSEInversionDescriptionLanguageTxtGet(
					earthquake.id,
					getLocaleSuffix()
				).executeApiCall().string()

			val resultDescription =
				finiteSourceService.catalogEventIdINVERSEFocalPlaneGRAPHICSResultDescriptionLanguageTxtGet(
					earthquake.id,
					focalPlaneType.name,
					getLocaleSuffix()
				).executeApiCall().string()

			val mainInversionMapImageUrl =
				finiteSourceService.catalogEventIdINVERSEFocalPlaneGRAPHICSMainInversionMapJpgGet(
					earthquake.id,
					getLocaleSuffix()
				).request().url.toString()

			val slipDistributionImageUrl =
				finiteSourceService.catalogEventIdINVERSEFocalPlaneGRAPHICSMainInversionMapJpgGet(
					earthquake.id,
					getLocaleSuffix()
				).request().url.toString()

			val source =
				finiteSourceService.catalogEventIdINVERSEFocalPlaneSOURCESGeoJsonFileNameGet(
					earthquake.id,
					focalPlaneType.name,
					CatalogConfig.sourceGeoJsonFileName
				).executeApiCall().string()

			FiniteSource(
				inversionDescription,
				resultDescription,
				mainInversionMapImageUrl,
				slipDistributionImageUrl,
				CustomGeoJson.parseString(source)
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
			val scenarios: MutableList<Scenario> = mutableListOf()
			for (scenarioType in availableScenarios) {
				val displacementMapDescription =
					scenariosService.catalogEventIdFOCMECHFWDScenarioIdFocalPlaneGRAPHICSDisplacementMapLanguageTxtGet(
						earthquake.id,
						scenarioType.id,
						focalPlaneType.name,
						getLocaleSuffix()
					).executeApiCall().string()

				val predictedFringesDescription =
					scenariosService.catalogEventIdFOCMECHFWDScenarioIdFocalPlaneGRAPHICSPredictedFringesLanguageTxtGet(
						earthquake.id,
						scenarioType.id,
						focalPlaneType.name,
						getLocaleSuffix()
					).executeApiCall().string()

				val displacementMapImageUrl =
					scenariosService.catalogEventIdFOCMECHFWDScenarioIdFocalPlaneGRAPHICSDisplacementMapJpgGet(
						earthquake.id,
						scenarioType.id,
						focalPlaneType.name,
					).request().url.toString()

				val predictedFringesImageUrl =
					scenariosService.catalogEventIdFOCMECHFWDScenarioIdFocalPlaneGRAPHICSPredictedFringesJpgGet(
						earthquake.id,
						scenarioType.id,
						focalPlaneType.name,
					).request().url.toString()

				scenarios.add(
					Scenario(
						scenarioType,
						displacementMapDescription,
						predictedFringesDescription,
						displacementMapImageUrl,
						predictedFringesImageUrl
					)
				)
			}
			if (scenarios.isEmpty())
				null    // should not happen
			else
				Scenarios(scenarios)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}

	fun getFootprints(earthquake: Earthquake): Footprints? = try {
		Footprints(
			footprintsService.catalogEventIdANCILLARYSentinelFootprintJpgGet(
				earthquake.id
			).request().url.toString(),
			footprintsService.catalogEventIdANCILLARYFootprintDescriptionLanguageTxtGet(
				earthquake.id,
				getLocaleSuffix()
			).executeApiCall().string(),
		)
	} catch (e: Exception) {
		e.printStackTrace()
		null
	}

	fun getEventDetails(id: String): CatalogEventIdEventDetailsJsonGet200Response {
		return apiClient
			.createService(EventApi::class.java)
			.catalogEventIdEventDetailsJsonGet(id)
			.executeApiCall()
	}

	fun getAvailableProducts(id: String): List<Products> {
		return getEventDetails(id).products?.map {
			Products.parseString(it)
		} ?: throw Exception("Error loading the event details")
	}

	/**
	 * Loads the details of the scenarios for the given earthquake and focal plane.
	 * Throws an exception if the details cannot be loaded.
	 */
	fun getScenarioDetails(id: String):
			CatalogEventIdFOCMECHFWDScenariosDetailsJsonGet200Response {
		return scenariosService
			.catalogEventIdFOCMECHFWDScenariosDetailsJsonGet(id)
			.executeApiCall()
	}

	fun getAvailableScenarios(id: String): List<ScenarioType> {
		return getScenarioDetails(id).providers?.map {
			ScenarioType.parseString(it)
		} ?: throw Exception("Error loading the scenario details")
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
