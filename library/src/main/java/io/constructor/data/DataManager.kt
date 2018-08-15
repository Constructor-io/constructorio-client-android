package io.constructor.data

import io.constructor.data.model.Suggestion
import io.constructor.data.remote.ConstructorApi
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataManager @Inject
constructor(private val constructorApi: ConstructorApi) {

    fun getAutocompleteResults(text: String): Single<MutableList<Suggestion>> = constructorApi.getSuggestions(text).toObservable().map {
        if (it.isSuccessful) {
            it.body()
        } else {
            throw HttpException(it)
        }
    }.flatMapIterable { result -> result.sections.suggestions }.toList()

    fun trackSelect(term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<Response<String>> {
        return constructorApi.trackSelect(term, params.toMap(), encodedParams.toMap())
    }

    fun trackSearch(term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<Response<String>> {
        return constructorApi.trackSearch(term, params.toMap(), encodedParams.toMap())
    }

    fun trackSessionStart(params: Array<Pair<String, String>>): Observable<Response<String>> {
        return constructorApi.trackSessionStart(params.toMap())
    }

    fun trackConversion(term: String, itemId: String, revenue: String? = null, params: Array<Pair<String, String>> = arrayOf()): Observable<Response<String>> {
        return constructorApi.trackConversion(term, itemId, revenue, params.toMap())
    }

    fun trackSearchResultClickThrough(term: String, itemId: String, position: String? = null, params: Array<Pair<String, String>> = arrayOf()): Observable<Response<String>> {
        return constructorApi.trackSearchResultClickThrough(term, itemId, position, params.toMap())
    }

    fun trackSearchResultLoaded(term: String, reultCount: Int, params: Array<Pair<String, String>>): Observable<Response<String>> {
        return constructorApi.trackSearchResultLoaded(term, reultCount, params.toMap())
    }

    fun trackInputFocus(term: String?, params: Array<Pair<String, String>>): Observable<Response<String>> {
        return constructorApi.trackInputFocus(term, params.toMap())
    }

}