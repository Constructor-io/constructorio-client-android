package io.constructor.data.model.common

import com.squareup.moshi.Json
import java.io.Serializable

/**
 * Models result sources
 */
data class ResultSources(
    @Json(name = "token_match") val tokenMatch: ResultSourcesData?,
    @Json(name = "embeddings_match") val embeddingsMatch: ResultSourcesData?,
) : Serializable
