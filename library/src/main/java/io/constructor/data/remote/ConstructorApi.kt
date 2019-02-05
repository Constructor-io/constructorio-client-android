package io.constructor.data.remote

import io.constructor.data.model.AutocompleteResult
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ConstructorApi {

    @GET(ApiPaths.URL_GET_SUGGESTIONS)
    fun getSuggestions(@Path("value") value: String, @QueryMap data: Map<String, String>): Single<Result<AutocompleteResult>>

    @GET(ApiPaths.URL_SELECT_EVENT)
    fun trackAutocompleteSelect(@Path("term") term: String, @QueryMap data: Map<String, String>, @QueryMap(encoded = true) encodedData: Map<String, String>): Completable

    @GET(ApiPaths.URL_SEARCH_EVENT)
    fun trackSearchSubmit(@Path("term") term: String, @QueryMap data: Map<String, String>, @QueryMap(encoded = true) encodedData: Map<String, String>): Completable

    @GET(ApiPaths.URL_SESSION_START_EVENT)
    fun trackSessionStart(@QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_CONVERT_EVENT)
    fun trackConversion(@Path("term") term: String, @Query("name") itemName: String, @Query("customer_id") customerId: String, @Query("revenue") revenue: String?, @QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_CLICK_THROUGH_EVENT)
    fun trackSearchResultTerm(@Path("term") term: String, @Query("name") itemName: String, @Query("customer_id") customerId: String, @QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_BEHAVIOR)
    fun trackSearchResultLoaded(@Query("term") term: String, @Query("num_results") resultCount: Int, @QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_BEHAVIOR)
    fun trackInputFocus(@Query("term") term: String?, @QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_PURCHASE)
    fun trackPurchase(@QueryMap params: Map<String, String>): Completable
}