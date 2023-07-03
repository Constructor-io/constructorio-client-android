package io.constructor.data.model.browse

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models a browse response
 */
@JsonClass(generateAdapter = true)
data class BrowseResponse(
        @Json(name = "response") val response: BrowseResponseInner?,
        @Json(name = "result_id") val resultId: String?,
        @Json(name= "request") val request:Map<String, Any?>?,
        var rawData: String?
) : Serializable
