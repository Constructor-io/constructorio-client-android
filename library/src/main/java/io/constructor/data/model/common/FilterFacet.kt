package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models facet filters available for a response
 */
@JsonClass(generateAdapter = true)
data class FilterFacet(
       @Json(name = "display_name") val displayName: String?,
       @Json(name = "name") val name: String,
       @Json(name = "status") val status: Map<String, Any>?,
       @Json(name = "min") val min: Double?,
       @Json(name = "max") val max: Double?,
       @Json(name = "options") val options: List<FilterFacetOption>?,
       @Json(name = "type") val type: String?,
       @Json(name = "hidden") val hidden: Boolean?,
) : Serializable
