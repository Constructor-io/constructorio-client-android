package io.constructor.data.builder

/**
 * Create a Product Insights Agent question submit tracking request object utilizing a builder. Sent when the user submits a question.
 *
 * @property itemId the ID of the item (product) the Product Insights Agent is displayed for, i.e. "1234567-AB"
 * @property question the question text, i.e. "Is it waterproof?"
 * @property questionTopic the topic the question belongs to, i.e. "Durability"
 * @property itemName the name of the item (product) the Product Insights Agent is displayed for, i.e. "Waterproof Jacket"
 * @property variationId the variation ID of the item (product) the Product Insights Agent is displayed for
 * @property threadId the ID of the Product Insights Agent conversation thread
 * @property sectionName the index section, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection]
 * @property analyticsTags additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision
 */
class ProductInsightsAgentQuestionSubmitTrackingData(
    val itemId: String,
    val question: String,
    val questionTopic: String? = null,
    val itemName: String? = null,
    val variationId: String? = null,
    val threadId: String? = null,
    val sectionName: String? = null,
    val analyticsTags: Map<String, String>? = null,
) {
    private constructor(builder: Builder) : this(
        builder.itemId,
        builder.question,
        builder.questionTopic,
        builder.itemName,
        builder.variationId,
        builder.threadId,
        builder.sectionName,
        builder.analyticsTags,
    )

    companion object {
        /**
         * Builds a [ProductInsightsAgentQuestionSubmitTrackingData] from the required parameters, applying [block] to set optional parameters
         */
        inline fun build(
            itemId: String,
            question: String,
            block: Builder.() -> Unit = {}
        ) = Builder(itemId, question).apply(block).build()
    }

    /**
     * Builder for [ProductInsightsAgentQuestionSubmitTrackingData]
     */
    class Builder(
        val itemId: String,
        val question: String
    ) {
        /** The topic the question belongs to, i.e. "Durability" */
        var questionTopic: String? = null
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

        /** Sets [questionTopic] */
        fun setQuestionTopic(questionTopic: String): Builder = apply { this.questionTopic = questionTopic }
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

        /** Builds the [ProductInsightsAgentQuestionSubmitTrackingData] */
        fun build(): ProductInsightsAgentQuestionSubmitTrackingData = ProductInsightsAgentQuestionSubmitTrackingData(this)
    }
}
