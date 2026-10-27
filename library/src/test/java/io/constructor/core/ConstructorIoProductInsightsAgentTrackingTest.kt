package io.constructor.core

import android.content.Context
import io.constructor.data.builder.*
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.common.TrackingItem
import io.constructor.data.model.tracking.ProductInsightsAgentFeedbackLabel
import io.constructor.data.model.tracking.ProductInsightsAgentQuestion
import io.constructor.data.model.tracking.ProductInsightsAgentViewTimespan
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConstructorIoProductInsightsAgentTrackingTest {

    @Rule
    @JvmField
    val overrideSchedulersRule = RxSchedulersOverrideRule()

    private lateinit var mockServer: MockWebServer
    private var constructorIo = ConstructorIo
    private val ctx = mockk<Context>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()

    private val tailFields = listOf("item_name", "variation_id", "thread_id")

    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()

        every { ctx.applicationContext } returns ctx

        every { preferencesHelper.apiKey } returns "copper-key"
        every { preferencesHelper.id } returns "wacko-the-guid"
        every { preferencesHelper.serviceUrl } returns mockServer.hostName
        every { preferencesHelper.port } returns mockServer.port
        every { preferencesHelper.scheme } returns "http"
        every { preferencesHelper.defaultItemSection } returns "Products"
        every { preferencesHelper.getSessionId(any(), any()) } returns 67
        every { preferencesHelper.getSessionId() } returns 67

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.defaultAnalyticsTags } returns mapOf("appVersion" to "123", "appPlatform" to "Android")
        every { configMemoryHolder.userId } returns "player-three"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("dummyKey")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    // region Focus

    @Test
    fun trackProductInsightsAgentFocus() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentFocusTrackingData.build("item-1") {
            itemName = "Waterproof Jacket"
            variationId = "var-1"
            threadId = "thread-1"
        }
        val observer = ConstructorIo.trackProductInsightsAgentFocusInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertEquals("POST", recordedRequest.method)
        assertPathAndSection(recordedRequest, "focus", "Products")

        val body = getJsonBody(recordedRequest)
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("Waterproof Jacket", body.getString("item_name"))
        assertEquals("var-1", body.getString("variation_id"))
        assertEquals("thread-1", body.getString("thread_id"))
        assertClientParams(recordedRequest, body)
    }

    @Test
    fun trackProductInsightsAgentFocusWithRequiredFieldsOnly() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentFocusTrackingData.build("item-1")
        val observer = ConstructorIo.trackProductInsightsAgentFocusInternal(request).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        assertEquals("item-1", body.getString("item_id"))
        assertAbsent(body, tailFields)
    }

    @Test
    fun trackProductInsightsAgentFocusWithSectionAndAnalyticsTags() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentFocusTrackingData.build("item-1") {
            sectionName = "Search Suggestions"
            analyticsTags = mapOf("test" to "test1", "appVersion" to "150")
        }
        val observer = ConstructorIo.trackProductInsightsAgentFocusInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertPathAndSection(recordedRequest, "focus", "Search Suggestions")
        val body = getJsonBody(recordedRequest)
        assertAbsent(body, listOf("section"))
        assertMergedAnalyticsTags(body)
    }

    @Test
    fun trackProductInsightsAgentFocusUsingPublicMethod() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        ConstructorIo.trackProductInsightsAgentFocus(ProductInsightsAgentFocusTrackingData.build("item-1"))
        val recordedRequest = awaitRequest()
        assertPathAndSection(recordedRequest, "focus", "Products")
        assertEquals("item-1", getJsonBody(recordedRequest).getString("item_id"))
    }

    @Test
    fun trackProductInsightsAgentFocus500() {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal server error"))
        val observer = ConstructorIo.trackProductInsightsAgentFocusInternal(ProductInsightsAgentFocusTrackingData.build("item-1")).test()
        observer.assertError { true }
        assertPathAndSection(awaitRequest(), "focus", "Products")
    }

    @Test
    fun trackProductInsightsAgentFocusTimeout() {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal server error").setBodyDelay(5, TimeUnit.SECONDS))
        val observer = ConstructorIo.trackProductInsightsAgentFocusInternal(ProductInsightsAgentFocusTrackingData.build("item-1")).test()
        observer.awaitDone(6, TimeUnit.SECONDS)
        observer.assertError(SocketTimeoutException::class.java)
        assertPathAndSection(awaitRequest(), "focus", "Products")
    }

    // endregion

    // region OutOfView

    @Test
    fun trackProductInsightsAgentOutOfView() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentOutOfViewTrackingData.build("item-1") {
            itemName = "Waterproof Jacket"
            variationId = "var-1"
            threadId = "thread-1"
        }
        val observer = ConstructorIo.trackProductInsightsAgentOutOfViewInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertEquals("POST", recordedRequest.method)
        assertPathAndSection(recordedRequest, "out_of_view", "Products")

        val body = getJsonBody(recordedRequest)
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("Waterproof Jacket", body.getString("item_name"))
        assertEquals("var-1", body.getString("variation_id"))
        assertEquals("thread-1", body.getString("thread_id"))
        assertClientParams(recordedRequest, body)
    }

    @Test
    fun trackProductInsightsAgentOutOfViewWithRequiredFieldsOnly() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val observer = ConstructorIo.trackProductInsightsAgentOutOfViewInternal(ProductInsightsAgentOutOfViewTrackingData.build("item-1")).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        assertEquals("item-1", body.getString("item_id"))
        assertAbsent(body, tailFields)
    }

    @Test
    fun trackProductInsightsAgentOutOfViewWithSectionAndAnalyticsTags() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentOutOfViewTrackingData.build("item-1") {
            sectionName = "Search Suggestions"
            analyticsTags = mapOf("test" to "test1", "appVersion" to "150")
        }
        val observer = ConstructorIo.trackProductInsightsAgentOutOfViewInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertPathAndSection(recordedRequest, "out_of_view", "Search Suggestions")
        assertMergedAnalyticsTags(getJsonBody(recordedRequest))
    }

    @Test
    fun trackProductInsightsAgentOutOfView500() {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal server error"))
        val observer = ConstructorIo.trackProductInsightsAgentOutOfViewInternal(ProductInsightsAgentOutOfViewTrackingData.build("item-1")).test()
        observer.assertError { true }
        assertPathAndSection(awaitRequest(), "out_of_view", "Products")
    }

    // endregion

    // region View

    @Test
    fun trackProductInsightsAgentView() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val questions = listOf(
            ProductInsightsAgentQuestion("Is it waterproof?", "Durability"),
            ProductInsightsAgentQuestion("Is it machine washable?"),
        )
        val request = ProductInsightsAgentViewTrackingData.build("item-1", questions) {
            itemName = "Waterproof Jacket"
            variationId = "var-1"
            threadId = "thread-1"
        }
        val observer = ConstructorIo.trackProductInsightsAgentViewInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertEquals("POST", recordedRequest.method)
        assertPathAndSection(recordedRequest, "view", "Products")

        val body = getJsonBody(recordedRequest)
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("Waterproof Jacket", body.getString("item_name"))
        assertEquals("var-1", body.getString("variation_id"))
        assertEquals("thread-1", body.getString("thread_id"))
        val sentQuestions = body.getJSONArray("questions")
        assertEquals(2, sentQuestions.length())
        assertEquals("Is it waterproof?", sentQuestions.getJSONObject(0).getString("question"))
        assertEquals("Durability", sentQuestions.getJSONObject(0).getString("question_topic"))
        assertEquals("Is it machine washable?", sentQuestions.getJSONObject(1).getString("question"))
        assertFalse(sentQuestions.getJSONObject(1).has("question_topic"))
        assertClientParams(recordedRequest, body)
    }

    @Test
    fun trackProductInsightsAgentViewWithRequiredFieldsOnly() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentViewTrackingData.build("item-1", listOf(ProductInsightsAgentQuestion("Is it waterproof?")))
        val observer = ConstructorIo.trackProductInsightsAgentViewInternal(request).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        assertEquals("item-1", body.getString("item_id"))
        assertEquals(1, body.getJSONArray("questions").length())
        assertAbsent(body, tailFields)
    }

    @Test
    fun trackProductInsightsAgentViewWithSectionAndAnalyticsTags() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentViewTrackingData.build("item-1", listOf(ProductInsightsAgentQuestion("Is it waterproof?"))) {
            sectionName = "Search Suggestions"
            analyticsTags = mapOf("test" to "test1", "appVersion" to "150")
        }
        val observer = ConstructorIo.trackProductInsightsAgentViewInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertPathAndSection(recordedRequest, "view", "Search Suggestions")
        assertMergedAnalyticsTags(getJsonBody(recordedRequest))
    }

    @Test
    fun trackProductInsightsAgentView500() {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal server error"))
        val request = ProductInsightsAgentViewTrackingData.build("item-1", listOf(ProductInsightsAgentQuestion("Is it waterproof?")))
        val observer = ConstructorIo.trackProductInsightsAgentViewInternal(request).test()
        observer.assertError { true }
        assertPathAndSection(awaitRequest(), "view", "Products")
    }

    // endregion

    // region Views

    @Test
    fun trackProductInsightsAgentViews() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentViewsTrackingData.build(
            "item-1",
            listOf(ProductInsightsAgentQuestion("Is it waterproof?", "Durability")),
            listOf(
                ProductInsightsAgentViewTimespan(1792550405123, 1792550410456),
                ProductInsightsAgentViewTimespan(1792550420000, 1792550425000),
            )
        ) {
            itemName = "Waterproof Jacket"
            variationId = "var-1"
            threadId = "thread-1"
        }
        val observer = ConstructorIo.trackProductInsightsAgentViewsInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertEquals("POST", recordedRequest.method)
        assertPathAndSection(recordedRequest, "views", "Products")

        val body = getJsonBody(recordedRequest)
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("Waterproof Jacket", body.getString("item_name"))
        assertEquals("var-1", body.getString("variation_id"))
        assertEquals("thread-1", body.getString("thread_id"))
        val sentQuestions = body.getJSONArray("questions")
        assertEquals("Is it waterproof?", sentQuestions.getJSONObject(0).getString("question"))
        assertEquals("Durability", sentQuestions.getJSONObject(0).getString("question_topic"))
        // Timespans are given as epoch milliseconds and sent as ISO-8601 UTC timestamps
        val timespans = body.getJSONArray("view_timespans")
        assertEquals(2, timespans.length())
        assertEquals("2026-10-21T02:40:05.123Z", timespans.getJSONObject(0).getString("start"))
        assertEquals("2026-10-21T02:40:10.456Z", timespans.getJSONObject(0).getString("end"))
        assertEquals("2026-10-21T02:40:20.000Z", timespans.getJSONObject(1).getString("start"))
        assertEquals("2026-10-21T02:40:25.000Z", timespans.getJSONObject(1).getString("end"))
        assertClientParams(recordedRequest, body)
    }

    @Test
    fun trackProductInsightsAgentViewsWithRequiredFieldsOnly() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentViewsTrackingData.build(
            "item-1",
            listOf(ProductInsightsAgentQuestion("Is it waterproof?")),
            listOf(ProductInsightsAgentViewTimespan(1792550405123, 1792550410456))
        )
        val observer = ConstructorIo.trackProductInsightsAgentViewsInternal(request).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        assertEquals("item-1", body.getString("item_id"))
        assertEquals(1, body.getJSONArray("view_timespans").length())
        assertAbsent(body, tailFields)
    }

    @Test
    fun trackProductInsightsAgentViewsSendsUtcTimespansRegardlessOfDefaultTimeZone() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val defaultTimeZone = java.util.TimeZone.getDefault()
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Santiago"))
            val request = ProductInsightsAgentViewsTrackingData.build(
                "item-1",
                listOf(ProductInsightsAgentQuestion("Is it waterproof?")),
                listOf(ProductInsightsAgentViewTimespan(1792550405123, 1792550410456))
            )
            val observer = ConstructorIo.trackProductInsightsAgentViewsInternal(request).test()
            observer.assertComplete()
            val timespan = getJsonBody(awaitRequest()).getJSONArray("view_timespans").getJSONObject(0)
            assertEquals("2026-10-21T02:40:05.123Z", timespan.getString("start"))
            assertEquals("2026-10-21T02:40:10.456Z", timespan.getString("end"))
        } finally {
            java.util.TimeZone.setDefault(defaultTimeZone)
        }
    }

    @Test
    fun trackProductInsightsAgentViewsWithSectionAndAnalyticsTags() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentViewsTrackingData.build(
            "item-1",
            listOf(ProductInsightsAgentQuestion("Is it waterproof?")),
            listOf(ProductInsightsAgentViewTimespan(1792550405123, 1792550410456))
        ) {
            sectionName = "Search Suggestions"
            analyticsTags = mapOf("test" to "test1", "appVersion" to "150")
        }
        val observer = ConstructorIo.trackProductInsightsAgentViewsInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertPathAndSection(recordedRequest, "views", "Search Suggestions")
        assertMergedAnalyticsTags(getJsonBody(recordedRequest))
    }

    @Test
    fun trackProductInsightsAgentViews500() {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal server error"))
        val request = ProductInsightsAgentViewsTrackingData.build(
            "item-1",
            listOf(ProductInsightsAgentQuestion("Is it waterproof?")),
            listOf(ProductInsightsAgentViewTimespan(1792550405123, 1792550410456))
        )
        val observer = ConstructorIo.trackProductInsightsAgentViewsInternal(request).test()
        observer.assertError { true }
        assertPathAndSection(awaitRequest(), "views", "Products")
    }

    // endregion

    // region QuestionClick

    @Test
    fun trackProductInsightsAgentQuestionClick() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentQuestionClickTrackingData.build("item-1", "Is it waterproof?") {
            questionTopic = "Durability"
            itemName = "Waterproof Jacket"
            variationId = "var-1"
            threadId = "thread-1"
        }
        val observer = ConstructorIo.trackProductInsightsAgentQuestionClickInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertEquals("POST", recordedRequest.method)
        assertPathAndSection(recordedRequest, "question_click", "Products")

        val body = getJsonBody(recordedRequest)
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("Is it waterproof?", body.getString("question"))
        assertEquals("Durability", body.getString("question_topic"))
        assertEquals("Waterproof Jacket", body.getString("item_name"))
        assertEquals("var-1", body.getString("variation_id"))
        assertEquals("thread-1", body.getString("thread_id"))
        assertClientParams(recordedRequest, body)
    }

    @Test
    fun trackProductInsightsAgentQuestionClickWithRequiredFieldsOnly() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentQuestionClickTrackingData.build("item-1", "Is it waterproof?")
        val observer = ConstructorIo.trackProductInsightsAgentQuestionClickInternal(request).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("Is it waterproof?", body.getString("question"))
        assertAbsent(body, listOf("question_topic") + tailFields)
    }

    @Test
    fun trackProductInsightsAgentQuestionClickWithSectionAndAnalyticsTags() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentQuestionClickTrackingData.build("item-1", "Is it waterproof?") {
            sectionName = "Search Suggestions"
            analyticsTags = mapOf("test" to "test1", "appVersion" to "150")
        }
        val observer = ConstructorIo.trackProductInsightsAgentQuestionClickInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertPathAndSection(recordedRequest, "question_click", "Search Suggestions")
        assertMergedAnalyticsTags(getJsonBody(recordedRequest))
    }

    @Test
    fun trackProductInsightsAgentQuestionClick500() {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal server error"))
        val request = ProductInsightsAgentQuestionClickTrackingData.build("item-1", "Is it waterproof?")
        val observer = ConstructorIo.trackProductInsightsAgentQuestionClickInternal(request).test()
        observer.assertError { true }
        assertPathAndSection(awaitRequest(), "question_click", "Products")
    }

    // endregion

    // region QuestionSubmit

    @Test
    fun trackProductInsightsAgentQuestionSubmit() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentQuestionSubmitTrackingData.build("item-1", "Is it waterproof?") {
            questionTopic = "Durability"
            itemName = "Waterproof Jacket"
            variationId = "var-1"
            threadId = "thread-1"
        }
        val observer = ConstructorIo.trackProductInsightsAgentQuestionSubmitInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertEquals("POST", recordedRequest.method)
        assertPathAndSection(recordedRequest, "question_submit", "Products")

        val body = getJsonBody(recordedRequest)
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("Is it waterproof?", body.getString("question"))
        assertEquals("Durability", body.getString("question_topic"))
        assertEquals("Waterproof Jacket", body.getString("item_name"))
        assertEquals("var-1", body.getString("variation_id"))
        assertEquals("thread-1", body.getString("thread_id"))
        assertClientParams(recordedRequest, body)
    }

    @Test
    fun trackProductInsightsAgentQuestionSubmitWithRequiredFieldsOnly() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentQuestionSubmitTrackingData.build("item-1", "Is it waterproof?")
        val observer = ConstructorIo.trackProductInsightsAgentQuestionSubmitInternal(request).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("Is it waterproof?", body.getString("question"))
        assertAbsent(body, listOf("question_topic") + tailFields)
    }

    @Test
    fun trackProductInsightsAgentQuestionSubmitWithSectionAndAnalyticsTags() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentQuestionSubmitTrackingData.build("item-1", "Is it waterproof?") {
            sectionName = "Search Suggestions"
            analyticsTags = mapOf("test" to "test1", "appVersion" to "150")
        }
        val observer = ConstructorIo.trackProductInsightsAgentQuestionSubmitInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertPathAndSection(recordedRequest, "question_submit", "Search Suggestions")
        assertMergedAnalyticsTags(getJsonBody(recordedRequest))
    }

    @Test
    fun trackProductInsightsAgentQuestionSubmit500() {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal server error"))
        val request = ProductInsightsAgentQuestionSubmitTrackingData.build("item-1", "Is it waterproof?")
        val observer = ConstructorIo.trackProductInsightsAgentQuestionSubmitInternal(request).test()
        observer.assertError { true }
        assertPathAndSection(awaitRequest(), "question_submit", "Products")
    }

    // endregion

    // region AnswerFeedback

    @Test
    fun trackProductInsightsAgentAnswerFeedback() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentAnswerFeedbackTrackingData.build("item-1", ProductInsightsAgentFeedbackLabel.THUMBS_UP) {
            qnaResultId = "qna-1"
            itemName = "Waterproof Jacket"
            variationId = "var-1"
            threadId = "thread-1"
        }
        val observer = ConstructorIo.trackProductInsightsAgentAnswerFeedbackInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertEquals("POST", recordedRequest.method)
        assertPathAndSection(recordedRequest, "answer_feedback", "Products")

        val body = getJsonBody(recordedRequest)
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("thumbs_up", body.getString("feedback_label"))
        assertEquals("qna-1", body.getString("qna_result_id"))
        assertEquals("Waterproof Jacket", body.getString("item_name"))
        assertEquals("var-1", body.getString("variation_id"))
        assertEquals("thread-1", body.getString("thread_id"))
        assertClientParams(recordedRequest, body)
    }

    @Test
    fun trackProductInsightsAgentAnswerFeedbackThumbsDown() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentAnswerFeedbackTrackingData.build("item-1", ProductInsightsAgentFeedbackLabel.THUMBS_DOWN)
        val observer = ConstructorIo.trackProductInsightsAgentAnswerFeedbackInternal(request).test()
        observer.assertComplete()
        assertEquals("thumbs_down", getJsonBody(awaitRequest()).getString("feedback_label"))
    }

    @Test
    fun trackProductInsightsAgentAnswerFeedbackWithRequiredFieldsOnly() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentAnswerFeedbackTrackingData.build("item-1", ProductInsightsAgentFeedbackLabel.THUMBS_UP)
        val observer = ConstructorIo.trackProductInsightsAgentAnswerFeedbackInternal(request).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("thumbs_up", body.getString("feedback_label"))
        assertAbsent(body, listOf("qna_result_id") + tailFields)
    }

    @Test
    fun trackProductInsightsAgentAnswerFeedbackWithSectionAndAnalyticsTags() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentAnswerFeedbackTrackingData.build("item-1", ProductInsightsAgentFeedbackLabel.THUMBS_UP) {
            sectionName = "Search Suggestions"
            analyticsTags = mapOf("test" to "test1", "appVersion" to "150")
        }
        val observer = ConstructorIo.trackProductInsightsAgentAnswerFeedbackInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertPathAndSection(recordedRequest, "answer_feedback", "Search Suggestions")
        assertMergedAnalyticsTags(getJsonBody(recordedRequest))
    }

    @Test
    fun trackProductInsightsAgentAnswerFeedback500() {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal server error"))
        val request = ProductInsightsAgentAnswerFeedbackTrackingData.build("item-1", ProductInsightsAgentFeedbackLabel.THUMBS_UP)
        val observer = ConstructorIo.trackProductInsightsAgentAnswerFeedbackInternal(request).test()
        observer.assertError { true }
        assertPathAndSection(awaitRequest(), "answer_feedback", "Products")
    }

    // endregion

    // region ResultClick

    @Test
    fun trackProductInsightsAgentResultClick() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentResultClickTrackingData.build("rec-1", "Is it waterproof?", "item-1") {
            position = 2
            seedItemName = "Waterproof Jacket"
            seedVariationId = "var-1"
            qnaResultId = "qna-1"
            itemName = "Rain Boots"
            variationId = "rec-var-1"
            threadId = "thread-1"
        }
        val observer = ConstructorIo.trackProductInsightsAgentResultClickInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertEquals("POST", recordedRequest.method)
        assertPathAndSection(recordedRequest, "result_click", "Products")

        val body = getJsonBody(recordedRequest)
        assertEquals("rec-1", body.getString("item_id"))
        assertEquals("Is it waterproof?", body.getString("question"))
        assertEquals("item-1", body.getString("seed_item_id"))
        assertEquals(2, body.getInt("position"))
        assertEquals("Waterproof Jacket", body.getString("seed_item_name"))
        assertEquals("var-1", body.getString("seed_variation_id"))
        assertEquals("qna-1", body.getString("qna_result_id"))
        assertEquals("Rain Boots", body.getString("item_name"))
        assertEquals("rec-var-1", body.getString("variation_id"))
        assertEquals("thread-1", body.getString("thread_id"))
        assertClientParams(recordedRequest, body)
    }

    @Test
    fun trackProductInsightsAgentResultClickWithPositionZero() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentResultClickTrackingData.build("rec-1", "Is it waterproof?", "item-1") {
            position = 0
        }
        val observer = ConstructorIo.trackProductInsightsAgentResultClickInternal(request).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        assertTrue(body.has("position"))
        assertEquals(0, body.getInt("position"))
    }

    @Test
    fun trackProductInsightsAgentResultClickWithRequiredFieldsOnly() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentResultClickTrackingData.build("rec-1", "Is it waterproof?", "item-1")
        val observer = ConstructorIo.trackProductInsightsAgentResultClickInternal(request).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        assertEquals("rec-1", body.getString("item_id"))
        assertEquals("Is it waterproof?", body.getString("question"))
        assertEquals("item-1", body.getString("seed_item_id"))
        assertAbsent(body, listOf("position", "seed_item_name", "seed_variation_id", "qna_result_id") + tailFields)
    }

    @Test
    fun trackProductInsightsAgentResultClickWithSectionAndAnalyticsTags() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentResultClickTrackingData.build("rec-1", "Is it waterproof?", "item-1") {
            sectionName = "Search Suggestions"
            analyticsTags = mapOf("test" to "test1", "appVersion" to "150")
        }
        val observer = ConstructorIo.trackProductInsightsAgentResultClickInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertPathAndSection(recordedRequest, "result_click", "Search Suggestions")
        assertMergedAnalyticsTags(getJsonBody(recordedRequest))
    }

    @Test
    fun trackProductInsightsAgentResultClick500() {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal server error"))
        val request = ProductInsightsAgentResultClickTrackingData.build("rec-1", "Is it waterproof?", "item-1")
        val observer = ConstructorIo.trackProductInsightsAgentResultClickInternal(request).test()
        observer.assertError { true }
        assertPathAndSection(awaitRequest(), "result_click", "Products")
    }

    // endregion

    // region AnswerView

    @Test
    fun trackProductInsightsAgentAnswerView() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentAnswerViewTrackingData.build("item-1", "Is it waterproof?", "Yes, it is fully waterproof.") {
            questionTopic = "Durability"
            qnaResultId = "qna-1"
            items = listOf(
                TrackingItem("rec-1", "rec-var-1", "camp-1", "owner-a", "Rain Jacket"),
                TrackingItem("rec-2", null, null, null),
            )
            followUpQuestions = listOf("Is it breathable?", "Does it come in blue?")
            itemName = "Waterproof Jacket"
            variationId = "var-1"
            threadId = "thread-1"
        }
        val observer = ConstructorIo.trackProductInsightsAgentAnswerViewInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertEquals("POST", recordedRequest.method)
        assertPathAndSection(recordedRequest, "answer_view", "Products")

        val body = getJsonBody(recordedRequest)
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("Is it waterproof?", body.getString("question"))
        assertEquals("Yes, it is fully waterproof.", body.getString("answer_text"))
        assertEquals("Durability", body.getString("question_topic"))
        assertEquals("qna-1", body.getString("qna_result_id"))
        assertEquals("Waterproof Jacket", body.getString("item_name"))
        assertEquals("var-1", body.getString("variation_id"))
        assertEquals("thread-1", body.getString("thread_id"))

        val items = body.getJSONArray("items")
        assertEquals(2, items.length())
        val firstItem = items.getJSONObject(0)
        assertEquals("rec-1", firstItem.getString("item_id"))
        assertEquals("rec-var-1", firstItem.getString("variation_id"))
        assertEquals("camp-1", firstItem.getString("sl_campaign_id"))
        assertEquals("owner-a", firstItem.getString("sl_campaign_owner"))
        assertEquals("Rain Jacket", firstItem.getString("item_name"))
        val secondItem = items.getJSONObject(1)
        assertEquals("rec-2", secondItem.getString("item_id"))
        assertEquals(setOf("item_id"), secondItem.keys().asSequence().toSet())

        val followUpQuestions = body.getJSONArray("follow_up_questions")
        assertEquals(2, followUpQuestions.length())
        assertEquals("Is it breathable?", followUpQuestions.getJSONObject(0).getString("value"))
        assertEquals("Does it come in blue?", followUpQuestions.getJSONObject(1).getString("value"))
        assertClientParams(recordedRequest, body)
    }

    @Test
    fun trackProductInsightsAgentAnswerViewWithEmptyAnswerText() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentAnswerViewTrackingData.build("item-1", "Is it waterproof?", "")
        val observer = ConstructorIo.trackProductInsightsAgentAnswerViewInternal(request).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        assertTrue(body.has("answer_text"))
        assertEquals("", body.getString("answer_text"))
    }

    @Test
    fun trackProductInsightsAgentAnswerViewCapsListsAt100() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentAnswerViewTrackingData.build("item-1", "Is it waterproof?", "Yes.") {
            items = (1..150).map { TrackingItem("rec-$it", null, null, null) }
            followUpQuestions = (1..150).map { "Question $it?" }
        }
        val observer = ConstructorIo.trackProductInsightsAgentAnswerViewInternal(request).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        val items = body.getJSONArray("items")
        assertEquals(100, items.length())
        assertEquals("rec-1", items.getJSONObject(0).getString("item_id"))
        assertEquals("rec-100", items.getJSONObject(99).getString("item_id"))
        val followUpQuestions = body.getJSONArray("follow_up_questions")
        assertEquals(100, followUpQuestions.length())
        assertEquals("Question 1?", followUpQuestions.getJSONObject(0).getString("value"))
        assertEquals("Question 100?", followUpQuestions.getJSONObject(99).getString("value"))
    }

    private fun trackAnswerViewAndGetAnswerText(answerText: String): String {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentAnswerViewTrackingData.build("item-1", "Is it waterproof?", answerText)
        val observer = ConstructorIo.trackProductInsightsAgentAnswerViewInternal(request).test()
        observer.assertComplete()
        return getJsonBody(awaitRequest()).getString("answer_text")
    }

    @Test
    fun trackProductInsightsAgentAnswerViewKeepsAnswerTextAt2000CodePoints() {
        val answerText = "a".repeat(2000)
        assertEquals(answerText, trackAnswerViewAndGetAnswerText(answerText))
    }

    @Test
    fun trackProductInsightsAgentAnswerViewTruncatesAnswerTextTo2000CodePoints() {
        assertEquals("a".repeat(2000), trackAnswerViewAndGetAnswerText("a".repeat(2500)))
    }

    @Test
    fun trackProductInsightsAgentAnswerViewCountsAnswerTextInCodePointsNotUtf16Units() {
        // U+1F600 is 2 UTF-16 units but 1 code point, so 2000 of them are under the limit
        val answerText = "😀".repeat(2000)
        assertEquals(answerText, trackAnswerViewAndGetAnswerText(answerText))
    }

    @Test
    fun trackProductInsightsAgentAnswerViewTruncatesAnswerTextWithoutSplittingSurrogatePairs() {
        // 2001 code points: the cut falls between emoji, so no half surrogate pair is sent
        assertEquals("a" + "\uD83D\uDE00".repeat(1999), trackAnswerViewAndGetAnswerText("a" + "\uD83D\uDE00".repeat(2000)))
    }

    @Test
    fun trackProductInsightsAgentAnswerViewWithRequiredFieldsOnly() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentAnswerViewTrackingData.build("item-1", "Is it waterproof?", "Yes.")
        val observer = ConstructorIo.trackProductInsightsAgentAnswerViewInternal(request).test()
        observer.assertComplete()
        val body = getJsonBody(awaitRequest())
        assertEquals("item-1", body.getString("item_id"))
        assertEquals("Is it waterproof?", body.getString("question"))
        assertEquals("Yes.", body.getString("answer_text"))
        assertAbsent(body, listOf("question_topic", "qna_result_id", "items", "follow_up_questions") + tailFields)
    }

    @Test
    fun trackProductInsightsAgentAnswerViewWithSectionAndAnalyticsTags() {
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val request = ProductInsightsAgentAnswerViewTrackingData.build("item-1", "Is it waterproof?", "Yes.") {
            sectionName = "Search Suggestions"
            analyticsTags = mapOf("test" to "test1", "appVersion" to "150")
        }
        val observer = ConstructorIo.trackProductInsightsAgentAnswerViewInternal(request).test()
        observer.assertComplete()
        val recordedRequest = awaitRequest()
        assertPathAndSection(recordedRequest, "answer_view", "Search Suggestions")
        assertMergedAnalyticsTags(getJsonBody(recordedRequest))
    }

    @Test
    fun trackProductInsightsAgentAnswerView500() {
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal server error"))
        val request = ProductInsightsAgentAnswerViewTrackingData.build("item-1", "Is it waterproof?", "Yes.")
        val observer = ConstructorIo.trackProductInsightsAgentAnswerViewInternal(request).test()
        observer.assertError { true }
        assertPathAndSection(awaitRequest(), "answer_view", "Products")
    }

    // endregion

    private fun awaitRequest(): RecordedRequest {
        return mockServer.takeRequest(1, TimeUnit.SECONDS)
                ?: throw AssertionError("Expected request but timed out")
    }

    private fun getJsonBody(request: RecordedRequest): JSONObject = JSONObject(request.body.readUtf8())

    private fun assertPathAndSection(request: RecordedRequest, suffix: String, section: String) {
        val url = request.requestUrl!!
        assertEquals("/v2/behavioral_action/product_insights_agent_$suffix", url.encodedPath)
        assertEquals(section, url.queryParameter("section"))
    }

    // The client parameters are added to the query by RequestInterceptor, not duplicated in the body
    private fun assertClientParams(request: RecordedRequest, body: JSONObject) {
        val url = request.requestUrl!!
        assertEquals("wacko-the-guid", url.queryParameter("i"))
        assertEquals("67", url.queryParameter("s"))
        assertEquals("copper-key", url.queryParameter("key"))
        assertEquals("player-three", url.queryParameter("ui"))
        assertTrue(url.queryParameter("c")!!.startsWith("cioand-"))
        assertTrue(url.queryParameter("_dt")!!.toLong() > 0)
        assertAbsent(body, listOf("c", "i", "s", "key", "ui", "us", "_dt", "beacon", "section"))
        val analyticsTags = body.getJSONObject("analytics_tags")
        assertEquals("123", analyticsTags.getString("appVersion"))
        assertEquals("Android", analyticsTags.getString("appPlatform"))
    }

    private fun assertMergedAnalyticsTags(body: JSONObject) {
        val analyticsTags = body.getJSONObject("analytics_tags")
        assertEquals(3, analyticsTags.length())
        assertEquals("150", analyticsTags.getString("appVersion"))
        assertEquals("Android", analyticsTags.getString("appPlatform"))
        assertEquals("test1", analyticsTags.getString("test"))
    }

    private fun assertAbsent(body: JSONObject, keys: List<String>) {
        keys.forEach { assertFalse(body.has(it), "Expected \"$it\" to be absent from the request body") }
    }
}
