package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models Features available for a response
 */
@JsonClass(generateAdapter = true)
data class Feature(
    @Json(name = "feature_name") val featureName: String?,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "enabled") val min: Boolean?,
    @Json(name = "variant") val variant: Map<String, Any>?,
) : Serializable