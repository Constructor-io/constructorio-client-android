package io.constructor.data.model.search

import com.squareup.moshi.Json
import io.constructor.data.model.ProductData

data class Path(@Json(name = "display_name") val displayName: String, val id: Long)