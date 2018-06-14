package io.constructor.data.remote

import io.constructor.data.model.Result
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ConstructorApi {

    @GET(ApiPaths.URL_GET_SUGGESTIONS)
    fun getSuggestions(@Path("value") value: String): Single<Response<Result>>

    @GET(ApiPaths.URL_SELECT_EVENT)
    fun triggerSelectEvent(@Path("term") term: String, @QueryMap data: Map<String, String>, @QueryMap(encoded = true) encodedData: Map<String, String>): Observable<Response<String>>

    @GET(ApiPaths.URL_SEARCH_EVENT)
    fun triggerSearchEvent(@Path("term") term: String, @QueryMap data: Map<String, String>, @QueryMap(encoded = true) encodedData: Map<String, String>): Observable<Response<String>>

    @GET(ApiPaths.URL_SESSION_START_EVENT)
    fun triggerSessionStartEvent(@QueryMap params: Map<String, String>): Observable<Response<String>>

    @GET(ApiPaths.URL_CONVERT_EVENT)
    fun triggerConvertEvent(@Query("item_id") itemId: String, @Query("revenue") revenue: String?, @QueryMap params: Map<String, String>): Observable<Response<String>>

    @GET(ApiPaths.URL_CLICK_THROUGH_EVENT)
    fun triggerSearchResultClickThroughEvent(@Path("term") term: String, @Query("item_id") itemId: String, @Query("position") position: String?, @QueryMap params: Map<String, String>): Observable<Response<String>>

    @GET(ApiPaths.URL_BEHAVIOR)
    fun triggerSearchResultLoadedEvent(@Query("term") term: String, @Query("num_results") resultCount: Int, @QueryMap params: Map<String, String>): Observable<Response<String>>

    @GET(ApiPaths.URL_BEHAVIOR)
    fun inputFocusEvent(@Query("term") term: String?, @QueryMap params: Map<String, String>): Observable<Response<String>>
}