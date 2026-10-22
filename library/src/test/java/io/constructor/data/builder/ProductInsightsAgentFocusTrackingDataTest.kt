package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductInsightsAgentFocusTrackingDataTest {
    private val itemId = "item-1"
    private val itemName = "Waterproof Jacket"
    private val variationId = "var-1"
    private val threadId = "thread-1"
    private val sectionName = "Search Suggestions"
    private val analyticsTags = mapOf("test" to "test1")

    @Test
    fun focusTrackingDataWithRequiredParams() {
        val request = ProductInsightsAgentFocusTrackingData.build(itemId)
        assertEquals(itemId, request.itemId)
        assertNull(request.itemName)
        assertNull(request.variationId)
        assertNull(request.threadId)
        assertNull(request.sectionName)
        assertNull(request.analyticsTags)
    }

    @Test
    fun focusTrackingDataUsingDsl() {
        val request = ProductInsightsAgentFocusTrackingData.build(itemId) {
            this.itemName = this@ProductInsightsAgentFocusTrackingDataTest.itemName
            this.variationId = this@ProductInsightsAgentFocusTrackingDataTest.variationId
            this.threadId = this@ProductInsightsAgentFocusTrackingDataTest.threadId
            this.sectionName = this@ProductInsightsAgentFocusTrackingDataTest.sectionName
            this.analyticsTags = this@ProductInsightsAgentFocusTrackingDataTest.analyticsTags
        }
        assertEquals(itemId, request.itemId)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun focusTrackingDataUsingBuilder() {
        val request = ProductInsightsAgentFocusTrackingData.Builder(itemId)
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
    fun focusTrackingDataUsingConstructor() {
        val request = ProductInsightsAgentFocusTrackingData(
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
