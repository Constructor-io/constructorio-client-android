package io.constructor.data.model.search

import com.squareup.moshi.Json
import io.constructor.data.model.ProductData

data class Group(val value: String, val data: ProductData, @Json(name = "matched_terms") val matchedTerms: List<String>?)