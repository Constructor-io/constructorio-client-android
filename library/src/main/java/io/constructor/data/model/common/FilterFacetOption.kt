package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models facet filter options available for a response
 */
@JsonClass(generateAdapter = true)
data class FilterFacetOption(
        @Json(name = "count") val count: Int,
        @Json(name = "display_name") val displayName: String?,
        @Json(name = "status") val status: String?,
        @Json(name = "value") val value: String?,
        @Json(name = "data") val data: Map<String, Any?>?,
) : Serializable
