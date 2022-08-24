package io.constructor.data.model.recommendations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Pod(
        @Json(name = "id") val response: String?,
        @Json(name = "display_name") val resultId: String?
) : Serializable
