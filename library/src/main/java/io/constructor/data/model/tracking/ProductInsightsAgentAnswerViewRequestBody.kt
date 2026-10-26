package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.TrackingItem
import java.io.Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class ProductInsightsAgentAnswerViewRequestBody(
        @Json(name = "item_id") val itemId: String,
        @Json(name = "question") val question: String,
        @Json(name = "answer_text") val answerText: String,
        @Json(name = "question_topic") val questionTopic: String?,
        @Json(name = "qna_result_id") val qnaResultId: String?,
        @Json(name = "items") val items: List<TrackingItem>?,
        @Json(name = "follow_up_questions") val followUpQuestions: List<ProductInsightsAgentFollowUpQuestion>?,
        @Json(name = "item_name") val itemName: String?,
        @Json(name = "variation_id") val variationId: String?,
        @Json(name = "thread_id") val threadId: String?,
        @Json(name = "analytics_tags") val analyticsTags: Map<String, String>?
) : Serializable
