package io.constructor.data

import com.squareup.moshi.Moshi
import io.constructor.BuildConfig
import io.constructor.data.model.Suggestion
import io.constructor.data.model.search.SearchResponse
import io.constructor.data.remote.ApiPaths
import io.constructor.data.remote.ConstructorApi
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataManager @Inject
constructor(private val constructorApi: ConstructorApi, private val moshi: Moshi) {

    fun getAutocompleteResults(text: String, params: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<List<Suggestion>?>> {
        return constructorApi.getAutocompleteResults(text, params.toMap()).map {
            if (!it.isError) {
                it.response()?.let {
                    if (it.isSuccessful) {
                        ConstructorData.of(it.body()?.sections?.suggestions)
                    } else {
                        ConstructorData.networkError(it.errorBody()?.string())
                    }
                } ?: ConstructorData.error(it.error())
            } else {
                ConstructorData.error(it.error())
            }
        }.toObservable()
    }

    fun getSearchResults(text: String, encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<SearchResponse>> {
        var dynamicUrl = BuildConfig.BASE_API_URL + "/${ApiPaths.URL_SEARCH.format(text)}"
        encodedParams.forEachIndexed { index, pair ->
            dynamicUrl += "${if (index != 0) "&" else "?" }${pair.first}=${pair.second}"
        }
        return constructorApi.getSearchResults(dynamicUrl).map { result ->
            if (!result.isError) {
                result.response()?.let {
                    if (it.isSuccessful){
                        val adapter = moshi.adapter(SearchResponse::class.java)
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

    fun trackAutocompleteSelect(term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackAutocompleteSelect(term, params.toMap(), encodedParams.toMap())
    }

    fun trackSearchSubmit(term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackSearchSubmit(term, params.toMap(), encodedParams.toMap())
    }

    fun trackSessionStart(params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackSessionStart(params.toMap())
    }

    fun trackConversion(term: String, itemName: String, customerId: String, revenue: String? = null, params: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackConversion(term, itemName, customerId, revenue, params.toMap())
    }

    fun trackSearchResultClick(itemName: String, customerId: String, term: String, params: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackSearchResultClick(term, itemName, customerId, params.toMap())
    }

    fun trackSearchResultsLoaded(term: String, resultCount: Int, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackSearchResultsLoaded(term, resultCount, params.toMap())
    }

    fun trackInputFocus(term: String?, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackInputFocus(term, params.toMap())
    }

    fun trackPurchase(customerIds: List<String>, revenue: String? = null, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackPurchase(customerIds, revenue, params.toMap())
    }

}