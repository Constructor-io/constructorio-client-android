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
    itemId: String,
    val question: String,
    val questionTopic: String? = null,
    itemName: String? = null,
    variationId: String? = null,
    threadId: String? = null,
    sectionName: String? = null,
    analyticsTags: Map<String, String>? = null,
) : ProductInsightsAgentTrackingData(itemId, itemName, variationId, threadId, sectionName, analyticsTags) {
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
    ) : ProductInsightsAgentTrackingData.Builder<Builder>() {
        /** The topic the question belongs to, i.e. "Durability" */
        var questionTopic: String? = null

        /** Sets [questionTopic] */
        fun setQuestionTopic(questionTopic: String): Builder = apply { this.questionTopic = questionTopic }

        /** Builds the [ProductInsightsAgentQuestionSubmitTrackingData] */
        fun build(): ProductInsightsAgentQuestionSubmitTrackingData = ProductInsightsAgentQuestionSubmitTrackingData(this)
    }
}
