package io.constructor.data.model.search

import com.squareup.moshi.Json


data class SearchData(val facets: List<Facet>?, val groups: List<FilterGroup>?, val results: List<Result>?, @Json(name = "total_num_results") val resultCount: Int)