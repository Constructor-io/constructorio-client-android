package io.constructor.data.model.search

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models search redirects
 */
@JsonClass(generateAdapter = true)
data class Redirect(
        @Json(name = "data") val data: RedirectData,
        @Json(name = "matched_terms") val matchedTerms: List<String>?,
        @Json(name = "matched_user_segments") val matchedUserSegments: String?
) : Serializable
