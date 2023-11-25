package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import okhttp3.ResponseBody
import com.squareup.moshi.Json

import org.openapitools.client.models.CatalogEventIdFOCMECHFOWScenarioDetailsJsonGet200Response

interface ScenariosApi {

    /**
    * enum for parameter language
    */
    enum class LanguageCatalogEventIdFOCMECHFOWFocMechFowDescriptionLanguageTxtGet(val value: kotlin.String) {
        @Json(name = "it") `it`("it"),
        @Json(name = "en") en("en")
    }

    /**
     * The description of the scenarios of the event
     * The description of the scenarios of the event. The description is a text file.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param language The language of the description.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/FOC_MECH_FOW/FocMechFowDescription_{language}.txt")
    fun catalogEventIdFOCMECHFOWFocMechFowDescriptionLanguageTxtGet(@Path("eventId") eventId: kotlin.String, @Path("language") language: kotlin.String): Call<ResponseBody>

    /**
     * The details of the scenario product for the event
     * The details of the scenario product for the event.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @return [Call]<[CatalogEventIdFOCMECHFOWScenarioDetailsJsonGet200Response]>
     */
    @GET("Catalog/{eventId}/FOC_MECH_FOW/scenario_details.json")
    fun catalogEventIdFOCMECHFOWScenarioDetailsJsonGet(@Path("eventId") eventId: kotlin.String): Call<CatalogEventIdFOCMECHFOWScenarioDetailsJsonGet200Response>


    /**
    * enum for parameter focalPlane
    */
    enum class FocalPlaneCatalogEventIdFOCMECHFOWScenarioIdFocalPlaneGraphicsDisplacementMapJpgGet(val value: kotlin.String) {
        @Json(name = "FP1") fP1("FP1"),
        @Json(name = "FP2") fP2("FP2")
    }

    /**
     * The displacement map of the scenario of the event
     * The displacement map of the scenario of the event. The image is a JPEG file. This file might not be available for all the events. If this file is available, the displacement map description is also available.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param scenarioId The id of the scenario.
     * @param focalPlane The focal plane of the finite source.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/FOC_MECH_FOW/{scenarioId}/{focalPlane}/graphics/DisplacementMap.jpg")
    fun catalogEventIdFOCMECHFOWScenarioIdFocalPlaneGraphicsDisplacementMapJpgGet(@Path("eventId") eventId: kotlin.String, @Path("scenarioId") scenarioId: kotlin.String, @Path("focalPlane") focalPlane: kotlin.String): Call<ResponseBody>


    /**
    * enum for parameter focalPlane
    */
    enum class FocalPlaneCatalogEventIdFOCMECHFOWScenarioIdFocalPlaneGraphicsDisplacementMapLanguageTxtGet(val value: kotlin.String) {
        @Json(name = "FP1") fP1("FP1"),
        @Json(name = "FP2") fP2("FP2")
    }


    /**
    * enum for parameter language
    */
    enum class LanguageCatalogEventIdFOCMECHFOWScenarioIdFocalPlaneGraphicsDisplacementMapLanguageTxtGet(val value: kotlin.String) {
        @Json(name = "it") `it`("it"),
        @Json(name = "en") en("en")
    }

    /**
     * The description of the displacement map of the scenario of the event
     * The description of the displacement map of the scenario of the event. The description is a text file. This file might not be available for all the events. If this file is available, the displacement map image is also available.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param scenarioId The id of the scenario.
     * @param focalPlane The focal plane of the finite source.
     * @param language The language of the description.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/FOC_MECH_FOW/{scenarioId}/{focalPlane}/graphics/DisplacementMap_{language}.txt")
    fun catalogEventIdFOCMECHFOWScenarioIdFocalPlaneGraphicsDisplacementMapLanguageTxtGet(@Path("eventId") eventId: kotlin.String, @Path("scenarioId") scenarioId: kotlin.String, @Path("focalPlane") focalPlane: kotlin.String, @Path("language") language: kotlin.String): Call<ResponseBody>


    /**
    * enum for parameter focalPlane
    */
    enum class FocalPlaneCatalogEventIdFOCMECHFOWScenarioIdFocalPlaneGraphicsPredictedFringesJpgGet(val value: kotlin.String) {
        @Json(name = "FP1") fP1("FP1"),
        @Json(name = "FP2") fP2("FP2")
    }

    /**
     * The predicted fringes of the scenario of the event
     * The predicted fringes of the scenario of the event. The image is a JPEG file. This file might not be available for all the events. If this file is available, the predicted fringes description is also available.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param scenarioId The id of the scenario.
     * @param focalPlane The focal plane of the finite source.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/FOC_MECH_FOW/{scenarioId}/{focalPlane}/graphics/PredictedFringes.jpg")
    fun catalogEventIdFOCMECHFOWScenarioIdFocalPlaneGraphicsPredictedFringesJpgGet(@Path("eventId") eventId: kotlin.String, @Path("scenarioId") scenarioId: kotlin.String, @Path("focalPlane") focalPlane: kotlin.String): Call<ResponseBody>


    /**
    * enum for parameter focalPlane
    */
    enum class FocalPlaneCatalogEventIdFOCMECHFOWScenarioIdFocalPlaneGraphicsPredictedFringesLanguageTxtGet(val value: kotlin.String) {
        @Json(name = "FP1") fP1("FP1"),
        @Json(name = "FP2") fP2("FP2")
    }


    /**
    * enum for parameter language
    */
    enum class LanguageCatalogEventIdFOCMECHFOWScenarioIdFocalPlaneGraphicsPredictedFringesLanguageTxtGet(val value: kotlin.String) {
        @Json(name = "it") `it`("it"),
        @Json(name = "en") en("en")
    }

    /**
     * The description of the predicted fringes of the scenario of the event
     * The description of the predicted fringes of the scenario of the event. The description is a text file. This file might not be available for all the events. If this file is available, the predicted fringes image is also available.
     * Responses:
     *  - 200: Successful operation
     *
     * @param eventId The id of the event.
     * @param scenarioId The id of the scenario.
     * @param focalPlane The focal plane of the finite source.
     * @param language The language of the description.
     * @return [Call]<[ResponseBody]>
     */
    @GET("Catalog/{eventId}/FOC_MECH_FOW/{scenarioId}/{focalPlane}/graphics/PredictedFringes_{language}.txt")
    fun catalogEventIdFOCMECHFOWScenarioIdFocalPlaneGraphicsPredictedFringesLanguageTxtGet(@Path("eventId") eventId: kotlin.String, @Path("scenarioId") scenarioId: kotlin.String, @Path("focalPlane") focalPlane: kotlin.String, @Path("language") language: kotlin.String): Call<ResponseBody>

}
