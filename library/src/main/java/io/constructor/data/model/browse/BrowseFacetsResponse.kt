package io.constructor.data.model.browse

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models a browse Facets response
 */
@JsonClass(generateAdapter = true)
data class BrowseFacetsResponse(
        @Json(name = "response") val response: BrowseFacetsResponseInner?,
        @Json(name = "result_id") val resultId: String?,
        var rawData: String?
) : Serializable
