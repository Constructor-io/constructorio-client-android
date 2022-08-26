package io.constructor.data.model.recommendations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*;
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class RecommendationsResponseInner(
        @Json(name = "pod") val pod: Pod?,
        @Json(name = "results") val results: List<Result>?,
        @Json(name = "total_num_results") val resultCount: Int
) : Serializable
