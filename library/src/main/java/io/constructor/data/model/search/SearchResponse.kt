package io.constructor.data.model.search

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models search response
 */
@JsonClass(generateAdapter = true)
data class SearchResponse(
        @Json(name = "response") val response: SearchResponseInner?,
        @Json(name= "request") val request:Map<String, Any?>?,
        @Json(name = "result_id") val resultId: String?,
        var rawData: String?
) : Serializable
