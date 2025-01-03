package io.constructor.data

import com.squareup.moshi.Moshi
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.data.model.browse.*
import io.constructor.data.model.tracking.ItemDetailLoadRequestBody
import io.constructor.data.model.conversion.ConversionRequestBody
import io.constructor.data.model.purchase.PurchaseRequestBody
import io.constructor.data.model.quiz.*
import io.constructor.data.model.recommendations.RecommendationResultClickRequestBody
import io.constructor.data.model.recommendations.RecommendationResultViewRequestBody
import io.constructor.data.model.recommendations.RecommendationsResponse
import io.constructor.data.model.search.*
import io.constructor.data.model.tracking.GenericResultClickRequestBody
import io.constructor.data.remote.ApiPaths
import io.constructor.data.remote.ConstructorApi
import io.constructor.injection.ConstructorSdk
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @suppress
 */
@Singleton
class DataManager @Inject
constructor(private val constructorApi: ConstructorApi, @ConstructorSdk private val moshi: Moshi) {

    private fun getAdditionalParamsQueryString(encodedParams: Array<Pair<String, String>>): String {
        var queryString = ""
        encodedParams.forEachIndexed { index, pair ->
            queryString += "${if (index != 0) "&" else "?" }${pair.first}=${pair.second}"
        }
        return queryString
    }

    fun getAutocompleteResults(term: String, encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<AutocompleteResponse>> {
        var dynamicUrl = "/${ApiPaths.URL_AUTOCOMPLETE.format(term)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getAutocompleteResults(dynamicUrl).map { result ->
            if (!result.isError) {
                result.response()?.let {
                    if (it.isSuccessful) {
                        val adapter = moshi.adapter(AutocompleteResponse::class.java)
                        val response = it.body()?.string()
                        val result = response?.let { adapter.fromJson(it) }
                        result?.rawData = response
                        ConstructorData.of(result!!)
                    } else {
                        ConstructorData.networkError(it.errorBody()?.string())
                    }
                } ?: ConstructorData.error(result.error())
            } else {
                ConstructorData.error(result.error())
            }
        }.toObservable()
    }

    suspend fun getAutocompleteResultsCRT(term: String, encodedParams: Array<Pair<String, String>> = arrayOf()): AutocompleteResponse {
        var dynamicUrl = "/${ApiPaths.URL_AUTOCOMPLETE.format(term)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getAutocompleteResultsCRT(dynamicUrl)
    }

    fun getSearchResults(term: String, encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<SearchResponse>> {
        var dynamicUrl = "/${ApiPaths.URL_SEARCH.format(term)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getSearchResults(dynamicUrl).map { result ->
            if (!result.isError) {
                result.response()?.let {
                    if (it.isSuccessful){
                        val adapter = moshi.adapter(SearchResponse::class.java)
                        val response = it.body()?.string()
                        val res = response?.let { adapter.fromJson(it) }
                        res?.rawData = response
                        ConstructorData.of(res!!)
                    } else {
                        ConstructorData.networkError(it.errorBody()?.string())
                    }
                } ?: ConstructorData.error(result.error())
            } else {
                ConstructorData.error(result.error())
            }
        }.toObservable()
    }

    suspend fun getSearchResultsCRT(term: String, encodedParams: Array<Pair<String, String>> = arrayOf()): SearchResponse {
        var dynamicUrl = "/${ApiPaths.URL_SEARCH.format(term)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getSearchResultsCRT(dynamicUrl)
    }

    fun trackAutocompleteSelect(term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String,  String>> = arrayOf()): Completable {
        return constructorApi.trackAutocompleteSelect(term, params.toMap(), encodedParams.toMap())
    }

    fun trackSearchSubmit(term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackSearchSubmit(term, params.toMap(), encodedParams.toMap())
    }

    fun trackSessionStart(params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackSessionStart(params.toMap())
    }

    fun trackConversion(conversionRequestBody: ConversionRequestBody, params: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackConversion(conversionRequestBody, params.toMap())
    }

    fun trackSearchResultClick(itemName: String, customerId: String, variationId: String?, term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String,  String>> = arrayOf()): Completable {
        return constructorApi.trackSearchResultClick(term, itemName, customerId, variationId, params.toMap(), encodedParams.toMap())
    }

    fun trackSearchResultsLoaded(searchResultLoadRequestBody: SearchResultLoadRequestBody, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackSearchResultsLoaded(searchResultLoadRequestBody, params.toMap())
    }

    fun trackInputFocus(term: String?, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackInputFocus(term, params.toMap())
    }

    fun trackPurchase(purchaseRequestBody: PurchaseRequestBody, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackPurchase(purchaseRequestBody, params.toMap())
    }

    fun getBrowseResults(filterName: String, filterValue: String, encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<BrowseResponse>> {
        var dynamicUrl = "/${ApiPaths.URL_BROWSE.format(filterName, filterValue)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getBrowseResults(dynamicUrl).map { result ->
            if (!result.isError) {
                result.response()?.let {
                    if (it.isSuccessful){
                        val adapter = moshi.adapter(BrowseResponse::class.java)
                        val response = it.body()?.string()
                        val res = response?.let { adapter.fromJson(it) }
                        res?.rawData = response
                        ConstructorData.of(res!!)
                    } else {
                        ConstructorData.networkError(it.errorBody()?.string())
                    }
                } ?: ConstructorData.error(result.error())
            } else {
                ConstructorData.error(result.error())
            }
        }.toObservable()
    }

    suspend fun getBrowseResultsCRT(filterName: String, filterValue: String, encodedParams: Array<Pair<String, String>> = arrayOf()): BrowseResponse {
        var dynamicUrl = "/${ApiPaths.URL_BROWSE.format(filterName, filterValue)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getBrowseResultsCRT(dynamicUrl)
    }

    fun getBrowseItemsResults(encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<BrowseResponse>> {
        var dynamicUrl = "/${ApiPaths.URL_BROWSE_ITEMS}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getBrowseResults(dynamicUrl).map { result ->
            if (!result.isError) {
                result.response()?.let {
                    if (it.isSuccessful){
                        val adapter = moshi.adapter(BrowseResponse::class.java)
                        val response = it.body()?.string()
                        val res = response?.let { adapter.fromJson(it) }
                        res?.rawData = response
                        ConstructorData.of(res!!)
                    } else {
                        ConstructorData.networkError(it.errorBody()?.string())
                    }
                } ?: ConstructorData.error(result.error())
            } else {
                ConstructorData.error(result.error())
            }
        }.toObservable()
    }

    suspend fun getBrowseItemsResultsCRT(encodedParams: Array<Pair<String, String>> = arrayOf()): BrowseResponse {
        var dynamicUrl = "/${ApiPaths.URL_BROWSE_ITEMS}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getBrowseResultsCRT(dynamicUrl)
    }
    
    fun getBrowseFacets(encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<BrowseFacetsResponse>> {
        var dynamicUrl = "/${ApiPaths.URL_BROWSE_FACETS}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getBrowseFacetsResults(dynamicUrl).map { result ->
            if (!result.isError) {
                result.response()?.let {
                    if (it.isSuccessful){
                        val adapter = moshi.adapter(BrowseFacetsResponse::class.java)
                        val response = it.body()?.string()
                        val res = response?.let { adapter.fromJson(it) }
                        res?.rawData = response
                        ConstructorData.of(res!!)
                    } else {
                        ConstructorData.networkError(it.errorBody()?.string())
                    }
                } ?: ConstructorData.error(result.error())
            } else {
                ConstructorData.error(result.error())
            }
        }.toObservable()
    }

    suspend fun getBrowseFacetsCRT(encodedParams: Array<Pair<String, String>> = arrayOf()): BrowseFacetsResponse {
        var dynamicUrl = "/${ApiPaths.URL_BROWSE_FACETS}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getBrowseFacetsResultsCRT(dynamicUrl)
    }

    fun getBrowseFacetOptions(encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<BrowseFacetOptionsResponse>> {
        var dynamicUrl = "/${ApiPaths.URL_BROWSE_FACET_OPTIONS}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getBrowseFacetOptionsResults(dynamicUrl).map { result ->
            if (!result.isError) {
                result.response()?.let {
                    if (it.isSuccessful){
                        val adapter = moshi.adapter(BrowseFacetOptionsResponse::class.java)
                        val response = it.body()?.string()
                        val res = response?.let { adapter.fromJson(it) }
                        res?.rawData = response
                        ConstructorData.of(res!!)
                    } else {
                        ConstructorData.networkError(it.errorBody()?.string())
                    }
                } ?: ConstructorData.error(result.error())
            } else {
                ConstructorData.error(result.error())
            }
        }.toObservable()
    }

    suspend fun getBrowseFacetOptionsCRT(encodedParams: Array<Pair<String, String>> = arrayOf()): BrowseFacetOptionsResponse {
        var dynamicUrl = "/${ApiPaths.URL_BROWSE_FACET_OPTIONS}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getBrowseFacetOptionsResultsCRT(dynamicUrl)
    }

    fun getBrowseGroups(encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<BrowseGroupsResponse>> {
        var dynamicUrl = "/${ApiPaths.URL_BROWSE_GROUPS}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getBrowseGroupsResults(dynamicUrl).map { result ->
            if (!result.isError) {
                result.response()?.let {
                    if (it.isSuccessful){
                        val adapter = moshi.adapter(BrowseGroupsResponse::class.java)
                        val response = it.body()?.string()
                        val res = response?.let { adapter.fromJson(it) }
                        res?.rawData = response
                        ConstructorData.of(res!!)
                    } else {
                        ConstructorData.networkError(it.errorBody()?.string())
                    }
                } ?: ConstructorData.error(result.error())
            } else {
                ConstructorData.error(result.error())
            }
        }.toObservable()
    }

    suspend fun getBrowseGroupsCRT(encodedParams: Array<Pair<String, String>> = arrayOf()): BrowseGroupsResponse {
        var dynamicUrl = "/${ApiPaths.URL_BROWSE_GROUPS}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getBrowseGroupsResultsCRT(dynamicUrl)
    }

    fun trackBrowseResultsLoaded(browseResultLoadRequestBody: BrowseResultLoadRequestBody, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackBrowseResultsLoaded(browseResultLoadRequestBody, params.toMap())
    }

    fun trackBrowseResultClick(browseResultClickRequestBody: BrowseResultClickRequestBody, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String,  String>> = arrayOf()): Completable {
        return constructorApi.trackBrowseResultClick(browseResultClickRequestBody, params.toMap(), encodedParams.toMap())
    }

    fun trackGenericResultClick(genericResultClickRequestBody: GenericResultClickRequestBody, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackGenericResultClick(genericResultClickRequestBody, params.toMap())
    }

    fun trackItemDetailLoaded(itemDetailLoadRequestBody: ItemDetailLoadRequestBody, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackItemDetailLoaded(itemDetailLoadRequestBody, params.toMap())
    }

    fun getRecommendationResults(podId: String, encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<RecommendationsResponse>> {
        var dynamicUrl = "/${ApiPaths.URL_RECOMMENDATIONS.format(podId)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getRecommendationResults(dynamicUrl).map {
            if (!it.isError) {
                it.response()?.let {
                    if (it.isSuccessful) {
                        val adapter = moshi.adapter(RecommendationsResponse::class.java)
                        val response = it.body()?.string()
                        val result = response?.let { adapter.fromJson(it) }
                        result?.rawData = response
                        ConstructorData.of(result!!)
                    } else {
                        ConstructorData.networkError(it.errorBody()?.string())
                    }
                } ?: ConstructorData.error(it.error())
            } else {
                ConstructorData.error(it.error())
            }
        }.toObservable()
    }

    suspend fun getRecommendationResultsCRT(podId: String, encodedParams: Array<Pair<String, String>> = arrayOf()): RecommendationsResponse {
        var dynamicUrl = "/${ApiPaths.URL_RECOMMENDATIONS.format(podId)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getRecommendationResultsCRT(dynamicUrl)
    }

    fun trackRecommendationResultClick(recommendationResultClickRequestBody: RecommendationResultClickRequestBody, params: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackRecommendationResultClick(recommendationResultClickRequestBody, params.toMap())
    }

    fun trackRecommendationResultsView(recommendationResultViewRequestBody: RecommendationResultViewRequestBody, params: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackRecommendationResultsView(recommendationResultViewRequestBody, params.toMap())
    }

    fun trackQuizResultClick(quizResultClickRequestBody: QuizResultClickRequestBody, params: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackQuizResultClick(quizResultClickRequestBody, params.toMap())
    }

    fun trackQuizResultLoad(quizResultLoadRequestBody: QuizResultLoadRequestBody, params: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackQuizResultLoad(quizResultLoadRequestBody, params.toMap())
    }

    fun trackQuizConversion(quizConversionRequestBody: QuizConversionRequestBody, params: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackQuizConversion(quizConversionRequestBody, params.toMap())
    }

    fun getQuizNextQuestion(quizId: String, encodedParams: Array<Pair<String, String>> = arrayOf(), preferencesHelper: PreferencesHelper): Observable<ConstructorData<QuizQuestionResponse>> {
        val url = "${preferencesHelper.scheme}://${preferencesHelper.quizzesServiceUrl}/${ApiPaths.URL_QUIZ_NEXT_QUESTION.format(quizId)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getQuizNextQuestion(url).map {
            if (!it.isError) {
                it.response()?.let {
                    if (it.isSuccessful) {
                        val adapter = moshi.adapter(QuizQuestionResponse::class.java)
                        val response = it.body()?.string()
                        val result = response?.let { adapter.fromJson(it) }
                        result?.rawData = response
                        ConstructorData.of(result!!)
                    } else {
                        ConstructorData.networkError(it.errorBody()?.string())
                    }
                } ?: ConstructorData.error(it.error())
            } else {
                ConstructorData.error(it.error())
            }
        }.toObservable()
    }

    suspend fun getQuizNextQuestionCRT(quizId: String, encodedParams: Array<Pair<String, String>> = arrayOf(), preferencesHelper: PreferencesHelper): QuizQuestionResponse {
        val url = "${preferencesHelper.scheme}://${preferencesHelper.quizzesServiceUrl}/${ApiPaths.URL_QUIZ_NEXT_QUESTION.format(quizId)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getQuizNextQuestionCRT(url)
    }

    fun getQuizResults(quizId: String, encodedParams: Array<Pair<String, String>> = arrayOf(), preferencesHelper: PreferencesHelper): Observable<ConstructorData<QuizResultsResponse>> {
        var url = "${preferencesHelper.scheme}://${preferencesHelper.quizzesServiceUrl}/${ApiPaths.URL_QUIZ_RESULTS.format(quizId)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getQuizResults(url).map {
            if (!it.isError) {
                it.response()?.let {
                    if (it.isSuccessful) {
                        val adapter = moshi.adapter(QuizResultsResponse::class.java)
                        val response = it.body()?.string()
                        val result = response?.let { adapter.fromJson(it) }
                        result?.rawData = response
                        ConstructorData.of(result!!)
                    } else {
                        ConstructorData.networkError(it.errorBody()?.string())
                    }
                } ?: ConstructorData.error(it.error())
            } else {
                ConstructorData.error(it.error())
            }
        }.toObservable()
    }

    suspend fun getQuizResultsCRT(quizId: String, encodedParams: Array<Pair<String, String>> = arrayOf(), preferencesHelper: PreferencesHelper): QuizResultsResponse {
        var url = "${preferencesHelper.scheme}://${preferencesHelper.quizzesServiceUrl}/${ApiPaths.URL_QUIZ_RESULTS.format(quizId)}${getAdditionalParamsQueryString(encodedParams)}"
        return constructorApi.getQuizResultsCRT(url)
    }
}
