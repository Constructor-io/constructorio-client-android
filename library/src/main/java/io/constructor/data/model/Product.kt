package io.constructor.data.model

import com.squareup.moshi.Json

data class Product(val value: String, val data: ProductData, @Json(name = "matched_terms") val matchedTerms: List<String>?)