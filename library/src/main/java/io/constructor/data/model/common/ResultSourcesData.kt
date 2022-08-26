package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models result sources data
 */
@JsonClass(generateAdapter = true)
data class ResultSourcesData(
    @Json(name = "count") val count: Int?,
) : Serializable
