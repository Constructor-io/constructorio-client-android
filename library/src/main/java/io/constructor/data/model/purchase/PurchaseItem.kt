package io.constructor.data.model.purchase

import com.squareup.moshi.Json
import io.constructor.data.model.common.*;
import java.io.Serializable

data class PurchaseItem(
        @Json(name = "item_id") val itemId: String?,
        @Json(name = "variation_id") val variation_id: String? = null
) : Serializable