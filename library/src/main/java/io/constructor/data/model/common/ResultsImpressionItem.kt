package io.constructor.data.model.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class ResultsImpressionItem(
        @Json(name = "item_id") val itemId: String,
        @Json(name = "item_name") val itemName: String,
        @Json(name = "variation_id") val variationId: String? = null,
        @Json(name = "sl_campaign_id") val slCampaignId: String? = null,
        @Json(name = "sl_campaign_owner") val slCampaignOwner: String? = null
) : Serializable
