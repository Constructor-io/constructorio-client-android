package io.constructor.data.builder

/**
 * Create a Product Insights Agent out of view tracking request object utilizing a builder. Sent when the Product Insights Agent scrolls out of view.
 *
 * @property itemId the ID of the item (product) the Product Insights Agent is displayed for, i.e. "1234567-AB"
 * @property itemName the name of the item (product) the Product Insights Agent is displayed for, i.e. "Waterproof Jacket"
 * @property variationId the variation ID of the item (product) the Product Insights Agent is displayed for
 * @property threadId the ID of the Product Insights Agent conversation thread
 * @property sectionName the index section, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection]
 * @property analyticsTags additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision
 */
class ProductInsightsAgentOutOfViewTrackingData(
    itemId: String,
    itemName: String? = null,
    variationId: String? = null,
    threadId: String? = null,
    sectionName: String? = null,
    analyticsTags: Map<String, String>? = null,
) : ProductInsightsAgentTrackingData(itemId, itemName, variationId, threadId, sectionName, analyticsTags) {
    private constructor(builder: Builder) : this(
        builder.itemId,
        builder.itemName,
        builder.variationId,
        builder.threadId,
        builder.sectionName,
        builder.analyticsTags,
    )

    companion object {
        /**
         * Builds a [ProductInsightsAgentOutOfViewTrackingData] from the required parameters, applying [block] to set optional parameters
         */
        inline fun build(
            itemId: String,
            block: Builder.() -> Unit = {}
        ) = Builder(itemId).apply(block).build()
    }

    /**
     * Builder for [ProductInsightsAgentOutOfViewTrackingData]
     */
    class Builder(
        val itemId: String
    ) : ProductInsightsAgentTrackingData.Builder<Builder>() {
        /** Builds the [ProductInsightsAgentOutOfViewTrackingData] */
        fun build(): ProductInsightsAgentOutOfViewTrackingData = ProductInsightsAgentOutOfViewTrackingData(this)
    }
}
