package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models group filters available for a response
 */
@JsonClass(generateAdapter = true)
data class FilterGroup(
        @Json(name = "children") val children: List<FilterGroup>?,
        @Json(name = "parents") val parents: List<FilterGroup>?,
        @Json(name = "count") val count: Int?,
        @Json(name = "display_name") val displayName: String,
        @Json(name = "group_id") val groupId: String
) : Serializable
