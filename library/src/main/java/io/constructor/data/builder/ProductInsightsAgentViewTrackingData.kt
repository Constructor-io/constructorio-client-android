package io.constructor.data.builder

import io.constructor.data.model.tracking.ProductInsightsAgentQuestion

/**
 * Create a Product Insights Agent view tracking request object utilizing a builder. Sent when the Product Insights Agent comes into view.
 *
 * @property itemId the ID of the item (product) the Product Insights Agent is displayed for, i.e. "1234567-AB"
 * @property questions the questions shown to the user
 * @property itemName the name of the item (product) the Product Insights Agent is displayed for, i.e. "Waterproof Jacket"
 * @property variationId the variation ID of the item (product) the Product Insights Agent is displayed for
 * @property threadId the ID of the Product Insights Agent conversation thread
 * @property sectionName the index section, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection]
 * @property analyticsTags additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision
 */
class ProductInsightsAgentViewTrackingData(
    val itemId: String,
    val questions: List<ProductInsightsAgentQuestion>,
    val itemName: String? = null,
    val variationId: String? = null,
    val threadId: String? = null,
    val sectionName: String? = null,
    val analyticsTags: Map<String, String>? = null,
) {
    private constructor(builder: Builder) : this(
        builder.itemId,
        builder.questions,
        builder.itemName,
        builder.variationId,
        builder.threadId,
        builder.sectionName,
        builder.analyticsTags,
    )

    companion object {
        /**
         * Builds a [ProductInsightsAgentViewTrackingData] from the required parameters, applying [block] to set optional parameters
         */
        inline fun build(
            itemId: String,
            questions: List<ProductInsightsAgentQuestion>,
            block: Builder.() -> Unit = {}
        ) = Builder(itemId, questions).apply(block).build()
    }

    /**
     * Builder for [ProductInsightsAgentViewTrackingData]
     */
    class Builder(
        val itemId: String,
        val questions: List<ProductInsightsAgentQuestion>
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

        /** Builds the [ProductInsightsAgentViewTrackingData] */
        fun build(): ProductInsightsAgentViewTrackingData = ProductInsightsAgentViewTrackingData(this)
    }
}
