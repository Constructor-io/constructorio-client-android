package io.constructor.data.model.tracking

import java.io.Serializable

/**
 * Models a span of time during which the Product Insights Agent was in view, used in Product Insights Agent tracking events.
 * Sent as ISO-8601 UTC timestamps, i.e. "2026-10-22T02:40:05.123Z"
 *
 * @property start the time the agent came into view, in epoch milliseconds, i.e. `System.currentTimeMillis()`
 * @property end the time the agent went out of view, in epoch milliseconds, i.e. `System.currentTimeMillis()`
 */
data class ProductInsightsAgentViewTimespan(
    val start: Long,
    val end: Long,
) : Serializable
