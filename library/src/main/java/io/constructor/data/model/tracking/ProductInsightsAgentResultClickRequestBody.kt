package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
class ProductInsightsAgentResultClickRequestBody(
    itemId: String,
    @Json(name = "question") val question: String,
    @Json(name = "seed_item_id") val seedItemId: String,
    @Json(name = "position") val position: Int?,
    @Json(name = "seed_item_name") val seedItemName: String?,
    @Json(name = "seed_variation_id") val seedVariationId: String?,
    @Json(name = "qna_result_id") val qnaResultId: String?,
    itemName: String?,
    variationId: String?,
    threadId: String?,
    analyticsTags: Map<String, String>?,
) : ProductInsightsAgentRequestBody(itemId, itemName, variationId, threadId, analyticsTags)
