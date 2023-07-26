package io.constructor.data.model.quiz

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*;
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class QuizResultClickRequestBody(
    @Json(name = "quiz_id") val quizId: String,
    @Json(name = "quiz_version_id") val quizVersionId: String,
    @Json(name = "quiz_session_id") val quizSessionId: String,
    @Json(name = "result_page") val resultPage: Int?,
    @Json(name = "result_count") val resultCount: Int?,
    @Json(name = "num_results_per_page") val numResultsPerPage: Int?,
    @Json(name = "result_id") val resultId: String?,
    @Json(name = "item_id") val itemId: String,
    @Json(name = "item_name") val itemName: String?,
    @Json(name = "variation_id") val variationId: String?,
    @Json(name = "action_class") val actionClass: String?,
    @Json(name = "c") val c: String,
    @Json(name = "i") val i: String,
    @Json(name = "s") val s: Int,
    @Json(name = "key") val key: String,
    @Json(name = "ui") val ui: String?,
    @Json(name = "us") val us: List<String?>,
    @Json(name= "beacon") val beacon: Boolean?,
    @Json(name= "section") val section: String?,
    @Json(name= "_dt") val _dt: Long?
) : Serializable
