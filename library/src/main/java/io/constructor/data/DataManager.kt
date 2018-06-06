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

    fun triggerSelectEvent(term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<Response<String>> {
        return constructorApi.triggerSelectEvent(term, params.toMap(), encodedParams.toMap())
    }

    fun triggerSearchEvent(term: String, params: Array<Pair<String, String>> = arrayOf(), encodedParams: Array<Pair<String, String>> = arrayOf()): Observable<Response<String>> {
        return constructorApi.triggerSearchEvent(term, params.toMap(), encodedParams.toMap())
    }

    fun triggerSessionStartEvent(params: Array<Pair<String, String>>): Observable<Response<String>> {
        return constructorApi.triggerSessionStartEvent(params.toMap())
    }

}