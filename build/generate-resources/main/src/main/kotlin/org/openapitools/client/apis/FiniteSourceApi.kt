package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import okhttp3.ResponseBody
import com.squareup.moshi.Json

import org.openapitools.client.models.CatalogEventIdINVERSEFocalPlaneFiniteSourceDetailsJsonGet200Response
import org.openapitools.client.models.CatalogEventIdINVERSEFocalPlaneSOURCESGeoJsonFileNameJsonGet200Response

interface FiniteSourceApi {

    /**
    * enum for parameter focalPlane
    */
    enum class FocalPlaneCatalogEventIdINVERSEFocalPlaneDataAndModelZipGet(val value: kotlin.String) {
        @Json(name = "FP1") fP1("FP1"),
        @Json(name = "FP2") fP2("FP2")
    }

    /**
     * The original data used to create the finite source of the event
     * The original data used to create the finite source of the event. The data is contained in a .zip file.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param focalPlane The focal plane of the finite source.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/INVERSE/{focalPlane}/data_and_model.zip")
    fun catalogEventIdINVERSEFocalPlaneDataAndModelZipGet(@Path("eventId") eventId: kotlin.String, @Path("focalPlane") focalPlane: kotlin.String): Call<ResponseBody>


    /**
    * enum for parameter focalPlane
    */
    enum class FocalPlaneCatalogEventIdINVERSEFocalPlaneFiniteSourceDetailsJsonGet(val value: kotlin.String) {
        @Json(name = "FP1") fP1("FP1"),
        @Json(name = "FP2") fP2("FP2")
    }

    /**
     * The details of the finite source of the event
     * The details of the finite source of the event. The details are the same for both the focal planes.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param focalPlane The focal plane of the finite source.
     * @return [Call]<[CatalogEventIdINVERSEFocalPlaneFiniteSourceDetailsJsonGet200Response]>
     */
    @GET("Catalog/{eventId}/INVERSE/{focalPlane}/finite_source_details.json")
    fun catalogEventIdINVERSEFocalPlaneFiniteSourceDetailsJsonGet(@Path("eventId") eventId: kotlin.String, @Path("focalPlane") focalPlane: kotlin.String): Call<CatalogEventIdINVERSEFocalPlaneFiniteSourceDetailsJsonGet200Response>


    /**
    * enum for parameter focalPlane
    */
    enum class FocalPlaneCatalogEventIdINVERSEFocalPlaneGRAPHICSMainInversionMapJpgGet(val value: kotlin.String) {
        @Json(name = "FP1") fP1("FP1"),
        @Json(name = "FP2") fP2("FP2")
    }

    /**
     * The main inversion map of the finite source of the event
     * The main inversion map of the finite source of the event. The image is a JPEG file.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param focalPlane The focal plane of the finite source.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/INVERSE/{focalPlane}/GRAPHICS/MainInversionMap.jpg")
    fun catalogEventIdINVERSEFocalPlaneGRAPHICSMainInversionMapJpgGet(@Path("eventId") eventId: kotlin.String, @Path("focalPlane") focalPlane: kotlin.String): Call<ResponseBody>


    /**
    * enum for parameter focalPlane
    */
    enum class FocalPlaneCatalogEventIdINVERSEFocalPlaneGRAPHICSResultDescriptionLanguageTxtGet(val value: kotlin.String) {
        @Json(name = "FP1") fP1("FP1"),
        @Json(name = "FP2") fP2("FP2")
    }


    /**
    * enum for parameter language
    */
    enum class LanguageCatalogEventIdINVERSEFocalPlaneGRAPHICSResultDescriptionLanguageTxtGet(val value: kotlin.String) {
        @Json(name = "it") `it`("it"),
        @Json(name = "en") en("en")
    }

    /**
     * The description of the images of the finite source of the event
     * The description of the images of the finite source of the event. The description is a text file.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param focalPlane The focal plane of the finite source.
     * @param language The language of the description.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/INVERSE/{focalPlane}/GRAPHICS/ResultDescription_{language}.txt")
    fun catalogEventIdINVERSEFocalPlaneGRAPHICSResultDescriptionLanguageTxtGet(@Path("eventId") eventId: kotlin.String, @Path("focalPlane") focalPlane: kotlin.String, @Path("language") language: kotlin.String): Call<ResponseBody>


    /**
    * enum for parameter focalPlane
    */
    enum class FocalPlaneCatalogEventIdINVERSEFocalPlaneGRAPHICSSlipDistributionJpgGet(val value: kotlin.String) {
        @Json(name = "FP1") fP1("FP1"),
        @Json(name = "FP2") fP2("FP2")
    }

    /**
     * The slip distribution of the finite source of the event
     * The slip distribution of the finite source of the event. The image is a JPEG file.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param focalPlane The focal plane of the finite source.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/INVERSE/{focalPlane}/GRAPHICS/SlipDistribution.jpg")
    fun catalogEventIdINVERSEFocalPlaneGRAPHICSSlipDistributionJpgGet(@Path("eventId") eventId: kotlin.String, @Path("focalPlane") focalPlane: kotlin.String): Call<ResponseBody>


    /**
    * enum for parameter focalPlane
    */
    enum class FocalPlaneCatalogEventIdINVERSEFocalPlaneSOURCESGeoJsonFileNameJsonGet(val value: kotlin.String) {
        @Json(name = "FP1") fP1("FP1"),
        @Json(name = "FP2") fP2("FP2")
    }

    /**
     * The finite source of the event
     * The finite source of the event. The finite source is a GeoJson file. The GeoJson file contains a FeatureCollection with a Feature for each polygon of the finite source. Every Polygon has two properties, the slip and the rake. The GeoJson also contains the trace of the fault as a MultiLineString. The properties of the trace are empty.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param focalPlane The focal plane of the finite source.
     * @param geoJsonFileName The name of the GeoJson file. It is found in the /config/GeneralConfig.json file. It is the same for all the events.
     * @return [Call]<[CatalogEventIdINVERSEFocalPlaneSOURCESGeoJsonFileNameJsonGet200Response]>
     */
    @GET("Catalog/{eventId}/INVERSE/{focalPlane}/SOURCES/{geoJsonFileName}.json")
    fun catalogEventIdINVERSEFocalPlaneSOURCESGeoJsonFileNameJsonGet(@Path("eventId") eventId: kotlin.String, @Path("focalPlane") focalPlane: kotlin.String, @Path("geoJsonFileName") geoJsonFileName: kotlin.String): Call<CatalogEventIdINVERSEFocalPlaneSOURCESGeoJsonFileNameJsonGet200Response>


    /**
    * enum for parameter language
    */
    enum class LanguageCatalogEventIdINVERSEInversionDescriptionLanguageTxtGet(val value: kotlin.String) {
        @Json(name = "it") `it`("it"),
        @Json(name = "en") en("en")
    }

    /**
     * The description of the finite source of the event
     * The description of the finite source of the event. The description is a text file.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param language The language of the description.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/INVERSE/InversionDescription_{language}.txt")
    fun catalogEventIdINVERSEInversionDescriptionLanguageTxtGet(@Path("eventId") eventId: kotlin.String, @Path("language") language: kotlin.String): Call<ResponseBody>

}
