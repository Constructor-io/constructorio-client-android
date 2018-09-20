package io.constructor.data.model

import com.squareup.moshi.Json

data class AutocompleteResponse(val sections: Sections, @Json(name = "result_id") val resultId: String?)