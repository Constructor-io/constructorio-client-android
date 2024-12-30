package io.constructor.data.model.search;

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*;
import java.io.Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class SearchResultLoadRequestBody(
        @Json(name = "search_term") val searchTerm: String,
        @Json(name = "items") val items: List<TrackingItem>?,
        @Json(name = "result_count") val resultCount: Int,
        @Json(name = "url") val url: String,
        @Json(name = "c") val c: String,
        @Json(name = "i") val i: String,
        @Json(name = "s") val s: Int,
        @Json(name = "key") val key: String,
        @Json(name = "ui") val ui: String?,
        @Json(name = "us") val us: List<String?>,
        @Json(name = "analytics_tags") val analyticsTags: Map<String, String>?,
        @Json(name= "beacon") val beacon: Boolean?,
        @Json(name= "section") val section: String?,
        @Json(name= "_dt") val _dt: Long?
) : Serializable
