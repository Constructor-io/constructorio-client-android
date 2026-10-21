package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class AgentResultClickRequestBody(
        @Json(name = "intent") val intent: String,
        @Json(name = "search_result_id") val searchResultId: String,
        @Json(name = "item_id") val itemId: String?,
        @Json(name = "item_name") val itemName: String?,
        @Json(name = "variation_id") val variationId: String?,
        @Json(name = "beacon") val beacon: Boolean = true,
        @Json(name = "section") val section: String?,
        @Json(name = "intent_result_id") val intentResultId: String?,
        @Json(name = "c") val c: String,
        @Json(name = "i") val i: String,
        @Json(name = "s") val s: Int,
        @Json(name = "key") val key: String,
        @Json(name = "ui") val ui: String?,
        @Json(name = "us") val us: List<String?>,
        @Json(name = "analytics_tags") val analyticsTags: Map<String, String>?,
        @Json(name = "_dt") val _dt: Long?
) : Serializable
