package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.TrackingItem

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
class ProductInsightsAgentAnswerViewRequestBody(
    itemId: String,
    @Json(name = "question") val question: String,
    @Json(name = "answer_text") val answerText: String,
    @Json(name = "question_topic") val questionTopic: String?,
    @Json(name = "qna_result_id") val qnaResultId: String?,
    @Json(name = "items") val items: List<TrackingItem>?,
    @Json(name = "follow_up_questions") val followUpQuestions: List<ProductInsightsAgentFollowUpQuestion>?,
    itemName: String?,
    variationId: String?,
    threadId: String?,
    analyticsTags: Map<String, String>?,
) : ProductInsightsAgentRequestBody(itemId, itemName, variationId, threadId, analyticsTags)
