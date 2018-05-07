package io.constructor.core

import io.constructor.data.model.Suggestion


interface SuggestionListener {
    /**
     * triggered after getting suggestions
     * @param suggestions list of suggestions
     */
    fun onSuggestionsResult(suggestions: List<Suggestion>)

    /**
     * triggered on error
     * @param error - the exception
     */
    fun onSuggestionsError(error: Throwable)
}