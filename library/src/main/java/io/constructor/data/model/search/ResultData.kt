package io.constructor.data.model.search

import com.squareup.moshi.Json
import io.constructor.data.model.ProductData

data class ResultData(@Json(name = "data") val result: Result, @Json(name = "matched_terms") val matchedTerms: List<String>?, val value: String)