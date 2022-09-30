package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models an individual refined content
 */
@JsonClass(generateAdapter = true)
data class RefinedContent (
    @Json(name="data") val data:Map<String, Any?>?,
) : Serializable