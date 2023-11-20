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
    @Json(name="quiz_version_id") val quizVersionId: String?,
    @Json(name="quiz_session_id") val quizSessionId: String?,
    @Json(name="quiz_id") val quizId: String,
    var rawData: String?
) : Serializable