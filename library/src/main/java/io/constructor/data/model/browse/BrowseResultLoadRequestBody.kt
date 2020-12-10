package io.constructor.data.model.browse

import com.squareup.moshi.Json
import io.constructor.data.model.common.*;
import java.io.Serializable

data class BrowseResultLoadRequestBody(
        @Json(name = "filter_name") val filterName: String,
        @Json(name = "filter_value") val filterValue: String,
        @Json(name = "result_count") val resultCount: Int?
) : Serializable