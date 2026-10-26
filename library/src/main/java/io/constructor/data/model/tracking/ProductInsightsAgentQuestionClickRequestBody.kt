package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
class ProductInsightsAgentQuestionClickRequestBody(
    itemId: String,
    @Json(name = "question") val question: String,
    @Json(name = "question_topic") val questionTopic: String?,
    itemName: String?,
    variationId: String?,
    threadId: String?,
    analyticsTags: Map<String, String>?,
) : ProductInsightsAgentRequestBody(itemId, itemName, variationId, threadId, analyticsTags)
