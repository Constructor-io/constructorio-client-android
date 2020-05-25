package io.constructor.data.model.search

import com.squareup.moshi.Json
import java.io.Serializable

data class SearchResult(@Json(name = "data") val result: ResultData, @Json(name = "matched_terms") val matchedTerms: List<String>?, val value: String) : Serializable
