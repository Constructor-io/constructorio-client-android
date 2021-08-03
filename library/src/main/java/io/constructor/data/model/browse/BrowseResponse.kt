package io.constructor.data.model.browse

import com.squareup.moshi.Json
import java.io.Serializable

/**
 * Models a browse response
 */
data class BrowseResponse(
        @Json(name = "response") val response: BrowseResponseInner?,
        @Json(name = "result_id") val resultId: String?,
        var rawData: String?
) : Serializable