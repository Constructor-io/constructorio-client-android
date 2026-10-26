package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class ProductInsightsAgentResultClickRequestBody(
        @Json(name = "item_id") val itemId: String,
        @Json(name = "question") val question: String,
        @Json(name = "seed_item_id") val seedItemId: String,
        @Json(name = "position") val position: Int?,
        @Json(name = "seed_item_name") val seedItemName: String?,
        @Json(name = "seed_variation_id") val seedVariationId: String?,
        @Json(name = "qna_result_id") val qnaResultId: String?,
        @Json(name = "item_name") val itemName: String?,
        @Json(name = "variation_id") val variationId: String?,
        @Json(name = "thread_id") val threadId: String?,
        @Json(name = "analytics_tags") val analyticsTags: Map<String, String>?
) : Serializable
