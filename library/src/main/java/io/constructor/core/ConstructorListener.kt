package io.constructor.core

import io.constructor.data.model.Group
import io.constructor.data.model.Suggestion


interface ConstructorListener {
    fun onSuggestionSelected(term: String, group: Group?, autocompleteSection: String?)
    fun onQuerySentToServer(query: String)
    fun onSuggestionsRetrieved(suggestions: List<Suggestion>)
    fun onErrorGettingSuggestions(error: Throwable)
}