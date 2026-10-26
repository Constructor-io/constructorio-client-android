package io.constructor.data.model.tracking

import com.squareup.moshi.JsonClass

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
class ProductInsightsAgentOutOfViewRequestBody(
    itemId: String,
    itemName: String?,
    variationId: String?,
    threadId: String?,
    analyticsTags: Map<String, String>?,
) : ProductInsightsAgentRequestBody(itemId, itemName, variationId, threadId, analyticsTags)
