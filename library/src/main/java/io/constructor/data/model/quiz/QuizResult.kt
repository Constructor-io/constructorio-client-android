package io.constructor.data.model.quiz

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
class QuizResult (
        @Json(name = "filter_expression") val filterExpression: Map<String, Any>,
        @Json(name = "results_url") val resultsUrl: String?,
) : Serializable