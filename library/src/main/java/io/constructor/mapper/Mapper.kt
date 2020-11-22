package io.constructor.mapper

import io.constructor.core.Constants
import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.data.model.common.Result

import java.util.*

object Mapper {

    fun toSuggestionsViewModel(response: AutocompleteResponse, groupsShownForFirstTerm : Int = Int.MAX_VALUE): List<Result> {
        val data = ArrayList<Result>()
        response.sections.get("Search Suggestions")?.mapIndexed { _, suggestion ->
            data.add(suggestion);
        }
        return data;
    }

}