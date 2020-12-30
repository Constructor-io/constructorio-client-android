package io.constructor.data.model.browse

import com.squareup.moshi.Json
import io.constructor.data.model.common.*;
import java.io.Serializable

data class BrowseResultClickRequestBody(
        @Json(name = "filter_name") val filterName: String,
        @Json(name = "filter_value") val filterValue: String,
        @Json(name = "item_id") val item_id: String,
        @Json(name = "result_position_on_page") val resultPositionOnPage: Int,
        @Json(name = "c") val c: String,
        @Json(name = "i") val i: String,
        @Json(name = "s") val s: Int,
        @Json(name = "key") val key: String,
        @Json(name = "ui") val ui: String?,
        @Json(name = "us") val us: List<String?>,
        @Json(name= "beacon") val beacon: Boolean?,
        @Json(name= "autocomplete_section") val autocomplete_section: String?,
        @Json(name= "_dt") val _dt: Long?
) : Serializable