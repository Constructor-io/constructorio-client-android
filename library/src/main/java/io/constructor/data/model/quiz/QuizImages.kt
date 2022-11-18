package io.constructor.data.model.quiz

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class QuizImages (
        @Json(name = "primary_url") val primaryUrl: String?,
        @Json(name = "primary_alt") val primaryAlt: String?,
        @Json(name = "secondary_url") val secondaryUrl: String?,
        @Json(name = "secondary_alt") val secondaryAlt: String?,
) : Serializable