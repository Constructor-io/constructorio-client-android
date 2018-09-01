package io.constructor.data.model.search

import com.squareup.moshi.Json
import io.constructor.data.model.Group
import io.constructor.data.model.ProductData

data class Result(val description: String?, val id: String,
                  @Json(name = "image_url") val imageUrl: String?,
                  val price: Double?,
                  val quantity: String?,
                  val url: String?,
                  @Json(name = "facets") val resultFacets: List<ResultFacet>?, val groups: List<Group>?)