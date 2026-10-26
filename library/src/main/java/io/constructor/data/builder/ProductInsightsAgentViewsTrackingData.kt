package io.constructor.data.builder

import io.constructor.data.model.tracking.ProductInsightsAgentQuestion
import io.constructor.data.model.tracking.ProductInsightsAgentViewTimespan

/**
 * Create a Product Insights Agent views tracking request object utilizing a builder. Sent when the Product Insights Agent has been viewed, reporting each span of time it was in view.
 *
 * @property itemId the ID of the item (product) the Product Insights Agent is displayed for, i.e. "1234567-AB"
 * @property questions the questions shown to the user
 * @property viewTimespans the spans of time during which the Product Insights Agent was in view
 * @property itemName the name of the item (product) the Product Insights Agent is displayed for, i.e. "Waterproof Jacket"
 * @property variationId the variation ID of the item (product) the Product Insights Agent is displayed for
 * @property threadId the ID of the Product Insights Agent conversation thread
 * @property sectionName the index section, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection]
 * @property analyticsTags additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision
 */
class ProductInsightsAgentViewsTrackingData(
    itemId: String,
    val questions: List<ProductInsightsAgentQuestion>,
    val viewTimespans: List<ProductInsightsAgentViewTimespan>,
    itemName: String? = null,
    variationId: String? = null,
    threadId: String? = null,
    sectionName: String? = null,
    analyticsTags: Map<String, String>? = null,
) : ProductInsightsAgentTrackingData(itemId, itemName, variationId, threadId, sectionName, analyticsTags) {
    private constructor(builder: Builder) : this(
        builder.itemId,
        builder.questions,
        builder.viewTimespans,
        builder.itemName,
        builder.variationId,
        builder.threadId,
        builder.sectionName,
        builder.analyticsTags,
    )

    companion object {
        /**
         * Builds a [ProductInsightsAgentViewsTrackingData] from the required parameters, applying [block] to set optional parameters
         */
        inline fun build(
            itemId: String,
            questions: List<ProductInsightsAgentQuestion>,
            viewTimespans: List<ProductInsightsAgentViewTimespan>,
            block: Builder.() -> Unit = {}
        ) = Builder(itemId, questions, viewTimespans).apply(block).build()
    }

    /**
     * Builder for [ProductInsightsAgentViewsTrackingData]
     */
    class Builder(
        val itemId: String,
        val questions: List<ProductInsightsAgentQuestion>,
        val viewTimespans: List<ProductInsightsAgentViewTimespan>
    ) : ProductInsightsAgentTrackingData.Builder<Builder>() {
        /** Builds the [ProductInsightsAgentViewsTrackingData] */
        fun build(): ProductInsightsAgentViewsTrackingData = ProductInsightsAgentViewsTrackingData(this)
    }
}
