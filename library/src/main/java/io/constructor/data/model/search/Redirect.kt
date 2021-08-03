package io.constructor.data.model.search

import com.squareup.moshi.Json
import java.io.Serializable

/**
 * Models search redirects
 */
data class Redirect(
        @Json(name = "data") val data: RedirectData,
        @Json(name = "matched_terms") val matchedTerms: List<String>?,
        @Json(name = "matched_user_segments") val matchedUserSegments: String?
) : Serializable
