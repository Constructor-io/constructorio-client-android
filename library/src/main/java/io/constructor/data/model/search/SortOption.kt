package io.constructor.data.model.search

import com.squareup.moshi.Json
import java.io.Serializable


data class SortOption(@Json(name = "display_name") val displayName: String,
                      @Json(name = "sort_by") val sortBy: String,
                      @Json(name = "sort_order") val sortOrder: String,
                      val status: String) : Serializable
