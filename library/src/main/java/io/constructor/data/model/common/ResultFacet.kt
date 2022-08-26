package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models facets on an item
 */
@JsonClass(generateAdapter = true)
data class ResultFacet(
        @Json(name = "name") val name: String,
        @Json(name = "values") val values: List<String>?
) : Serializable
