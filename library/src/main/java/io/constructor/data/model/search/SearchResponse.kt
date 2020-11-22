package io.constructor.data.model.search

import com.squareup.moshi.Json
import java.io.Serializable

data class SearchResponse(
        @Json(name = "response") val response: SearchResponseInner?,
        @Json(name = "result_id") val resultId: String?,
        var rawData: String?
) : Serializable