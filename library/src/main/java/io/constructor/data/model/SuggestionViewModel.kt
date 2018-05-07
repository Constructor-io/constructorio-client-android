package io.constructor.data.model

import java.io.Serializable

data class SuggestionViewModel(val term: String, val group: Group?, val section: String?, val matchedTerms: List<String>?) : Serializable