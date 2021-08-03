package io.constructor.data.model.common

import com.squareup.moshi.Json
import java.io.Serializable

/**
 * Models groups on an item
 */
data class ResultGroup(
        @Json(name="display_name") var displayName: String?,
        @Json(name="group_id") var groupId: String,
        @Json(name="path") var path: String? = null
) : Serializable