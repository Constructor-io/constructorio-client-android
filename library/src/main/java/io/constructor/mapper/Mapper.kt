package io.constructor.mapper

import io.constructor.core.Constants
import io.constructor.data.model.Suggestion
import io.constructor.data.model.SuggestionViewModel
import java.util.*


object Mapper {

    fun toSuggestionsViewModel(suggestions: List<Suggestion>, categoriesPerTerm : Int = Int.MAX_VALUE): List<SuggestionViewModel> {
        val data = ArrayList<SuggestionViewModel>()
        suggestions.mapIndexed { index, suggestion ->
            if (index == 0) {
                suggestion.data.groups.take(categoriesPerTerm).mapTo(data) { SuggestionViewModel(suggestion.value, it, Constants.QueryValues.SEARCH_SUGGESTIONS, suggestion.matchedTerms) }
            } else {
                data.add(SuggestionViewModel(suggestion.value, null, Constants.QueryValues.SEARCH_SUGGESTIONS, suggestion.matchedTerms))
            }
        }
        return data
    }

}