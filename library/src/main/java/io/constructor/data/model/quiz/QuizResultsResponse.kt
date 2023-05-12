package io.constructor.data.model.quiz

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models quiz results response
 */
@JsonClass(generateAdapter = true)
data class QuizResultsResponse(
    @Json(name="request") val request: Map<String, Any>,
    @Json(name="response") val response: QuizResultsResponseInner,
    @Json(name="result_id") val resultId: String,
    @Json(name="quiz_version_id") val quizVersionId: String,
    @Json(name="quiz_session_id") val quizSessionId: String,
    @Json(name="quiz_id") val quizId: String,
    var rawData: String?
) : Serializable