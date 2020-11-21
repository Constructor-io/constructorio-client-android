package io.constructor.data.model.browse

import com.squareup.moshi.Json

data class BrowseResponse(@Json(name = "response") val browseData: BrowseData, @Json(name = "result_id") val resultId: String, var rawData: String?)