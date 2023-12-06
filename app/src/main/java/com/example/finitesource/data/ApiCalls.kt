package com.example.finitesource.data

import com.example.finitesource.data.earthquake.Earthquake
import com.example.finitesource.data.earthquake.Footprints
import com.example.finitesource.data.earthquake.focalplane.FiniteSource
import com.example.finitesource.data.earthquake.focalplane.FocalPlaneType
import com.example.finitesource.data.earthquake.focalplane.Scenarios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.EventApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.CatalogEventIdEventDetailsJsonGet200Response
import retrofit2.Call
import javax.inject.Inject

class ApiCalls @Inject constructor(private val apiClient: ApiClient) {
	// loads the finite source and adds it to the earthquake details
	fun getFiniteSource(earthquake: Earthquake, focalPlaneType: FocalPlaneType): FiniteSource? {
		return null
	}

	fun getScenarios(earthquake: Earthquake, fP1: FocalPlaneType): Scenarios? {
		return null
	}

	fun getFootprints(earthquake: Earthquake): Footprints {
		// TODO
		return Footprints("", "")
	}

	fun getEventDetails(id: String): Resource<CatalogEventIdEventDetailsJsonGet200Response> {
		return apiClient
			.createService(EventApi::class.java)
			.catalogEventIdEventDetailsJsonGet(id)
			.executeApiCall()
	}
}


suspend fun <T> Call<T>.executeSafeApiCall(): Resource<T> {
	return withContext(Dispatchers.IO) {
		this@executeSafeApiCall.executeApiCall()
	}
}

// function to make a network call and return a Resource
fun <T> Call<T>.executeApiCall(): Resource<T> {
	return try {
		val response = this.execute()
		if (response.isSuccessful)
			Resource.Success(data = response.body()!!)
		else
			Resource.Error(errorMessage = "Something went wrong")
		// TODO handle errors
	} catch (e: Exception) {
		Resource.Error(errorMessage = "Something went wrong")
	}
}