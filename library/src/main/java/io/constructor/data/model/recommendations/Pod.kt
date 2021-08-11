package io.constructor.data.model.recommendations

import com.squareup.moshi.Json
import java.io.Serializable

data class Pod(
        @Json(name = "id") val response: String?,
        @Json(name = "display_name") val resultId: String?
) : Serializable