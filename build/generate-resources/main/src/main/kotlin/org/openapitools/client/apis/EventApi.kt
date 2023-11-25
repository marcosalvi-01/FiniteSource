package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.squareup.moshi.Json

import org.openapitools.client.models.CatalogEventIdEventDetailsJsonGet200Response

interface EventApi {
    /**
     * The details of the event
     * All the details of the event like the id, the date, the magnitude, the depth, the location, the number of the scenarios and the number of the finite sources.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @return [Call]<[CatalogEventIdEventDetailsJsonGet200Response]>
     */
    @GET("Catalog/{eventId}/event_details.json")
    fun catalogEventIdEventDetailsJsonGet(@Path("eventId") eventId: kotlin.String): Call<CatalogEventIdEventDetailsJsonGet200Response>

}
