package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Models variations map object available for modifying the request
 */
@JsonClass(generateAdapter = true)
data class VariationsMap constructor(
    @Json(name = "dtype") val dtype: String,
    @Json(name = "values") val values: Map<String, Map<String, String>>,
    @Json(name = "group_by") val groupBy: List<Map<String, String>>? = null,
) : Serializable
