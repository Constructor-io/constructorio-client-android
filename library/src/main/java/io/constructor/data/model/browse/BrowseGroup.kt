package io.constructor.data.model.browse

import com.squareup.moshi.Json

data class BrowseGroup(@Json(name = "children") val children: List<BrowseGroup>?,
                       @Json(name = "parents") val parents: List<BrowseGroup>?,
                       val count: Int?,
                       @Json(name = "display_name") val displayName: String,
                       @Json(name = "group_id") val groupId: String)