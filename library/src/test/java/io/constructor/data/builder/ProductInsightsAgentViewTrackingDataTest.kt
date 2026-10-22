package io.constructor.data.builder

import io.constructor.data.model.tracking.ProductInsightsAgentQuestion
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductInsightsAgentViewTrackingDataTest {
    private val itemId = "item-1"
    private val questions = listOf(ProductInsightsAgentQuestion("Is it waterproof?", "Durability"))
    private val itemName = "Waterproof Jacket"
    private val variationId = "var-1"
    private val threadId = "thread-1"
    private val sectionName = "Search Suggestions"
    private val analyticsTags = mapOf("test" to "test1")

    @Test
    fun viewTrackingDataWithRequiredParams() {
        val request = ProductInsightsAgentViewTrackingData.build(itemId, questions)
        assertEquals(itemId, request.itemId)
        assertEquals(questions, request.questions)
        assertNull(request.itemName)
        assertNull(request.variationId)
        assertNull(request.threadId)
        assertNull(request.sectionName)
        assertNull(request.analyticsTags)
    }

    @Test
    fun viewTrackingDataUsingDsl() {
        val request = ProductInsightsAgentViewTrackingData.build(itemId, questions) {
            this.itemName = this@ProductInsightsAgentViewTrackingDataTest.itemName
            this.variationId = this@ProductInsightsAgentViewTrackingDataTest.variationId
            this.threadId = this@ProductInsightsAgentViewTrackingDataTest.threadId
            this.sectionName = this@ProductInsightsAgentViewTrackingDataTest.sectionName
            this.analyticsTags = this@ProductInsightsAgentViewTrackingDataTest.analyticsTags
        }
        assertEquals(itemId, request.itemId)
        assertEquals(questions, request.questions)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun viewTrackingDataUsingBuilder() {
        val request = ProductInsightsAgentViewTrackingData.Builder(itemId, questions)
            .setItemName(itemName)
            .setVariationId(variationId)
            .setThreadId(threadId)
            .setSectionName(sectionName)
            .setAnalyticsTags(analyticsTags)
            .build()
        assertEquals(itemId, request.itemId)
        assertEquals(questions, request.questions)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun viewTrackingDataUsingConstructor() {
        val request = ProductInsightsAgentViewTrackingData(
            itemId = itemId,
            questions = questions,
            itemName = itemName,
            variationId = variationId,
            threadId = threadId,
            sectionName = sectionName,
            analyticsTags = analyticsTags,
        )
        assertEquals(itemId, request.itemId)
        assertEquals(questions, request.questions)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }
}
