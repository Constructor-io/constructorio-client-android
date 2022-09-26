package io.constructor.data.model.search;

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*;
import java.io.Serializable

/**
 * Models search response details
 */
@JsonClass(generateAdapter = true)
data class SearchResponseInner(
        @Json(name = "facets") val facets: List<FilterFacet>?,
        @Json(name = "groups") val groups: List<FilterGroup>?,
        @Json(name = "results") val results: List<Result>?,
        @Json(name = "sort_options") val filterSortOptions: List<FilterSortOption>? = null,
        @Json(name = "total_num_results") val resultCount: Int?,
        @Json(name = "redirect") val redirect: Redirect?,
        @Json(name = "result_sources") val resultSources: ResultSources?,
        @Json(name = "refined_content") val refinedContent: List<RefinedContent>?
) : Serializable
