package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductInsightsAgentResultClickTrackingDataTest {
    private val itemId = "item-1"
    private val question = "Is it waterproof?"
    private val seedItemId = "seed-1"
    private val position = 0
    private val seedItemName = "Seed Jacket"
    private val seedVariationId = "seed-var-1"
    private val qnaResultId = "qna-1"
    private val itemName = "Waterproof Jacket"
    private val variationId = "var-1"
    private val threadId = "thread-1"
    private val sectionName = "Search Suggestions"
    private val analyticsTags = mapOf("test" to "test1")

    @Test
    fun resultClickTrackingDataWithRequiredParams() {
        val request = ProductInsightsAgentResultClickTrackingData.build(itemId, question, seedItemId)
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertEquals(seedItemId, request.seedItemId)
        assertNull(request.position)
        assertNull(request.seedItemName)
        assertNull(request.seedVariationId)
        assertNull(request.qnaResultId)
        assertNull(request.itemName)
        assertNull(request.variationId)
        assertNull(request.threadId)
        assertNull(request.sectionName)
        assertNull(request.analyticsTags)
    }

    @Test
    fun resultClickTrackingDataUsingDsl() {
        val request = ProductInsightsAgentResultClickTrackingData.build(itemId, question, seedItemId) {
            this.position = this@ProductInsightsAgentResultClickTrackingDataTest.position
            this.seedItemName = this@ProductInsightsAgentResultClickTrackingDataTest.seedItemName
            this.seedVariationId = this@ProductInsightsAgentResultClickTrackingDataTest.seedVariationId
            this.qnaResultId = this@ProductInsightsAgentResultClickTrackingDataTest.qnaResultId
            this.itemName = this@ProductInsightsAgentResultClickTrackingDataTest.itemName
            this.variationId = this@ProductInsightsAgentResultClickTrackingDataTest.variationId
            this.threadId = this@ProductInsightsAgentResultClickTrackingDataTest.threadId
            this.sectionName = this@ProductInsightsAgentResultClickTrackingDataTest.sectionName
            this.analyticsTags = this@ProductInsightsAgentResultClickTrackingDataTest.analyticsTags
        }
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertEquals(seedItemId, request.seedItemId)
        assertEquals(position, request.position)
        assertEquals(seedItemName, request.seedItemName)
        assertEquals(seedVariationId, request.seedVariationId)
        assertEquals(qnaResultId, request.qnaResultId)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun resultClickTrackingDataUsingBuilder() {
        val request = ProductInsightsAgentResultClickTrackingData.Builder(itemId, question, seedItemId)
            .setPosition(position)
            .setSeedItemName(seedItemName)
            .setSeedVariationId(seedVariationId)
            .setQnaResultId(qnaResultId)
            .setItemName(itemName)
            .setVariationId(variationId)
            .setThreadId(threadId)
            .setSectionName(sectionName)
            .setAnalyticsTags(analyticsTags)
            .build()
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertEquals(seedItemId, request.seedItemId)
        assertEquals(position, request.position)
        assertEquals(seedItemName, request.seedItemName)
        assertEquals(seedVariationId, request.seedVariationId)
        assertEquals(qnaResultId, request.qnaResultId)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun resultClickTrackingDataUsingConstructor() {
        val request = ProductInsightsAgentResultClickTrackingData(
            itemId = itemId,
            question = question,
            seedItemId = seedItemId,
            position = position,
            seedItemName = seedItemName,
            seedVariationId = seedVariationId,
            qnaResultId = qnaResultId,
            itemName = itemName,
            variationId = variationId,
            threadId = threadId,
            sectionName = sectionName,
            analyticsTags = analyticsTags,
        )
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertEquals(seedItemId, request.seedItemId)
        assertEquals(position, request.position)
        assertEquals(seedItemName, request.seedItemName)
        assertEquals(seedVariationId, request.seedVariationId)
        assertEquals(qnaResultId, request.qnaResultId)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun resultClickTrackingDataKeepsPositionZero() {
        val request = ProductInsightsAgentResultClickTrackingData.build(itemId, question, seedItemId) {
            position = 0
        }
        assertEquals(0, request.position)
    }
}
