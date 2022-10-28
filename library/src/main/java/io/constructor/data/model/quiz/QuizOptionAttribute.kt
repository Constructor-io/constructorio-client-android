package io.constructor.data.model.quiz

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
class QuizOptionAttribute (
        @Json(name = "name") val name: String?,
        @Json(name = "value") val value: String?,
        @Json(name = "images") val images: QuizImages?,
) : Serializable