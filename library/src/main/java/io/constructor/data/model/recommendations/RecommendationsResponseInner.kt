package io.constructor.data.model.recommendations

import com.squareup.moshi.Json
import io.constructor.data.model.common.*;
import java.io.Serializable

data class RecommendationsResponseInner(
        @Json(name = "pod") val pod: Pod?,
        @Json(name = "facets") val facets: List<FilterFacet>?,
        @Json(name = "results") val results: List<Result>?,
        @Json(name = "total_num_results") val resultCount: Int
) : Serializable
