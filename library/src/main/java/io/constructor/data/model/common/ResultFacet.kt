package io.constructor.data.model.common

import com.squareup.moshi.Json
import java.io.Serializable

data class ResultFacet(
        @Json(name = "name") val name: String,
        @Json(name = "values") val values: List<String>?
) : Serializable
