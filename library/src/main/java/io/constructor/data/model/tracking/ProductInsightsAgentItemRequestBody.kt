package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * @suppress
 * Shared by the Product Insights Agent focus and out of view events, which send the same fields
 */
@JsonClass(generateAdapter = true)
data class ProductInsightsAgentItemRequestBody(
        @Json(name = "item_id") val itemId: String,
        @Json(name = "item_name") val itemName: String?,
        @Json(name = "variation_id") val variationId: String?,
        @Json(name = "thread_id") val threadId: String?,
        @Json(name = "analytics_tags") val analyticsTags: Map<String, String>?
) : Serializable
