package io.constructor.data.model.search

import com.squareup.moshi.Json

data class GroupData(@Json(name = "children") val data: List<String>, val count: Int, @Json(name = "display_name") val displayName: String,
                     @Json(name = "group_id") val groupId: Long)