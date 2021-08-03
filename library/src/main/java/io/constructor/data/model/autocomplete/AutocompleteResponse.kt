package io.constructor.data.model.autocomplete

import com.squareup.moshi.Json
import io.constructor.data.model.common.*;
import java.io.Serializable

/**
 * Models an autocomplete response
 */
data class AutocompleteResponse (
        @Json(name = "sections") val sections: Map<String, List<Result>>?,
        @Json(name = "result_id") val resultId: String?,
        var rawData: String?
) : Serializable