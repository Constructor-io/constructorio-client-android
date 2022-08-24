package io.constructor.data.model.purchase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.constructor.data.model.common.*;
import java.io.Serializable

/**
 * @suppress
 */
@JsonClass(generateAdapter = true)
data class PurchaseItem(
        @Json(name = "item_id") val itemId: String?,
        @Json(name = "variation_id") val variationId: String? = null
) : Serializable
