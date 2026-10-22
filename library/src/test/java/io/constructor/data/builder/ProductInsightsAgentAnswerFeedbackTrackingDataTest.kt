package io.constructor.data.builder

import io.constructor.data.model.tracking.ProductInsightsAgentFeedbackLabel
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductInsightsAgentAnswerFeedbackTrackingDataTest {
    private val itemId = "item-1"
    private val feedbackLabel = ProductInsightsAgentFeedbackLabel.THUMBS_UP
    private val qnaResultId = "qna-1"
    private val itemName = "Waterproof Jacket"
    private val variationId = "var-1"
    private val threadId = "thread-1"
    private val sectionName = "Search Suggestions"
    private val analyticsTags = mapOf("test" to "test1")

    @Test
    fun answerFeedbackTrackingDataWithRequiredParams() {
        val request = ProductInsightsAgentAnswerFeedbackTrackingData.build(itemId, feedbackLabel)
        assertEquals(itemId, request.itemId)
        assertEquals(feedbackLabel, request.feedbackLabel)
        assertNull(request.qnaResultId)
        assertNull(request.itemName)
        assertNull(request.variationId)
        assertNull(request.threadId)
        assertNull(request.sectionName)
        assertNull(request.analyticsTags)
    }

    @Test
    fun answerFeedbackTrackingDataUsingDsl() {
        val request = ProductInsightsAgentAnswerFeedbackTrackingData.build(itemId, feedbackLabel) {
            this.qnaResultId = this@ProductInsightsAgentAnswerFeedbackTrackingDataTest.qnaResultId
            this.itemName = this@ProductInsightsAgentAnswerFeedbackTrackingDataTest.itemName
            this.variationId = this@ProductInsightsAgentAnswerFeedbackTrackingDataTest.variationId
            this.threadId = this@ProductInsightsAgentAnswerFeedbackTrackingDataTest.threadId
            this.sectionName = this@ProductInsightsAgentAnswerFeedbackTrackingDataTest.sectionName
            this.analyticsTags = this@ProductInsightsAgentAnswerFeedbackTrackingDataTest.analyticsTags
        }
        assertEquals(itemId, request.itemId)
        assertEquals(feedbackLabel, request.feedbackLabel)
        assertEquals(qnaResultId, request.qnaResultId)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun answerFeedbackTrackingDataUsingBuilder() {
        val request = ProductInsightsAgentAnswerFeedbackTrackingData.Builder(itemId, feedbackLabel)
            .setQnaResultId(qnaResultId)
            .setItemName(itemName)
            .setVariationId(variationId)
            .setThreadId(threadId)
            .setSectionName(sectionName)
            .setAnalyticsTags(analyticsTags)
            .build()
        assertEquals(itemId, request.itemId)
        assertEquals(feedbackLabel, request.feedbackLabel)
        assertEquals(qnaResultId, request.qnaResultId)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun answerFeedbackTrackingDataUsingConstructor() {
        val request = ProductInsightsAgentAnswerFeedbackTrackingData(
            itemId = itemId,
            feedbackLabel = feedbackLabel,
            qnaResultId = qnaResultId,
            itemName = itemName,
            variationId = variationId,
            threadId = threadId,
            sectionName = sectionName,
            analyticsTags = analyticsTags,
        )
        assertEquals(itemId, request.itemId)
        assertEquals(feedbackLabel, request.feedbackLabel)
        assertEquals(qnaResultId, request.qnaResultId)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }
}
