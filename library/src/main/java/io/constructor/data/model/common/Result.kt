package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models an individual item in a response as well as its variations
 */
@JsonClass(generateAdapter = true)
data class Result(
        @Json(name = "data") val data: ResultData,
        @Json(name = "matched_terms") val matchedTerms: List<String>?,
        @Json(name = "variations") val variations: List<Result>?,
        @Json(name = "variations_map") val variationsMap: Any?,
        @Json(name = "value") val value: String
) : Serializable
