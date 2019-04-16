package io.constructor.data.model.search

import com.squareup.moshi.Json

data class SearchResponse(@Json(name = "response") val searchData: SearchData, @Json(name = "result_id") val resultId: String, var rawData: String?)