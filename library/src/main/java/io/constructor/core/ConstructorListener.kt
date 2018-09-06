package io.constructor.core

import io.constructor.data.model.ResultGroup
import io.constructor.data.model.Suggestion


interface ConstructorListener {
    fun onSuggestionSelected(term: String, group: ResultGroup?, autocompleteSection: String?)
    fun onQuerySentToServer(query: String)
    fun onSuggestionsRetrieved(suggestions: List<Suggestion>)
    fun onErrorGettingSuggestions(error: Throwable)
}