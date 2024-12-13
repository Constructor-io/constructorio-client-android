package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models a Recommendation Strategy object
 */
@JsonClass(generateAdapter = true)
data class RecommendationStrategy(
        @Json(name = "id") val id: String,
) : Serializable
