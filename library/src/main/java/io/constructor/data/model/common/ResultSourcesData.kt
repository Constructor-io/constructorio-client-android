package io.constructor.data.model.common

import com.squareup.moshi.Json
import java.io.Serializable

/**
 * Models result sources data
 */
data class ResultSourcesData(
    @Json(name = "count") val count: Int?,
) : Serializable