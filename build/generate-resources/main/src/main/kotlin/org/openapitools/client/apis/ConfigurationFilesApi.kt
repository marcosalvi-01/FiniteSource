package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.squareup.moshi.Json

import org.openapitools.client.models.ConfigFocalMechanismProvidersJsonGet200ResponseInner
import org.openapitools.client.models.ConfigGeneralConfigJsonGet200Response

interface ConfigurationFilesApi {
    /**
     * The list of the possible providers for the scenarios of an event
     * The list of the possible providers for the scenarios of an event. The providers are identified by their id.
     * Responses:
     *  - 200: Successful operation
     *
     * @return [Call]<[kotlin.collections.List<ConfigFocalMechanismProvidersJsonGet200ResponseInner>]>
     */
    @GET("config/FocalMechanismProviders.json")
    fun configFocalMechanismProvidersJsonGet(): Call<kotlin.collections.List<ConfigFocalMechanismProvidersJsonGet200ResponseInner>>

    /**
     * A JSON file that contains some general data about the whole API
     * The configuration contains the list of the available languages, the URL of the INGV server to append the INGV event id, the name of the GeoJson file containing the finite source data to plot on a map, the version of the color palette used to represent the slip in the finite source data and the list of the id of the possible available products for the events in the catalog. 
     * Responses:
     *  - 200: Successful operation
     *
     * @return [Call]<[ConfigGeneralConfigJsonGet200Response]>
     */
    @GET("config/GeneralConfig.json")
    fun configGeneralConfigJsonGet(): Call<ConfigGeneralConfigJsonGet200Response>

    /**
     * The color palette used to represent the slip in the finite source data
     * The color palette used to represent the slip in the finite source data. The color palette is an array of hexadecimal colors including the alpha value. The first color is used for the slip value 0, the last color is used for the slip value 1. The colors in between are used for the intermediate values.
     * Responses:
     *  - 200: Successful operation
     *
     * @return [Call]<[kotlin.collections.List<kotlin.String>]>
     */
    @GET("config/slip_color_palette.json")
    fun configSlipColorPaletteJsonGet(): Call<kotlin.collections.List<kotlin.String>>

}
