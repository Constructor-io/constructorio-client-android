package io.constructor.data.builder

import io.constructor.data.model.tracking.ProductInsightsAgentFeedbackLabel

/**
 * Create a Product Insights Agent answer feedback tracking request object utilizing a builder. Sent when the user gives feedback on an answer.
 *
 * @property itemId the ID of the item (product) the Product Insights Agent is displayed for, i.e. "1234567-AB"
 * @property feedbackLabel the feedback the user gave on the answer
 * @property qnaResultId the result ID of the answer, as returned by the Product Insights Agent
 * @property itemName the name of the item (product) the Product Insights Agent is displayed for, i.e. "Waterproof Jacket"
 * @property variationId the variation ID of the item (product) the Product Insights Agent is displayed for
 * @property threadId the ID of the Product Insights Agent conversation thread
 * @property sectionName the index section, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection]
 * @property analyticsTags additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision
 */
class ProductInsightsAgentAnswerFeedbackTrackingData(
    override val itemId: String,
    val feedbackLabel: ProductInsightsAgentFeedbackLabel,
    val qnaResultId: String? = null,
    override val itemName: String? = null,
    override val variationId: String? = null,
    override val threadId: String? = null,
    override val sectionName: String? = null,
    override val analyticsTags: Map<String, String>? = null,
) : ProductInsightsAgentTrackingData {
    private constructor(builder: Builder) : this(
        builder.itemId,
        builder.feedbackLabel,
        builder.qnaResultId,
        builder.itemName,
        builder.variationId,
        builder.threadId,
        builder.sectionName,
        builder.analyticsTags,
    )

    companion object {
        /**
         * Builds a [ProductInsightsAgentAnswerFeedbackTrackingData] from the required parameters, applying [block] to set optional parameters
         */
        inline fun build(
            itemId: String,
            feedbackLabel: ProductInsightsAgentFeedbackLabel,
            block: Builder.() -> Unit = {}
        ) = Builder(itemId, feedbackLabel).apply(block).build()
    }

    /**
     * Builder for [ProductInsightsAgentAnswerFeedbackTrackingData]
     */
    class Builder(
        val itemId: String,
        val feedbackLabel: ProductInsightsAgentFeedbackLabel
    ) {
        /** The result ID of the answer, as returned by the Product Insights Agent */
        var qnaResultId: String? = null
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

        /** Builds the [ProductInsightsAgentAnswerFeedbackTrackingData] */
        fun build(): ProductInsightsAgentAnswerFeedbackTrackingData = ProductInsightsAgentAnswerFeedbackTrackingData(this)
    }
}
