package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models result sources
 */
@JsonClass(generateAdapter = true)
data class ResultSources(
    @Json(name = "token_match") val tokenMatch: ResultSourcesData?,
    @Json(name = "embeddings_match") val embeddingsMatch: ResultSourcesData?,
) : Serializable
