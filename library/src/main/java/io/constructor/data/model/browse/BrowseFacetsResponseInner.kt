package io.constructor.data.model.browse

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*;
import java.io.Serializable
import kotlin.collections.Collection

/**
 * Models browse facets response details
 */
@JsonClass(generateAdapter = true)
data class BrowseFacetsResponseInner(
        @Json(name = "facets") val facets: List<FilterFacet>?,
        @Json(name = "total_num_results") val resultCount: Int,
) : Serializable
