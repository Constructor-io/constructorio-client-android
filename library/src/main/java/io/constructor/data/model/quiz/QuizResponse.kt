package io.constructor.data.model.quiz

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models quiz response
 */
@JsonClass(generateAdapter = true)
data class QuizResponse(
        @Json(name="next_question") val nextQuestion: QuizQuestion?,
        @Json(name="version_id") val versionId: String?,
        @Json(name="is_last_question") val isLastQuestion: Boolean?,
        @Json(name="result") val result: QuizResult?,
        var rawData: String?
) : Serializable