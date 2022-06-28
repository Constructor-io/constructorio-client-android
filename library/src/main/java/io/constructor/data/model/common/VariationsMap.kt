package io.constructor.data.model.common

import com.squareup.moshi.Json
import java.io.Serializable

/**
 * Models variations map object available for modifying the request
 */
data class VariationsMap(
 @Json(name = "dtype") val dtype: String,
 @Json(name = "values") val values: Map<String, Map<String, String>>,
 @Json(name = "group_by") val groupBy: List<Map<String, String>>? = null,
) : Serializable
