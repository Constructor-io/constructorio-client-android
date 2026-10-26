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
    itemId: String,
    val feedbackLabel: ProductInsightsAgentFeedbackLabel,
    val qnaResultId: String? = null,
    itemName: String? = null,
    variationId: String? = null,
    threadId: String? = null,
    sectionName: String? = null,
    analyticsTags: Map<String, String>? = null,
) : ProductInsightsAgentTrackingData(itemId, itemName, variationId, threadId, sectionName, analyticsTags) {
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
    ) : ProductInsightsAgentTrackingData.Builder<Builder>() {
        /** The result ID of the answer, as returned by the Product Insights Agent */
        var qnaResultId: String? = null

        /** Sets [qnaResultId] */
        fun setQnaResultId(qnaResultId: String): Builder = apply { this.qnaResultId = qnaResultId }

        /** Builds the [ProductInsightsAgentAnswerFeedbackTrackingData] */
        fun build(): ProductInsightsAgentAnswerFeedbackTrackingData = ProductInsightsAgentAnswerFeedbackTrackingData(this)
    }
}
