package io.constructor.data.builder

import io.constructor.data.model.tracking.ProductInsightsAgentQuestion
import io.constructor.data.model.tracking.ProductInsightsAgentViewTimespan
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductInsightsAgentViewsTrackingDataTest {
    private val itemId = "item-1"
    private val questions = listOf(ProductInsightsAgentQuestion("Is it waterproof?", "Durability"))
    private val viewTimespans = listOf(ProductInsightsAgentViewTimespan(1792550405123, 1792550410456))
    private val itemName = "Waterproof Jacket"
    private val variationId = "var-1"
    private val threadId = "thread-1"
    private val sectionName = "Search Suggestions"
    private val analyticsTags = mapOf("test" to "test1")

    @Test
    fun viewsTrackingDataWithRequiredParams() {
        val request = ProductInsightsAgentViewsTrackingData.build(itemId, questions, viewTimespans)
        assertEquals(itemId, request.itemId)
        assertEquals(questions, request.questions)
        assertEquals(viewTimespans, request.viewTimespans)
        assertNull(request.itemName)
        assertNull(request.variationId)
        assertNull(request.threadId)
        assertNull(request.sectionName)
        assertNull(request.analyticsTags)
    }

    @Test
    fun viewsTrackingDataUsingDsl() {
        val request = ProductInsightsAgentViewsTrackingData.build(itemId, questions, viewTimespans) {
            this.itemName = this@ProductInsightsAgentViewsTrackingDataTest.itemName
            this.variationId = this@ProductInsightsAgentViewsTrackingDataTest.variationId
            this.threadId = this@ProductInsightsAgentViewsTrackingDataTest.threadId
            this.sectionName = this@ProductInsightsAgentViewsTrackingDataTest.sectionName
            this.analyticsTags = this@ProductInsightsAgentViewsTrackingDataTest.analyticsTags
        }
        assertEquals(itemId, request.itemId)
        assertEquals(questions, request.questions)
        assertEquals(viewTimespans, request.viewTimespans)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun viewsTrackingDataUsingBuilder() {
        val request = ProductInsightsAgentViewsTrackingData.Builder(itemId, questions, viewTimespans)
            .setItemName(itemName)
            .setVariationId(variationId)
            .setThreadId(threadId)
            .setSectionName(sectionName)
            .setAnalyticsTags(analyticsTags)
            .build()
        assertEquals(itemId, request.itemId)
        assertEquals(questions, request.questions)
        assertEquals(viewTimespans, request.viewTimespans)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun viewsTrackingDataUsingConstructor() {
        val request = ProductInsightsAgentViewsTrackingData(
            itemId = itemId,
            questions = questions,
            viewTimespans = viewTimespans,
            itemName = itemName,
            variationId = variationId,
            threadId = threadId,
            sectionName = sectionName,
            analyticsTags = analyticsTags,
        )
        assertEquals(itemId, request.itemId)
        assertEquals(questions, request.questions)
        assertEquals(viewTimespans, request.viewTimespans)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }
}
