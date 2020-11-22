package io.constructor.data.model.common

import com.squareup.moshi.Json
import java.io.Serializable

data class Result(
        @Json(name = "data") val data: ResultData,
        @Json(name = "matched_terms") val matchedTerms: List<String>?,
        @Json(name = "variations") val variations: List<Result>?,
        @Json(name = "value") val value: String
) : Serializable
