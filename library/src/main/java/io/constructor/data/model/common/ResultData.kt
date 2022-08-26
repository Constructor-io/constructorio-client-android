package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models item metadata
 */
@JsonClass(generateAdapter = true)
data class ResultData(
        @Json(name = "description") val description: String?,
        @Json(name = "id") val id: String?,
        @Json(name = "url") val url: String?,
        @Json(name = "image_url") val imageUrl: String?,
        @Json(name = "groups") val groups: List<ResultGroup>?,
        @Json(name = "facets") val facets: List<ResultFacet>?,
        @Json(name = "metadata") var metadata: Map<String, Any?>?
) : Serializable
