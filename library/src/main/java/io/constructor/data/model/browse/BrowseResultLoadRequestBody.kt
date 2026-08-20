package io.constructor.data.model.browse

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*;
import java.io.Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class BrowseResultLoadRequestBody(
        @Json(name = "filter_name") val filterName: String,
        @Json(name = "filter_value") val filterValue: String,
        @Json(name = "items") val items: List<TrackingItem>?,
        @Json(name = "result_count") val resultCount: Int,
        @Json(name = "result_id") val resultId: String?,
        @Json(name = "result_page") val resultPage: Int?,
        @Json(name = "result_offset") val resultOffset: Int?,
        @Json(name = "sort_order") val sortOrder: String?,
        @Json(name = "sort_by") val sortBy: String?,
        @Json(name = "selected_filters") val selectedFilters: Map<String, List<String>>?,
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
