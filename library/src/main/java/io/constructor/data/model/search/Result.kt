package io.constructor.data.model.search

import com.squareup.moshi.Json

data class Result(@Json(name = "data") val result: ResultData, @Json(name = "matched_terms") val matchedTerms: List<String>?, val value: String)
