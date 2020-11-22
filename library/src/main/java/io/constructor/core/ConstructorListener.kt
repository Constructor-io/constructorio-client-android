package io.constructor.core

import io.constructor.data.model.common.ResultGroup
import io.constructor.data.model.common.Result

interface ConstructorListener {
    fun onSuggestionSelected(term: String, resultGroup: ResultGroup?, autocompleteSection: String?)
    fun onQuerySentToServer(query: String)
    fun onSuggestionsRetrieved(suggestions: List<Result>)
    fun onErrorGettingSuggestions(error: Throwable)
}