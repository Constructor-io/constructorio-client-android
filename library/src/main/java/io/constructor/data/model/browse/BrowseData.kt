package io.constructor.data.model.browse

import com.squareup.moshi.Json
import io.constructor.data.model.search.SortOption

data class BrowseData(val facets: List<BrowseFacet>?, val groups: List<BrowseGroup>?, @Json(name = "results") val browseResults: List<BrowseResult>?, @Json(name = "sort_options") val sortOptions: List<SortOption>? = null, @Json(name = "total_num_results") val resultCount: Int)
