package io.constructor.data.model.recommendations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class RecommendationsResponse(
        @Json(name = "response") val response: RecommendationsResponseInner?,
        @Json(name = "result_id") val resultId: String?,
        var rawData: String?
) : Serializable
