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
    override val itemId: String,
    val questions: List<ProductInsightsAgentQuestion>,
    val viewTimespans: List<ProductInsightsAgentViewTimespan>,
    override val itemName: String? = null,
    override val variationId: String? = null,
    override val threadId: String? = null,
    override val sectionName: String? = null,
    override val analyticsTags: Map<String, String>? = null,
) : ProductInsightsAgentTrackingData {
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
    ) {
        /** The name of the item (product) the Product Insights Agent is displayed for, i.e. "Waterproof Jacket" */
        var itemName: String? = null
        /** The variation ID of the item (product) the Product Insights Agent is displayed for */
        var variationId: String? = null
        /** The ID of the Product Insights Agent conversation thread */
        var threadId: String? = null
        /** The index section, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection] */
        var sectionName: String? = null
        /** Additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision */
        var analyticsTags: Map<String, String>? = null

        /** Sets [itemName] */
        fun setItemName(itemName: String): Builder = apply { this.itemName = itemName }
        /** Sets [variationId] */
        fun setVariationId(variationId: String): Builder = apply { this.variationId = variationId }
        /** Sets [threadId] */
        fun setThreadId(threadId: String): Builder = apply { this.threadId = threadId }
        /** Sets [sectionName] */
        fun setSectionName(sectionName: String): Builder = apply { this.sectionName = sectionName }
        /** Sets [analyticsTags] */
        fun setAnalyticsTags(analyticsTags: Map<String, String>): Builder = apply { this.analyticsTags = analyticsTags }

        /** Builds the [ProductInsightsAgentViewsTrackingData] */
        fun build(): ProductInsightsAgentViewsTrackingData = ProductInsightsAgentViewsTrackingData(this)
    }
}
