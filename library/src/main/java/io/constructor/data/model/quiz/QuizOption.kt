package io.constructor.data.model.quiz

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class QuizOption (
        @Json(name = "id") val id: Int?,
        @Json(name = "value") val value: String?,
        @Json(name = "attribute") val attribute: QuizOptionAttribute?,
        @Json(name = "images") val images: QuizImages?,
) : Serializable