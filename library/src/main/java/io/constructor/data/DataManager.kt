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

    fun getAutocompleteResults(text: String): Observable<ConstructorData<List<Suggestion>?>> = constructorApi.getSuggestions(text).map {
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

    fun search(text: String, encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<SearchResponse> {
        var dynamicUrl = BuildConfig.BASE_API_URL + "/${ApiPaths.URL_SEARCH.format(text)}"
        encodedParams.forEachIndexed { index, pair ->
            dynamicUrl += "${if (index != 0) "&" else "?" }${pair.first}=${pair.second}"
        }
        return constructorApi.search(dynamicUrl).map {
            if (it.isSuccessful) {
                val adapter = moshi.adapter(SearchResponse::class.java)
                val response = it.body()?.string()
                val result = response?.let { adapter.fromJson(it) }
                result?.rawData = response
                result
            } else {
                throw Exception()
            }
        }
    }

    fun trackSelect(term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackSelect(term, params.toMap(), encodedParams.toMap())
    }

    fun trackSearch(term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackSearch(term, params.toMap(), encodedParams.toMap())
    }

    fun trackSessionStart(params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackSessionStart(params.toMap())
    }

    fun trackConversion(term: String, itemId: String, revenue: String? = null, params: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackConversion(term, itemId, revenue, params.toMap())
    }

    fun trackSearchResultClickThrough(term: String, itemId: String, position: String? = null, params: Array<Pair<String, String>> = arrayOf()): Completable {
        return constructorApi.trackSearchResultClickThrough(term, itemId, position, params.toMap())
    }

    fun trackSearchResultLoaded(term: String, reultCount: Int, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackSearchResultLoaded(term, reultCount, params.toMap())
    }

    fun trackInputFocus(term: String?, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackInputFocus(term, params.toMap())
    }

}