package io.constructor.data.model.purchase

import com.squareup.moshi.Json
import io.constructor.data.model.common.*;
import java.io.Serializable

data class PurchaseRequestBody(
        @Json(name = "items") val items: List<PurchaseItem>?,
        @Json(name = "revenue") val revenue: Double?,
        @Json(name = "order_id") val orderId: String?,
        @Json(name = "c") val c: String?,
        @Json(name = "i") val i: String?,
        @Json(name = "s") val s: Int?,
        @Json(name = "ui") val ui: String?,
        @Json(name = "us") val us: List<String?>,
        @Json(name = "key") val key: String?,
        @Json(name= "beacon") val beacon: Boolean?,
        @Json(name= "autocomplete_section") val autocomplete_section: String?,
        @Json(name= "_dt") val _dt: Int?
) : Serializable