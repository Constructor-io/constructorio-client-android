package io.constructor.data.builder

import io.constructor.data.model.tracking.ProductInsightsAgentFeedbackLabel
import io.constructor.data.model.tracking.ProductInsightsAgentQuestion
import io.constructor.data.model.tracking.ProductInsightsAgentViewTimespan
import org.junit.Test
import kotlin.test.assertEquals

class ProductInsightsAgentTrackingDataTest {
    private val itemId = "item-1"
    private val questions = listOf(ProductInsightsAgentQuestion("Is it waterproof?"))

    @Test
    fun everyEventExposesTheSharedFieldsThroughTheInterface() {
        val requests: List<ProductInsightsAgentTrackingData> = listOf(
            ProductInsightsAgentFocusTrackingData(itemId, "Waterproof Jacket", "var-1", "thread-1", "Search Suggestions", mapOf("test" to "test1")),
            ProductInsightsAgentOutOfViewTrackingData(itemId, "Waterproof Jacket", "var-1", "thread-1", "Search Suggestions", mapOf("test" to "test1")),
            ProductInsightsAgentViewTrackingData(itemId, questions, "Waterproof Jacket", "var-1", "thread-1", "Search Suggestions", mapOf("test" to "test1")),
            ProductInsightsAgentViewsTrackingData(itemId, questions, listOf(ProductInsightsAgentViewTimespan(1, 2)), "Waterproof Jacket", "var-1", "thread-1", "Search Suggestions", mapOf("test" to "test1")),
            ProductInsightsAgentQuestionClickTrackingData(itemId, "Is it waterproof?", itemName = "Waterproof Jacket", variationId = "var-1", threadId = "thread-1", sectionName = "Search Suggestions", analyticsTags = mapOf("test" to "test1")),
            ProductInsightsAgentQuestionSubmitTrackingData(itemId, "Is it waterproof?", itemName = "Waterproof Jacket", variationId = "var-1", threadId = "thread-1", sectionName = "Search Suggestions", analyticsTags = mapOf("test" to "test1")),
            ProductInsightsAgentAnswerFeedbackTrackingData(itemId, ProductInsightsAgentFeedbackLabel.THUMBS_UP, itemName = "Waterproof Jacket", variationId = "var-1", threadId = "thread-1", sectionName = "Search Suggestions", analyticsTags = mapOf("test" to "test1")),
            ProductInsightsAgentResultClickTrackingData(itemId, "Is it waterproof?", "seed-1", itemName = "Waterproof Jacket", variationId = "var-1", threadId = "thread-1", sectionName = "Search Suggestions", analyticsTags = mapOf("test" to "test1")),
            ProductInsightsAgentAnswerViewTrackingData(itemId, "Is it waterproof?", "Yes", itemName = "Waterproof Jacket", variationId = "var-1", threadId = "thread-1", sectionName = "Search Suggestions", analyticsTags = mapOf("test" to "test1")),
        )
        requests.forEach {
            val name = it::class.simpleName
            assertEquals(itemId, it.itemId, name)
            assertEquals("Waterproof Jacket", it.itemName, name)
            assertEquals("var-1", it.variationId, name)
            assertEquals("thread-1", it.threadId, name)
            assertEquals("Search Suggestions", it.sectionName, name)
            assertEquals(mapOf("test" to "test1"), it.analyticsTags, name)
        }
    }
}
