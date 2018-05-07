package io.constructor.data.model

import com.squareup.moshi.Json
import java.io.Serializable


data class Group(@Json(name="display_name") var displayName: String?, @Json(name="group_id") var groupId: String, @Json(name="path") var path: String? = null) : Serializable