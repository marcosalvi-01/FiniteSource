package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.squareup.moshi.Json

import org.openapitools.client.models.FiniteSourceAppAppJsonGet200ResponseInner

interface FiniteSourceAndroidAppApi {
    /**
     * A JSON file that contains all the data necessary to the initial visualization of the events on the map
     * A JSON file that contains all the data necessary to the initial visualization of the events on the map. The JSON file contains an array of objects. Each object contains the data of an event. The data in this file is the same as the data in the catalog but put together to avoid multiple requests to the server.
     * Responses:
     *  - 200: Successful operation
     *
     * @return [Call]<[kotlin.collections.List<FiniteSourceAppAppJsonGet200ResponseInner>]>
     */
    @GET("Finite_source_app/app.json")
    fun finiteSourceAppAppJsonGet(): Call<kotlin.collections.List<FiniteSourceAppAppJsonGet200ResponseInner>>

}
