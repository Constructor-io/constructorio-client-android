package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
class ProductInsightsAgentViewsRequestBody(
    itemId: String,
    @Json(name = "questions") val questions: List<ProductInsightsAgentQuestion>,
    @Json(name = "view_timespans") val viewTimespans: List<ProductInsightsAgentViewTimespanBody>,
    itemName: String?,
    variationId: String?,
    threadId: String?,
    analyticsTags: Map<String, String>?,
) : ProductInsightsAgentRequestBody(itemId, itemName, variationId, threadId, analyticsTags)
