package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models a item to be used in tracking
 */
@JsonClass(generateAdapter = true)
data class TrackingItem(
    @Json(name = "item_id") val itemId: String?,
    @Json(name = "variation_id") val variationId: String?,
) : Serializable
