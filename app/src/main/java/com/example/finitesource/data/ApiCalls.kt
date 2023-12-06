package com.example.finitesource.data

import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.Footprints
import com.example.finitesource.data.earthquake.focalplane.FiniteSource
import com.example.finitesource.data.earthquake.focalplane.FocalPlaneType
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
	// loads the finite source
	fun getFiniteSource(earthquake: Earthquake, focalPlaneType: FocalPlaneType): FiniteSource? {
		val finiteSourceService = apiClient.createService(FiniteSourceApi::class.java)
		try {
			return FiniteSource(
				// inversion description
				finiteSourceService.catalogEventIdINVERSEInversionDescriptionLanguageTxtGet(
					earthquake.id,
					getLocaleSuffix()
				).executeApiCall().string(),
				// result description
				finiteSourceService.catalogEventIdINVERSEFocalPlaneGRAPHICSResultDescriptionLanguageTxtGet(
					earthquake.id,
					focalPlaneType.name,
					getLocaleSuffix()
				).executeApiCall().string(),
				// main inversion map image url
				finiteSourceService.catalogEventIdINVERSEFocalPlaneGRAPHICSMainInversionMapJpgGet(
					earthquake.id,
					getLocaleSuffix()
				).request().url.toString(),
				// slip distribution image url
				finiteSourceService.catalogEventIdINVERSEFocalPlaneGRAPHICSMainInversionMapJpgGet(
					earthquake.id,
					getLocaleSuffix()
				).request().url.toString(),
			)
		} catch (e: Exception) {
			// TODO handle the error
			return null
		}
	}

	fun getScenarios(earthquake: Earthquake, focalPlaneType: FocalPlaneType): Scenarios? {
		return null
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
