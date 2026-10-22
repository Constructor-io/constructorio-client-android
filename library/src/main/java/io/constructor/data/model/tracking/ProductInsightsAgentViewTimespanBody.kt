package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class ProductInsightsAgentViewTimespanBody(
    @Json(name = "start") val start: String,
    @Json(name = "end") val end: String,
) : Serializable
