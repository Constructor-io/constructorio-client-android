package io.constructor.data.builder

import io.constructor.data.model.common.TrackingItem
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductInsightsAgentAnswerViewTrackingDataTest {
    private val itemId = "item-1"
    private val question = "Is it waterproof?"
    private val answerText = "Yes, it is fully waterproof."
    private val questionTopic = "Durability"
    private val qnaResultId = "qna-1"
    private val items = listOf(TrackingItem("rec-1", null, null, null))
    private val followUpQuestions = listOf("Is it breathable?")
    private val itemName = "Waterproof Jacket"
    private val variationId = "var-1"
    private val threadId = "thread-1"
    private val sectionName = "Search Suggestions"
    private val analyticsTags = mapOf("test" to "test1")

    @Test
    fun answerViewTrackingDataWithRequiredParams() {
        val request = ProductInsightsAgentAnswerViewTrackingData.build(itemId, question, answerText)
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertEquals(answerText, request.answerText)
        assertNull(request.questionTopic)
        assertNull(request.qnaResultId)
        assertNull(request.items)
        assertNull(request.followUpQuestions)
        assertNull(request.itemName)
        assertNull(request.variationId)
        assertNull(request.threadId)
        assertNull(request.sectionName)
        assertNull(request.analyticsTags)
    }

    @Test
    fun answerViewTrackingDataUsingDsl() {
        val request = ProductInsightsAgentAnswerViewTrackingData.build(itemId, question, answerText) {
            this.questionTopic = this@ProductInsightsAgentAnswerViewTrackingDataTest.questionTopic
            this.qnaResultId = this@ProductInsightsAgentAnswerViewTrackingDataTest.qnaResultId
            this.items = this@ProductInsightsAgentAnswerViewTrackingDataTest.items
            this.followUpQuestions = this@ProductInsightsAgentAnswerViewTrackingDataTest.followUpQuestions
            this.itemName = this@ProductInsightsAgentAnswerViewTrackingDataTest.itemName
            this.variationId = this@ProductInsightsAgentAnswerViewTrackingDataTest.variationId
            this.threadId = this@ProductInsightsAgentAnswerViewTrackingDataTest.threadId
            this.sectionName = this@ProductInsightsAgentAnswerViewTrackingDataTest.sectionName
            this.analyticsTags = this@ProductInsightsAgentAnswerViewTrackingDataTest.analyticsTags
        }
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertEquals(answerText, request.answerText)
        assertEquals(questionTopic, request.questionTopic)
        assertEquals(qnaResultId, request.qnaResultId)
        assertEquals(items, request.items)
        assertEquals(followUpQuestions, request.followUpQuestions)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun answerViewTrackingDataUsingBuilder() {
        val request = ProductInsightsAgentAnswerViewTrackingData.Builder(itemId, question, answerText)
            .setQuestionTopic(questionTopic)
            .setQnaResultId(qnaResultId)
            .setItems(items)
            .setFollowUpQuestions(followUpQuestions)
            .setItemName(itemName)
            .setVariationId(variationId)
            .setThreadId(threadId)
            .setSectionName(sectionName)
            .setAnalyticsTags(analyticsTags)
            .build()
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertEquals(answerText, request.answerText)
        assertEquals(questionTopic, request.questionTopic)
        assertEquals(qnaResultId, request.qnaResultId)
        assertEquals(items, request.items)
        assertEquals(followUpQuestions, request.followUpQuestions)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun answerViewTrackingDataUsingConstructor() {
        val request = ProductInsightsAgentAnswerViewTrackingData(
            itemId = itemId,
            question = question,
            answerText = answerText,
            questionTopic = questionTopic,
            qnaResultId = qnaResultId,
            items = items,
            followUpQuestions = followUpQuestions,
            itemName = itemName,
            variationId = variationId,
            threadId = threadId,
            sectionName = sectionName,
            analyticsTags = analyticsTags,
        )
        assertEquals(itemId, request.itemId)
        assertEquals(question, request.question)
        assertEquals(answerText, request.answerText)
        assertEquals(questionTopic, request.questionTopic)
        assertEquals(qnaResultId, request.qnaResultId)
        assertEquals(items, request.items)
        assertEquals(followUpQuestions, request.followUpQuestions)
        assertEquals(itemName, request.itemName)
        assertEquals(variationId, request.variationId)
        assertEquals(threadId, request.threadId)
        assertEquals(sectionName, request.sectionName)
        assertEquals(analyticsTags, request.analyticsTags)
    }

    @Test
    fun answerViewTrackingDataKeepsEmptyAnswerText() {
        val request = ProductInsightsAgentAnswerViewTrackingData.build(itemId, question, "")
        assertEquals("", request.answerText)
    }
}
