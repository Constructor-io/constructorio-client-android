package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models a question shown by the Product Insights Agent, used in Product Insights Agent tracking events
 *
 * @property question the question text, i.e. "Is it waterproof?"
 * @property questionTopic the topic the question belongs to, i.e. "Durability"
 */
@JsonClass(generateAdapter = true)
data class ProductInsightsAgentQuestion(
    @Json(name = "question") val question: String,
    @Json(name = "question_topic") val questionTopic: String? = null,
) : Serializable
