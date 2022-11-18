package io.constructor.data.model.quiz

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models quiz question response
 */
@JsonClass(generateAdapter = true)
data class QuizQuestionResponse(
    @Json(name="next_question") val nextQuestion: QuizQuestion?,
    @Json(name="version_id") val versionId: String?,
    @Json(name="is_last_question") val isLastQuestion: Boolean?,
    var rawData: String?
) : Serializable