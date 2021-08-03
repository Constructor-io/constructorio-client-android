package io.constructor.data.model.common

import com.squareup.moshi.Json
import java.io.Serializable

/**
 * Models facet filter options available for a response
 */
data class FilterFacetOption(
        @Json(name = "count") val count: Int,
        @Json(name = "display_name") val displayName: String?,
        @Json(name = "status") val status: String?,
        @Json(name = "value") val value: String?
) : Serializable