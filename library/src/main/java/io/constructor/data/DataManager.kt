package io.constructor.data

import io.constructor.data.model.Suggestion
import io.constructor.data.remote.ConstructorApi
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataManager @Inject
constructor(private val constructorApi: ConstructorApi) {

    fun getAutocompleteResults(text: String, params: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<List<Suggestion>?>> = constructorApi.getSuggestions(text, params.toMap()).map {
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
        return constructorApi.trackSearchResultTerm(term, itemName, customerId, params.toMap())
    }

    fun trackSearchResultsLoaded(term: String, resultCount: Int, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackSearchResultsLoaded(term, resultCount, params.toMap())
    }

    fun trackInputFocus(term: String?, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackInputFocus(term, params.toMap())
    }

    fun trackPurchase(params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackPurchase(params.toMap())
    }

}