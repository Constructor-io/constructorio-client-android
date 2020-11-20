package io.constructor.data.model.browse

import com.squareup.moshi.Json
import io.constructor.data.model.search.ResultData
import java.io.Serializable

data class BrowseResult(@Json(name = "data") val result: ResultData, @Json(name = "matched_terms") val matchedTerms: List<String>?, val value: String) : Serializable
