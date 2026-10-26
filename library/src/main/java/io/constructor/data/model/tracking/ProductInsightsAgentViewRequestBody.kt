package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class ProductInsightsAgentViewRequestBody(
        @Json(name = "item_id") val itemId: String,
        @Json(name = "questions") val questions: List<ProductInsightsAgentQuestion>,
        @Json(name = "item_name") val itemName: String?,
        @Json(name = "variation_id") val variationId: String?,
        @Json(name = "thread_id") val threadId: String?,
        @Json(name = "analytics_tags") val analyticsTags: Map<String, String>?
) : Serializable
