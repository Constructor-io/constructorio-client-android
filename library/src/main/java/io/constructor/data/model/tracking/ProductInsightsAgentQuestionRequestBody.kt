package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * @suppress
 * Shared by the Product Insights Agent question click and question submit events, which send the same fields
 */
@JsonClass(generateAdapter = true)
data class ProductInsightsAgentQuestionRequestBody(
        @Json(name = "item_id") val itemId: String,
        @Json(name = "question") val question: String,
        @Json(name = "question_topic") val questionTopic: String?,
        @Json(name = "item_name") val itemName: String?,
        @Json(name = "variation_id") val variationId: String?,
        @Json(name = "thread_id") val threadId: String?,
        @Json(name = "analytics_tags") val analyticsTags: Map<String, String>?
) : Serializable
