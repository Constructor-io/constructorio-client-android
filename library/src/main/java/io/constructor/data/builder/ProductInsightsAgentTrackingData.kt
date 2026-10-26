package io.constructor.data.builder

/**
 * Base class for the Product Insights Agent tracking request objects, holding the parameters shared by every event
 *
 * @property itemId the ID of the item (product) the event is for, i.e. "1234567-AB"
 * @property itemName the name of the item (product) the Product Insights Agent is displayed for, i.e. "Waterproof Jacket"
 * @property variationId the variation ID of the item (product) the Product Insights Agent is displayed for
 * @property threadId the ID of the Product Insights Agent conversation thread
 * @property sectionName the index section, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection]
 * @property analyticsTags additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision
 */
abstract class ProductInsightsAgentTrackingData(
    val itemId: String,
    val itemName: String?,
    val variationId: String?,
    val threadId: String?,
    val sectionName: String?,
    val analyticsTags: Map<String, String>?,
) {
    /**
     * Base builder holding the optional parameters shared by every Product Insights Agent event
     */
    abstract class Builder<B : Builder<B>> {
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
        @Suppress("UNCHECKED_CAST")
        fun setItemName(itemName: String): B = apply { this.itemName = itemName } as B
        /** Sets [variationId] */
        @Suppress("UNCHECKED_CAST")
        fun setVariationId(variationId: String): B = apply { this.variationId = variationId } as B
        /** Sets [threadId] */
        @Suppress("UNCHECKED_CAST")
        fun setThreadId(threadId: String): B = apply { this.threadId = threadId } as B
        /** Sets [sectionName] */
        @Suppress("UNCHECKED_CAST")
        fun setSectionName(sectionName: String): B = apply { this.sectionName = sectionName } as B
        /** Sets [analyticsTags] */
        @Suppress("UNCHECKED_CAST")
        fun setAnalyticsTags(analyticsTags: Map<String, String>): B = apply { this.analyticsTags = analyticsTags } as B
    }
}
