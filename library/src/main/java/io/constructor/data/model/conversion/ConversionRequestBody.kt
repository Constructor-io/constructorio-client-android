package io.constructor.data.model.conversion

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*;
import java.io.Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class ConversionRequestBody(
        @Json(name = "search_term") val searchTerm: String,
        @Json(name = "item_id") val itemID: String,
        @Json(name = "variation_id") val variationId: String?,
        @Json(name = "item_name") val itemName: String,
        @Json(name = "revenue") val revenue: String,
        @Json(name = "type") val conversionType: String?,
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
