package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductInsightsAgentOutOfViewTrackingDataTest {
    private val itemId = "item-1"
    private val itemName = "Waterproof Jacket"
    private val variationId = "var-1"
    private val threadId = "thread-1"
    private val sectionName = "Search Suggestions"
    private val analyticsTags = mapOf("test" to "test1")

    @Test
    fun outOfViewTrackingDataWithRequiredParams() {
        val request = ProductInsightsAgentOutOfViewTrackingData.build(itemId)
        assertEquals(itemId, request.itemId)
        assertNull(request.itemName)
        assertNull(request.variationId)
        assertNull(request.threadId)
        assertNull(request.sectionName)
        assertNull(request.analyticsTags)
    }

    @Test
    fun outOfViewTrackingDataUsingDsl() {
        val request = ProductInsightsAgentOutOfViewTrackingData.build(itemId) {
            this.itemName = this@ProductInsightsAgentOutOfViewTrackingDataTest.itemName
            this.variationId = this@ProductInsightsAgentOutOfViewTrackingDataTest.variationId
            this.threadId = this@ProductInsightsAgentOutOfViewTrackingDataTest.threadId
            this.sectionName = this@ProductInsightsAgentOutOfViewTrackingDataTest.sectionName
            this.analyticsTags = this@ProductInsightsAgentOutOfViewTrackingDataTest.analyticsTags
        }
        assertEquals(itemId, request.itemId)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun outOfViewTrackingDataUsingBuilder() {
        val request = ProductInsightsAgentOutOfViewTrackingData.Builder(itemId)
            .setItemName(itemName)
            .setVariationId(variationId)
            .setThreadId(threadId)
            .setSectionName(sectionName)
            .setAnalyticsTags(analyticsTags)
            .build()
        assertEquals(itemId, request.itemId)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun outOfViewTrackingDataUsingConstructor() {
        val request = ProductInsightsAgentOutOfViewTrackingData(
            itemId = itemId,
            itemName = itemName,
            variationId = variationId,
            threadId = threadId,
            sectionName = sectionName,
            analyticsTags = analyticsTags,
        )
        assertEquals(itemId, request.itemId)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }
}
