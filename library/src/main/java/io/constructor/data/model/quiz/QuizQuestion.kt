package io.constructor.data.model.quiz

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models quiz question data
 */

@JsonClass(generateAdapter = true)
data class QuizQuestion (
        @Json(name = "id") val id: Int?,
        @Json(name = "title") val title: String?,
        @Json(name = "description") val description: String?,
        @Json(name = "type") val type: String?,
        @Json(name = "cta_text") val ctaText: String?,
        @Json(name = "images") val images: QuizImages?,
        @Json(name = "options") val options: List<QuizOption>?,
        @Json(name = "input_placeholder") val inputPlaceholder: String?,
) : Serializable