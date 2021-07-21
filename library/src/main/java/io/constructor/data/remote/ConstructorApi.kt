package io.constructor.data.remote

import io.constructor.data.model.browse.BrowseResultClickRequestBody
import io.constructor.data.model.browse.BrowseResultLoadRequestBody
import io.constructor.data.model.purchase.PurchaseRequestBody
import io.constructor.data.model.conversion.ConversionRequestBody
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface ConstructorApi {

    @GET(ApiPaths.URL_AUTOCOMPLETE)
    fun getAutocompleteResults(@Path("term") term: String,
                               @QueryMap(encoded = true) encodedData: Map<String, String>): Single<Result<ResponseBody>>

    @GET(ApiPaths.URL_AUTOCOMPLETE_SELECT_EVENT)
    fun trackAutocompleteSelect(@Path("term") term: String,
                                @QueryMap data: Map<String, String>,
                                @QueryMap(encoded = true) encodedData: Map<String, String>): Completable

    @GET(ApiPaths.URL_SEARCH_SUBMIT_EVENT)
    fun trackSearchSubmit(@Path("term") term: String,
                          @QueryMap data: Map<String, String>,
                          @QueryMap(encoded = true) encodedData: Map<String, String>): Completable

    @GET(ApiPaths.URL_SESSION_START_EVENT)
    fun trackSessionStart(@QueryMap params: Map<String, String>): Completable

    @POST(ApiPaths.URL_CONVERSION_EVENT)
    fun trackConversion(@Body conversionRequestBody: ConversionRequestBody,
                        @QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_SEARCH_RESULT_CLICK_EVENT)
    fun trackSearchResultClick(@Path("term") term: String,
                               @Query("name") itemName: String,
                               @Query("customer_id") customerId: String,
                               @QueryMap params: Map<String, String>,
                               @QueryMap(encoded = true) encodedData: Map<String, String>): Completable

    @GET(ApiPaths.URL_BEHAVIOR)
    fun trackSearchResultsLoaded(@Query("term") term: String,
                                 @Query("num_results") resultCount: Int,
                                 @Query("customer_ids") customerIds: String?,
                                 @QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_BEHAVIOR)
    fun trackInputFocus(@Query("term") term: String?, @QueryMap params: Map<String, String>): Completable

    @POST(ApiPaths.URL_PURCHASE)
    fun trackPurchase(@Body purchaseRequestBody: PurchaseRequestBody,
                      @QueryMap params: Map<String, String>): Completable

    @GET(ApiPaths.URL_SEARCH)
    fun getSearchResults(@Path("term") term: String,
                         @QueryMap(encoded = true) encodedData: Map<String, String>?): Single<Result<ResponseBody>>

    @GET(ApiPaths.URL_BROWSE)
    fun getBrowseResults(@Path("filter_name") filterName: String,
                         @Path("filter_value") filterValue: String,
                         @QueryMap(encoded = true) encodedData: Map<String, String>): Single<Result<ResponseBody>>

    @POST(ApiPaths.URL_BROWSE_RESULT_CLICK_EVENT)
    fun trackBrowseResultClick(@Body browseResultClickRequestBody: BrowseResultClickRequestBody,
                               @QueryMap params: Map<String, String?>,
                               @QueryMap(encoded = true) encodedData: Map<String, String>): Completable

    @POST(ApiPaths.URL_BROWSE_RESULT_LOAD_EVENT)
    fun trackBrowseResultsLoaded(@Body browseRequestBody: BrowseResultLoadRequestBody,
                                 @QueryMap params: Map<String, String>): Completable

}