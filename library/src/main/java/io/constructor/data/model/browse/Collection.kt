package io.constructor.data.model.browse

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models a collection response
 */
@JsonClass(generateAdapter = true)
data class Collection(
        @Json(name = "data") val data: Map<String, Any>?,
        @Json(name = "display_name") val displayName: String,
        @Json(name = "id") val id: String,
) : Serializable
