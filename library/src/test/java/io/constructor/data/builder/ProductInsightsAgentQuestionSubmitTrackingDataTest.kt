package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductInsightsAgentQuestionSubmitTrackingDataTest {
    private val itemId = "item-1"
    private val question = "Is it waterproof?"
    private val questionTopic = "Durability"
    private val itemName = "Waterproof Jacket"
    private val variationId = "var-1"
    private val threadId = "thread-1"
    private val sectionName = "Search Suggestions"
    private val analyticsTags = mapOf("test" to "test1")

    @Test
    fun questionSubmitTrackingDataWithRequiredParams() {
        val request = ProductInsightsAgentQuestionSubmitTrackingData.build(itemId, question)
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertNull(request.questionTopic)
        assertNull(request.itemName)
        assertNull(request.variationId)
        assertNull(request.threadId)
        assertNull(request.sectionName)
        assertNull(request.analyticsTags)
    }

    @Test
    fun questionSubmitTrackingDataUsingDsl() {
        val request = ProductInsightsAgentQuestionSubmitTrackingData.build(itemId, question) {
            this.questionTopic = this@ProductInsightsAgentQuestionSubmitTrackingDataTest.questionTopic
            this.itemName = this@ProductInsightsAgentQuestionSubmitTrackingDataTest.itemName
            this.variationId = this@ProductInsightsAgentQuestionSubmitTrackingDataTest.variationId
            this.threadId = this@ProductInsightsAgentQuestionSubmitTrackingDataTest.threadId
            this.sectionName = this@ProductInsightsAgentQuestionSubmitTrackingDataTest.sectionName
            this.analyticsTags = this@ProductInsightsAgentQuestionSubmitTrackingDataTest.analyticsTags
        }
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertEquals(questionTopic, request.questionTopic)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun questionSubmitTrackingDataUsingBuilder() {
        val request = ProductInsightsAgentQuestionSubmitTrackingData.Builder(itemId, question)
            .setQuestionTopic(questionTopic)
            .setItemName(itemName)
            .setVariationId(variationId)
            .setThreadId(threadId)
            .setSectionName(sectionName)
            .setAnalyticsTags(analyticsTags)
            .build()
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertEquals(questionTopic, request.questionTopic)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun questionSubmitTrackingDataUsingConstructor() {
        val request = ProductInsightsAgentQuestionSubmitTrackingData(
            itemId = itemId,
            question = question,
            questionTopic = questionTopic,
            itemName = itemName,
            variationId = variationId,
            threadId = threadId,
            sectionName = sectionName,
            analyticsTags = analyticsTags,
        )
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertEquals(questionTopic, request.questionTopic)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }
}
