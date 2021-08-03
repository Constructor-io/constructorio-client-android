package io.constructor.data.model.common

import com.squareup.moshi.Json
import java.io.Serializable

/**
 * Models sorting options available for a response
 */
data class FilterSortOption(
        @Json(name = "display_name") val displayName: String,
        @Json(name = "sort_by") val sortBy: String,
        @Json(name = "sort_order") val sortOrder: String,
        @Json(name = "status") val status: String
) : Serializable