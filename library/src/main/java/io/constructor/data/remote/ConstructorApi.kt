package io.constructor.data.remote

import io.constructor.data.model.AutocompleteResponse
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ConstructorApi {

    @GET(ApiPaths.URL_GET_SUGGESTIONS)
    fun getSuggestions(@Path("value") value: String): Single<Response<AutocompleteResponse>>

    @GET(ApiPaths.URL_SELECT_EVENT)
    fun trackSelect(@Path("term") term: String, @QueryMap data: Map<String, String>, @QueryMap(encoded = true) encodedData: Map<String, String>): Completable

    @GET
    fun search(@Url searchUrl: String): Observable<Response<ResponseBody>>

    @GET(ApiPaths.URL_SEARCH_EVENT)
    fun trackSearch(@Path("term") term: String, @QueryMap data: Map<String, String>, @QueryMap(encoded = true) encodedData: Map<String, String>): Completable

    @GET(ApiPaths.URL_SESSION_START_EVENT)
    fun trackSessionStart(@QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_CONVERT_EVENT)
    fun trackConversion(@Path("term") term: String, @Query("item_id") itemId: String, @Query("revenue") revenue: String?, @QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_CLICK_THROUGH_EVENT)
    fun trackSearchResultClickThrough(@Path("term") term: String, @Query("item_id") itemId: String, @Query("position") position: String?, @QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_BEHAVIOR)
    fun trackSearchResultLoaded(@Query("term") term: String, @Query("num_results") resultCount: Int, @QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_BEHAVIOR)
    fun trackInputFocus(@Query("term") term: String?, @QueryMap params: Map<String, String>): Completable
}