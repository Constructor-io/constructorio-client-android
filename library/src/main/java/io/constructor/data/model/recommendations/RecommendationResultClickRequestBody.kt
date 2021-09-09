package io.constructor.data.model.recommendations

import com.squareup.moshi.Json
import io.constructor.data.model.common.*;
import java.io.Serializable

data class RecommendationResultClickRequestBody(
        @Json(name = "pod_id") val podId: String,
        @Json(name = "strategy_id") val strategyId: String?,
        @Json(name = "item_id") val itemId: String,
        @Json(name = "variation_id") val variationId: String?,
        @Json(name = "result_id") val resultId: String?,
        @Json(name = "num_results_per_page") val numResultsPerPage: Int?,
        @Json(name = "result_page") val resultPage: Int?,
        @Json(name = "result_count") val resultCount: Int?,
        @Json(name = "result_position_on_page") val resultPositionOnPage: Int?,
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