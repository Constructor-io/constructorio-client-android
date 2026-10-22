package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class ProductInsightsAgentFollowUpQuestion(
    @Json(name = "value") val value: String,
) : Serializable
