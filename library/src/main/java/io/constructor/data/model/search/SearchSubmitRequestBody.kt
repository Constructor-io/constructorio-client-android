package io.constructor.data.model.search

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class SearchSubmitRequestBody(
        @Json(name = "search_term") val searchTerm: String,
        @Json(name = "user_input") val userInput: String,
        @Json(name = "filters") val filters: SearchSubmitFilters?,
        @Json(name = "analytics_tags") val analyticsTags: Map<String, String>?
) : Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class SearchSubmitFilters(
        @Json(name = "group_id") val groupId: String
) : Serializable
