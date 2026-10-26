package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
class ProductInsightsAgentAnswerFeedbackRequestBody(
    itemId: String,
    @Json(name = "feedback_label") val feedbackLabel: ProductInsightsAgentFeedbackLabel,
    @Json(name = "qna_result_id") val qnaResultId: String?,
    itemName: String?,
    variationId: String?,
    threadId: String?,
    analyticsTags: Map<String, String>?,
) : ProductInsightsAgentRequestBody(itemId, itemName, variationId, threadId, analyticsTags)
