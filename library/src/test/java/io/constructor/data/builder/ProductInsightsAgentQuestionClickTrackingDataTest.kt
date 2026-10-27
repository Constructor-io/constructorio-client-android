package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductInsightsAgentQuestionClickTrackingDataTest {
    private val itemId = "item-1"
    private val question = "Is it waterproof?"
    private val questionTopic = "Durability"
    private val itemName = "Waterproof Jacket"
    private val variationId = "var-1"
    private val threadId = "thread-1"
    private val sectionName = "Search Suggestions"
    private val analyticsTags = mapOf("test" to "test1")

    @Test
    fun questionClickTrackingDataWithRequiredParams() {
        val request = ProductInsightsAgentQuestionClickTrackingData.build(itemId, question)
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
    fun questionClickTrackingDataUsingDsl() {
        val request = ProductInsightsAgentQuestionClickTrackingData.build(itemId, question) {
            this.questionTopic = this@ProductInsightsAgentQuestionClickTrackingDataTest.questionTopic
            this.itemName = this@ProductInsightsAgentQuestionClickTrackingDataTest.itemName
            this.variationId = this@ProductInsightsAgentQuestionClickTrackingDataTest.variationId
            this.threadId = this@ProductInsightsAgentQuestionClickTrackingDataTest.threadId
            this.sectionName = this@ProductInsightsAgentQuestionClickTrackingDataTest.sectionName
            this.analyticsTags = this@ProductInsightsAgentQuestionClickTrackingDataTest.analyticsTags
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
    fun questionClickTrackingDataUsingBuilder() {
        val request = ProductInsightsAgentQuestionClickTrackingData.Builder(itemId, question)
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
    fun questionClickTrackingDataUsingConstructor() {
        val request = ProductInsightsAgentQuestionClickTrackingData(
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
