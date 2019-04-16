package io.constructor.data.model.search

import com.squareup.moshi.Json

data class SearchGroup(@Json(name = "children") val children: List<SearchGroup>?,
                       @Json(name = "parents") val parents: List<SearchGroup>?,
                       val count: Int,
                       @Json(name = "display_name") val displayName: String,
                       @Json(name = "group_id") val groupId: Long)