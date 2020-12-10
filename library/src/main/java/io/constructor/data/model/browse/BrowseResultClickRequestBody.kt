package io.constructor.data.model.browse

import com.squareup.moshi.Json
import io.constructor.data.model.common.*;
import java.io.Serializable

data class BrowseResultClickRequestBody(
        @Json(name = "filter_name") val filterName: String,
        @Json(name = "filter_value") val filterValue: String,
        @Json(name = "customer_id") val customerId: String?,
        @Json(name = "result_position_on_page") val resultPositionOnPage: Int?
) : Serializable