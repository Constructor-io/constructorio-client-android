package io.constructor.data.model.browse

import com.squareup.moshi.Json
import io.constructor.data.model.common.*;
import java.io.Serializable

data class BrowseResponseInner(
        @Json(name = "facets") val facets: List<FilterFacet>?,
        @Json(name = "groups") val groups: List<FilterGroup>?,
        @Json(name = "results") val results: List<Result>?,
        @Json(name = "sort_options") val filterSortOptions: List<FilterSortOption>? = null,
        @Json(name = "total_num_results") val resultCount: Int
) : Serializable