package io.constructor.data

import com.squareup.moshi.Moshi
import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.data.model.search.SearchResponse
import io.constructor.data.model.browse.BrowseResponse
import io.constructor.data.model.browse.BrowseResultClickRequestBody
import io.constructor.data.model.browse.BrowseResultLoadRequestBody
import io.constructor.data.model.conversion.ConversionRequestBody
import io.constructor.data.model.purchase.PurchaseRequestBody
import io.constructor.data.remote.ApiPaths
import io.constructor.data.remote.ConstructorApi
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataManager @Inject
constructor(private val constructorApi: ConstructorApi, private val moshi: Moshi) {

    fun getAutocompleteResults(term: String, params: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<AutocompleteResponse>> {
        return constructorApi.getAutocompleteResults(term, params.toMap()).map {
            if (!it.isError) {
                it.response()?.let {
                    if (it.isSuccessful) {
                        val adapter = moshi.adapter(AutocompleteResponse::class.java)
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

    fun getSearchResults(term: String, encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<SearchResponse>> {
        var dynamicUrl = "/${ApiPaths.URL_SEARCH.format(term)}"
        encodedParams.forEachIndexed { index, pair ->
            dynamicUrl += "${if (index != 0) "&" else "?" }${pair.first}=${pair.second}"
        }
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

    fun trackSearchResultClick(itemName: String, customerId: String, term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String,  String>> = arrayOf()): Completable {
        return constructorApi.trackSearchResultClick(term, itemName, customerId, params.toMap(), encodedParams.toMap())
    }

    fun trackSearchResultsLoaded(term: String, resultCount: Int, customerIds: Array<String>? = null, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackSearchResultsLoaded(term, resultCount, customerIds?.take(60)?.joinToString(","), params.toMap())
    }

    fun trackInputFocus(term: String?, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackInputFocus(term, params.toMap())
    }

    fun trackPurchase(purchaseRequestBody: PurchaseRequestBody, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackPurchase(purchaseRequestBody, params.toMap())
    }

    fun getBrowseResults(filterName: String, filterValue: String, encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<ConstructorData<BrowseResponse>> {
        var dynamicUrl = "/${ApiPaths.URL_BROWSE.format(filterName, filterValue)}"
        encodedParams.forEachIndexed { index, pair ->
            dynamicUrl += "${if (index != 0) "&" else "?" }${pair.first}=${pair.second}"
        }
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

    fun trackBrowseResultsLoaded(browseResultLoadRequestBody: BrowseResultLoadRequestBody, params: Array<Pair<String, String>>): Completable {
        return constructorApi.trackBrowseResultsLoaded(browseResultLoadRequestBody, params.toMap())
    }

    fun trackBrowseResultClick(browseResultClickRequestBody: BrowseResultClickRequestBody, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String,  String>> = arrayOf()): Completable {
        return constructorApi.trackBrowseResultClick(browseResultClickRequestBody, params.toMap(), encodedParams.toMap())
    }

}