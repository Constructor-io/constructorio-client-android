package io.constructor.data.model.common

import com.squareup.moshi.Json
import java.io.Serializable

data class FilterFacet(
       @Json(name = "display_name") val displayName: String?,
       @Json(name = "name") val name: String,
       @Json(name = "status") val status: Map<String, Any>?,
       @Json(name = "min") val min: Double?,
       @Json(name = "max") val max: Double?,
       @Json(name = "options") val options: List<FilterFacetOption>?,
       @Json(name = "type") val type: String?
) : Serializable
