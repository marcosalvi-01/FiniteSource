package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.squareup.moshi.Json


interface CatalogApi {
    /**
     * The list of the events in the catalog
     * The list of the events in the catalog. The list is a JSON file containing an array with all the id of the events in the catalog.
     * Responses:
     *  - 200: Successful operation
     *
     * @return [Call]<[kotlin.collections.List<kotlin.String>]>
     */
    @GET("Catalog/event_list.json")
    fun catalogEventListJsonGet(): Call<kotlin.collections.List<kotlin.String>>

}
