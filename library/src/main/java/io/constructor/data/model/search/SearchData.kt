package io.constructor.data.model.search

import com.squareup.moshi.Json


data class SearchData(val facets: List<SearchFacet>?, val groups: List<SearchGroup>?, @Json(name = "results") val searchResults: List<SearchResult>?, @Json(name = "sort_options") val sortOptions: List<SortOption>? = null, @Json(name = "total_num_results") val resultCount: Int)
