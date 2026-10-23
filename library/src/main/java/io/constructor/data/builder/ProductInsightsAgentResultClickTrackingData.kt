package io.constructor.data.builder

/**
 * Create a Product Insights Agent result click tracking request object utilizing a builder. Sent when the user clicks an item (product) recommended in an answer.
 *
 * @property itemId the ID of the clicked item (product) recommended in the answer, i.e. "1234567-AB"
 * @property question the question text, i.e. "Is it waterproof?"
 * @property seedItemId the ID of the item (product) the Product Insights Agent is displayed for, i.e. the product detail page item
 * @property position the zero-based position of the clicked item within the answer
 * @property seedItemName the name of the item (product) the Product Insights Agent is displayed for
 * @property seedVariationId the variation ID of the item (product) the Product Insights Agent is displayed for
 * @property qnaResultId the result ID of the answer, as returned by the Product Insights Agent
 * @property itemName the name of the clicked item (product) recommended in the answer
 * @property variationId the variation ID of the clicked item (product) recommended in the answer
 * @property threadId the ID of the Product Insights Agent conversation thread
 * @property sectionName the index section, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection]
 * @property analyticsTags additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision
 */
class ProductInsightsAgentResultClickTrackingData(
    val itemId: String,
    val question: String,
    val seedItemId: String,
    val position: Int? = null,
    val seedItemName: String? = null,
    val seedVariationId: String? = null,
    val qnaResultId: String? = null,
    val itemName: String? = null,
    val variationId: String? = null,
    val threadId: String? = null,
    val sectionName: String? = null,
    val analyticsTags: Map<String, String>? = null,
) {
    private constructor(builder: Builder) : this(
        builder.itemId,
        builder.question,
        builder.seedItemId,
        builder.position,
        builder.seedItemName,
        builder.seedVariationId,
        builder.qnaResultId,
        builder.itemName,
        builder.variationId,
        builder.threadId,
        builder.sectionName,
        builder.analyticsTags,
    )

    companion object {
        /**
         * Builds a [ProductInsightsAgentResultClickTrackingData] from the required parameters, applying [block] to set optional parameters
         */
        inline fun build(
            itemId: String,
            question: String,
            seedItemId: String,
            block: Builder.() -> Unit = {}
        ) = Builder(itemId, question, seedItemId).apply(block).build()
    }

    /**
     * Builder for [ProductInsightsAgentResultClickTrackingData]
     */
    class Builder(
        val itemId: String,
        val question: String,
        val seedItemId: String
    ) {
        /** The zero-based position of the clicked item within the answer */
        var position: Int? = null
        /** The name of the item (product) the Product Insights Agent is displayed for */
        var seedItemName: String? = null
        /** The variation ID of the item (product) the Product Insights Agent is displayed for */
        var seedVariationId: String? = null
        /** The result ID of the answer, as returned by the Product Insights Agent */
        var qnaResultId: String? = null
        /** The name of the clicked item (product) recommended in the answer */
        var itemName: String? = null
        /** The variation ID of the clicked item (product) recommended in the answer */
        var variationId: String? = null
        /** The ID of the Product Insights Agent conversation thread */
        var threadId: String? = null
        /** The index section, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection] */
        var sectionName: String? = null
        /** Additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision */
        var analyticsTags: Map<String, String>? = null

        /** Sets [position] */
        fun setPosition(position: Int): Builder = apply { this.position = position }
        /** Sets [seedItemName] */
        fun setSeedItemName(seedItemName: String): Builder = apply { this.seedItemName = seedItemName }
        /** Sets [seedVariationId] */
        fun setSeedVariationId(seedVariationId: String): Builder = apply { this.seedVariationId = seedVariationId }
        /** Sets [qnaResultId] */
        fun setQnaResultId(qnaResultId: String): Builder = apply { this.qnaResultId = qnaResultId }
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

        /** Builds the [ProductInsightsAgentResultClickTrackingData] */
        fun build(): ProductInsightsAgentResultClickTrackingData = ProductInsightsAgentResultClickTrackingData(this)
    }
}
