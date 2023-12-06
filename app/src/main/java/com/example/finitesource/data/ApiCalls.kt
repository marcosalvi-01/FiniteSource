package com.example.finitesource.data

import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.Footprints
import com.example.finitesource.data.earthquake.focalplane.FiniteSource
import com.example.finitesource.data.earthquake.focalplane.FocalPlaneType
import com.example.finitesource.data.earthquake.focalplane.Scenario
import com.example.finitesource.data.earthquake.focalplane.ScenarioType
import com.example.finitesource.data.earthquake.focalplane.Scenarios
import com.example.finitesource.getLocaleSuffix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.EventApi
import org.openapitools.client.apis.FiniteSourceApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.CatalogEventIdEventDetailsJsonGet200Response
import retrofit2.Call
import javax.inject.Inject

class ApiCalls @Inject constructor(private val apiClient: ApiClient) {

	private val finiteSourceService = apiClient.createService(FiniteSourceApi::class.java)

	// loads the finite source
	fun getFiniteSource(earthquake: Earthquake, focalPlaneType: FocalPlaneType): FiniteSource? {
		return try {
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

			FiniteSource(
				inversionDescription,
				resultDescription,
				mainInversionMapImageUrl,
				slipDistributionImageUrl
			)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}

	fun getScenarios(earthquake: Earthquake, focalPlaneType: FocalPlaneType): Scenarios? {
		// TODO
		return Scenarios(
			listOf(
				Scenario(
					ScenarioType("1", "2", "3"),
					"4", "5", "6", "7"
				)
			)
		)
	}

	fun getFootprints(earthquake: Earthquake): Footprints {
		// TODO
		return Footprints("", "")
	}

	fun getEventDetails(id: String): CatalogEventIdEventDetailsJsonGet200Response? {
		return apiClient
			.createService(EventApi::class.java)
			.catalogEventIdEventDetailsJsonGet(id)
			.executeApiCall()
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
				"Call: " + this.request().url + "\nunsuccessful: " + response.errorBody()?.string()
			)
	} catch (e: Exception) {
		// TODO handle the error
		e.printStackTrace()
		throw e
	}
}
