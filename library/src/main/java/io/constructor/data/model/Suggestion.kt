package io.constructor.data.model

import com.squareup.moshi.Json
import io.constructor.core.Constants

data class Suggestion(val value: String, val data: SuggestionData, @Json(name = "matched_terms") val matchedTerms: List<String>?, val sectionName: String = Constants.QueryValues.SEARCH_SUGGESTIONS)