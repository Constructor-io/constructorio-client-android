package io.constructor.data.model.search

import com.squareup.moshi.Json

data class SearchFacet(val name: String,
                       @Json(name = "display_name") val displayName: String?,
                       val status: Map<String, Any>?,
                       val type: String?,
                       val min: Int?,
                       val max: Int?,
                       val options: List<FacetOption>?)
