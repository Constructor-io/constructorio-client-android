package io.constructor.data.model.tracking

import com.squareup.moshi.Json

/**
 * The feedback a user gave on a Product Insights Agent answer, used in Product Insights Agent tracking events
 */
enum class ProductInsightsAgentFeedbackLabel {
    /** The user found the answer helpful */
    @Json(name = "thumbs_up")
    THUMBS_UP,

    /** The user did not find the answer helpful */
    @Json(name = "thumbs_down")
    THUMBS_DOWN,
}
