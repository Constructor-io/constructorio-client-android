package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*;
import java.io.Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class ItemDetailLoadRequestBody(
        @Json(name = "item_name") val itemName: String,
        @Json(name = "item_id") val itemId: String,
        @Json(name = "variation_id") val variationId: String?,
        @Json(name = "url") val url: String,
        @Json(name = "c") val c: String,
        @Json(name = "i") val i: String,
        @Json(name = "s") val s: Int,
        @Json(name = "key") val key: String,
        @Json(name = "ui") val ui: String?,
        @Json(name = "us") val us: List<String?>,
        @Json(name= "beacon") val beacon: Boolean?,
        @Json(name= "section") val section: String?,
        @Json(name= "_dt") val _dt: Long?
) : Serializable
