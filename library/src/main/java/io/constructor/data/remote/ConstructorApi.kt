package io.constructor.data.remote

import io.constructor.data.model.Result
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ConstructorApi {

    /**
     * gets suggestions based on search term
     */
    @GET(ApiPaths.URL_GET_SUGGESTIONS)
    fun getSuggestions(@Path("value") value: String): Single<Response<Result>>

    /**
     * event that triggers on user selection
     */
    @GET(ApiPaths.URL_SELECT_EVENT)
    fun triggerSelectEvent(@Path("term") term: String, @QueryMap data: Map<String, String>, @QueryMap(encoded = true) encodedData: Map<String, String>): Observable<Response<String>>

    /**
     * event that triggers on user search
     */
    @GET(ApiPaths.URL_SEARCH_EVENT)
    fun triggerSearchEvent(@Path("term") term: String, @QueryMap data: Map<String, String>, @QueryMap(encoded = true) encodedData: Map<String, String>): Observable<Response<String>>
}