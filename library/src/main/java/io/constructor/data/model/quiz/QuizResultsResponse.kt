package io.constructor.data.model.quiz

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models quiz results response
 */
@JsonClass(generateAdapter = true)
data class QuizResultsResponse(
    @Json(name="version_id") val versionId: String?,
    @Json(name="result") val result: QuizResult?,
    var rawData: String?
) : Serializable