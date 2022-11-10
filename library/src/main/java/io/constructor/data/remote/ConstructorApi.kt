package io.constructor.data.remote

import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.data.model.browse.BrowseResponse
import io.constructor.data.model.browse.BrowseResultClickRequestBody
import io.constructor.data.model.browse.BrowseResultLoadRequestBody
import io.constructor.data.model.conversion.ConversionRequestBody
import io.constructor.data.model.purchase.PurchaseRequestBody
import io.constructor.data.model.quiz.QuizQuestionResponse
import io.constructor.data.model.quiz.QuizResultsResponse
import io.constructor.data.model.recommendations.RecommendationResultClickRequestBody
import io.constructor.data.model.recommendations.RecommendationResultViewRequestBody
import io.constructor.data.model.recommendations.RecommendationsResponse
import io.constructor.data.model.search.SearchResponse
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

/**
 * @suppress
 */
interface ConstructorApi {

    @GET
    fun getAutocompleteResults(@Url autocompleteUrl: String): Single<Result<ResponseBody>>

    @GET
    suspend fun getAutocompleteResultsCRT(@Url autocompleteUrl: String): AutocompleteResponse

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
                               @Query("variation_id") variationId: String?,
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

    @GET
    fun getSearchResults(@Url searchUrl: String): Single<Result<ResponseBody>>

    @GET
    suspend fun getSearchResultsCRT(@Url autocompleteUrl: String): SearchResponse

    @GET
    fun getBrowseResults(@Url browseUrl: String): Single<Result<ResponseBody>>

    @GET
    suspend fun getBrowseResultsCRT(@Url browseUrl: String): BrowseResponse

    @POST(ApiPaths.URL_BROWSE_RESULT_CLICK_EVENT)
    fun trackBrowseResultClick(@Body browseResultClickRequestBody: BrowseResultClickRequestBody,
                               @QueryMap params: Map<String, String?>,
                               @QueryMap(encoded = true) encodedData: Map<String, String>): Completable

    @POST(ApiPaths.URL_BROWSE_RESULT_LOAD_EVENT)
    fun trackBrowseResultsLoaded(@Body browseRequestBody: BrowseResultLoadRequestBody,
                                 @QueryMap params: Map<String, String>): Completable

    @GET
    fun getRecommendationResults(@Url recommendationUrl: String): Single<Result<ResponseBody>>

    @GET
    suspend fun getRecommendationResultsCRT(@Url recommendationUrl: String): RecommendationsResponse

    @POST(ApiPaths.URL_RECOMMENDATION_RESULT_CLICK_EVENT)
    fun trackRecommendationResultClick(@Body recommendationResultClickRequestBody: RecommendationResultClickRequestBody,
                                       @QueryMap params: Map<String, String?>): Completable

    @POST(ApiPaths.URL_RECOMMENDATION_RESULT_VIEW_EVENT)
    fun trackRecommendationResultsView(@Body recommendationResultViewRequestBody: RecommendationResultViewRequestBody,
                                       @QueryMap params: Map<String, String>): Completable

    @GET
    fun getQuizQuestion(@Url quizUrl: String): Single<Result<ResponseBody>>

    @GET
    suspend fun getQuizQuestionCRT(@Url quizUrl: String): QuizQuestionResponse

    @GET
    fun getQuizResults(@Url quizUrl: String): Single<Result<ResponseBody>>

    @GET
    suspend fun getQuizResultsCRT(@Url quizUrl: String): QuizResultsResponse
}
