package io.constructor.data.model.browse

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*;
import java.io.Serializable
import kotlin.collections.Collection

/**
 * Models browse groups response details
 */
@JsonClass(generateAdapter = true)
data class BrowseGroupsResponseInner(
        @Json(name = "groups") val facets: List<FilterGroup>?,
) : Serializable
