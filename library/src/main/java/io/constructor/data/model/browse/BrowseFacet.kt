package io.constructor.data.model.browse

import com.squareup.moshi.Json
import java.io.Serializable
import io.constructor.data.model.search.FacetOption


data class BrowseFacet(val name: String,
                       @Json(name = "display_name") val displayName: String?,
                       val status: Map<String, Any>?,
                       val type: String?,
                       val min: Double?,
                       val max: Double?,
                       val options: List<FacetOption>?) : Serializable
