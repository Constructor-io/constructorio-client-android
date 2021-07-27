package io.constructor.core

import io.constructor.data.model.common.Result

/**
 * @suppress
 */
interface SuggestionListener {
    /**
     * triggered after getting suggestions
     * @param suggestions list of suggestions
     */
    fun onSuggestionsResult(suggestions: List<Result>)

    /**
     * triggered on error
     * @param error - the exception
     */
    fun onSuggestionsError(error: Throwable)
}