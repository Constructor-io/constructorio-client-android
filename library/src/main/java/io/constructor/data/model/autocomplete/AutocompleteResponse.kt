package io.constructor.data.model.autocomplete

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*;
import java.io.Serializable

/**
 * Models an autocomplete response
 */
@JsonClass(generateAdapter = true)
data class AutocompleteResponse (
        @Json(name = "sections") val sections: Map<String, List<Result>>?,
        @Json(name = "result_id") val resultId: String?,
        @Json(name= "request") val request:Map<String, Any?>?,
        var rawData: String?
) : Serializable
