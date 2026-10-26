package io.constructor.data.model.tracking

import com.squareup.moshi.Json
import java.io.Serializable

/**
 * @suppress
 * Base request body holding the fields shared by every Product Insights Agent event. The common
 * client parameters (c, i, s, key, ui, us, _dt) are added as query parameters by RequestInterceptor.
 */
abstract class ProductInsightsAgentRequestBody(
    itemId: String,
    itemName: String?,
    variationId: String?,
    threadId: String?,
    analyticsTags: Map<String, String>?,
) : Serializable {
    @Json(name = "item_id") val itemId: String = itemId
    @Json(name = "item_name") val itemName: String? = itemName
    @Json(name = "variation_id") val variationId: String? = variationId
    @Json(name = "thread_id") val threadId: String? = threadId
    @Json(name = "analytics_tags") val analyticsTags: Map<String, String>? = analyticsTags
}
