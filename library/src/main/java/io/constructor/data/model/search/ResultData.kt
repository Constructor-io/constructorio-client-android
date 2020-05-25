package io.constructor.data.model.search

import com.squareup.moshi.Json
import io.constructor.data.model.Group
import java.io.Serializable

data class ResultData(val description: String?,
                      val id: String,
                      @Json(name = "image_url") val imageUrl: String?,
                      val url: String?,
                      val facets: List<ResultFacet>?,
                      val groups: List<Group>?,
                      var metadata: Map<String, Any?>) : Serializable