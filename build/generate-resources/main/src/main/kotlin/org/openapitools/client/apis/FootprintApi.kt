package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import okhttp3.ResponseBody
import com.squareup.moshi.Json

import org.openapitools.client.models.CatalogEventIdANCILLARYFootprintsDetailsJsonGet200Response

interface FootprintApi {

    /**
    * enum for parameter language
    */
    enum class LanguageCatalogEventIdANCILLARYFootprintDescriptionLanguageTxtGet(val value: kotlin.String) {
        @Json(name = "it") `it`("it"),
        @Json(name = "en") en("en")
    }

    /**
     * The description of the footprint of the event
     * The description of the footprint of the event. The description is a text file.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param language The language of the description.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/ANCILLARY/FootprintDescription_{language}.txt")
    fun catalogEventIdANCILLARYFootprintDescriptionLanguageTxtGet(@Path("eventId") eventId: kotlin.String, @Path("language") language: kotlin.String): Call<ResponseBody>

    /**
     * The details of the footprint product for the event
     * The details of the footprint product for the event.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @return [Call]<[CatalogEventIdANCILLARYFootprintsDetailsJsonGet200Response]>
     */
    @GET("Catalog/{eventId}/ANCILLARY/footprints_details.json")
    fun catalogEventIdANCILLARYFootprintsDetailsJsonGet(@Path("eventId") eventId: kotlin.String): Call<CatalogEventIdANCILLARYFootprintsDetailsJsonGet200Response>

    /**
     * The image for the footprint of the event
     * The image for the footprint of the event. The image is a JPEG file.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/ANCILLARY/SentinelFootprint.jpg")
    fun catalogEventIdANCILLARYSentinelFootprintJpgGet(@Path("eventId") eventId: kotlin.String): Call<ResponseBody>

}
