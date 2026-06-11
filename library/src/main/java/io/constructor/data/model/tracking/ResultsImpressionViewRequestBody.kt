package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.ResultsImpressionItem
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class ResultsImpressionViewRequestBody(
        @Json(name = "items") val items: List<ResultsImpressionItem>,
        @Json(name = "search_term") val searchTerm: String?,
        @Json(name = "filter_name") val filterName: String?,
        @Json(name = "filter_value") val filterValue: String?,
        @Json(name = "analytics_tags") val analyticsTags: Map<String, String>?,
        @Json(name = "beacon") val beacon: Boolean = true,
        @Json(name = "c") val c: String,
        @Json(name = "i") val i: String,
        @Json(name = "s") val s: Int,
        @Json(name = "key") val key: String,
        @Json(name = "ui") val ui: String?,
        @Json(name = "us") val us: List<String?>,
        @Json(name = "_dt") val _dt: Long?
) : Serializable
