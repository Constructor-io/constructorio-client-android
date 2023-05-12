package io.constructor.data.model.quiz

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*
import java.io.Serializable

/**
 * Models quiz results response
 */
@JsonClass(generateAdapter = true)
data class QuizResultsResponseInner(
    @Json(name = "facets") val facets: List<FilterFacet>?,
    @Json(name = "groups") val groups: List<FilterGroup>?,
    @Json(name = "results") val results: List<Result>?,
    @Json(name = "sort_options") val filterSortOptions: List<FilterSortOption>? = null,
    @Json(name = "total_num_results") val resultCount: Int,
    @Json(name = "refined_content") val refinedContent: List<RefinedContent>?,
    @Json(name = "features") val features: List<Feature>?,
    @Json(name = "result_sources") val resultSources: ResultSources?,
) : Serializable