package io.constructor.data.builder

import io.constructor.data.model.common.TrackingItem

/**
 * Create a Product Insights Agent answer view tracking request object utilizing a builder. Sent when an answer is shown to the user.
 *
 * @property itemId the ID of the item (product) the Product Insights Agent is displayed for, i.e. "1234567-AB"
 * @property question the question text, i.e. "Is it waterproof?"
 * @property answerText the answer text shown to the user. May be empty
 * @property questionTopic the topic the question belongs to, i.e. "Durability"
 * @property qnaResultId the result ID of the answer, as returned by the Product Insights Agent
 * @property items the items (products) shown in the answer. Only the first 100 are sent
 * @property followUpQuestions the follow-up questions shown with the answer. Only the first 100 are sent
 * @property itemName the name of the item (product) the Product Insights Agent is displayed for, i.e. "Waterproof Jacket"
 * @property variationId the variation ID of the item (product) the Product Insights Agent is displayed for
 * @property threadId the ID of the Product Insights Agent conversation thread
 * @property sectionName the index section, i.e. "Products". Defaults to [io.constructor.core.ConstructorIoConfig.defaultItemSection]
 * @property analyticsTags additional analytics tags to pass. Merged with [io.constructor.core.ConstructorIoConfig.defaultAnalyticsTags], with request-level values overriding defaults on key collision
 */
class ProductInsightsAgentAnswerViewTrackingData(
    val itemId: String,
    val question: String,
    val answerText: String,
    val questionTopic: String? = null,
    val qnaResultId: String? = null,
    val items: List<TrackingItem>? = null,
    val followUpQuestions: List<String>? = null,
    val itemName: String? = null,
    val variationId: String? = null,
    val threadId: String? = null,
    val sectionName: String? = null,
    val analyticsTags: Map<String, String>? = null,
) {
    private constructor(builder: Builder) : this(
        builder.itemId,
        builder.question,
        builder.answerText,
        builder.questionTopic,
        builder.qnaResultId,
        builder.items,
        builder.followUpQuestions,
        builder.itemName,
        builder.variationId,
        builder.threadId,
        builder.sectionName,
        builder.analyticsTags,
    )

    companion object {
        /**
         * Builds a [ProductInsightsAgentAnswerViewTrackingData] from the required parameters, applying [block] to set optional parameters
         */
        inline fun build(
            itemId: String,
            question: String,
            answerText: String,
            block: Builder.() -> Unit = {}
        ) = Builder(itemId, question, answerText).apply(block).build()
    }

    /**
     * Builder for [ProductInsightsAgentAnswerViewTrackingData]
     */
    class Builder(
        val itemId: String,
        val question: String,
        val answerText: String
    ) {
        /** The topic the question belongs to, i.e. "Durability" */
        var questionTopic: String? = null
        /** The result ID of the answer, as returned by the Product Insights Agent */
        var qnaResultId: String? = null
        /** The items (products) shown in the answer. Only the first 100 are sent */
        var items: List<TrackingItem>? = null
        /** The follow-up questions shown with the answer. Only the first 100 are sent */
        var followUpQuestions: List<String>? = null
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
        /** Sets [qnaResultId] */
        fun setQnaResultId(qnaResultId: String): Builder = apply { this.qnaResultId = qnaResultId }
        /** Sets [items] */
        fun setItems(items: List<TrackingItem>): Builder = apply { this.items = items }
        /** Sets [followUpQuestions] */
        fun setFollowUpQuestions(followUpQuestions: List<String>): Builder = apply { this.followUpQuestions = followUpQuestions }
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

        /** Builds the [ProductInsightsAgentAnswerViewTrackingData] */
        fun build(): ProductInsightsAgentAnswerViewTrackingData = ProductInsightsAgentAnswerViewTrackingData(this)
    }
}
