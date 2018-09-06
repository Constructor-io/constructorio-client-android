package io.constructor.data.model

import com.squareup.moshi.Json
import io.constructor.data.model.search.Path
import java.io.Serializable


data class ResultGroup(@Json(name="display_name") var displayName: String?, @Json(name="group_id") var groupId: String, @Json(name="path") var path: String? = null, @Json(name="path_list") var pathList: List<Path>? = null) : Serializable