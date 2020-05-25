package io.constructor.data.model.search

import com.squareup.moshi.Json
import java.io.Serializable

data class SearchFacet(val name: String,
                       @Json(name = "display_name") val displayName: String?,
                       val status: Map<String, Any>?,
                       val type: String?,
                       val min: Double?,
                       val max: Double?,
                       val options: List<FacetOption>?) : Serializable
