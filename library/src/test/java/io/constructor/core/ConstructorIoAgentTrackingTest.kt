package io.constructor.core

import android.content.Context
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.common.ResultsImpressionItem
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class ConstructorIoAgentTrackingTest {

    @Rule
    @JvmField
    val overrideSchedulersRule = RxSchedulersOverrideRule()

    private lateinit var mockServer: MockWebServer
    private var constructorIo = ConstructorIo
    private val ctx = mockk<Context>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()

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

    // --- trackAgentSubmit ---

    @Test
    fun trackAgentSubmit() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentSubmitInternal("find me a red shirt").test()
        observer.assertComplete()
        val request = awaitRequest()
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_submit"))

        val requestBody = getRequestBody(request)
        assertEquals("find me a red shirt", requestBody["intent"])
        assertEquals("true", requestBody["beacon"])
        assertEquals("Products", requestBody["section"])
        assertEquals("wacko-the-guid", requestBody["i"])
        assertEquals("67", requestBody["s"])
    }

    @Test
    fun trackAgentSubmitWithSection() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentSubmitInternal("find me a red shirt", sectionName = "Search Suggestions").test()
        observer.assertComplete()
        val request = awaitRequest()

        val requestBody = getRequestBody(request)
        assertEquals("Search Suggestions", requestBody["section"])
    }

    @Test
    fun trackAgentSubmit500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentSubmitInternal("find me a red shirt").test()
        observer.assertError { true }
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_submit"))
    }

    @Test
    fun trackAgentSubmitTimeout() {
        val mockResponse = MockResponse()
                .setResponseCode(500)
                .setBody("Internal server error")
                .setBodyDelay(5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentSubmitInternal("find me a red shirt").test()
        observer.awaitDone(6, TimeUnit.SECONDS)
        observer.assertError(SocketTimeoutException::class.java)
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_submit"))
    }

    // --- trackAgentResultLoadStarted ---

    @Test
    fun trackAgentResultLoadStarted() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultLoadStartedInternal("find me a red shirt", intentResultId = "intent-123").test()
        observer.assertComplete()
        val request = awaitRequest()
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_result_load_start"))

        val requestBody = getRequestBody(request)
        assertEquals("find me a red shirt", requestBody["intent"])
        assertEquals("intent-123", requestBody["intent_result_id"])
        assertEquals("true", requestBody["beacon"])
        assertEquals("Products", requestBody["section"])
    }

    @Test
    fun trackAgentResultLoadStarted500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultLoadStartedInternal("find me a red shirt").test()
        observer.assertError { true }
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_result_load_start"))
    }

    @Test
    fun trackAgentResultLoadStartedTimeout() {
        val mockResponse = MockResponse()
                .setResponseCode(500)
                .setBody("Internal server error")
                .setBodyDelay(5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultLoadStartedInternal("find me a red shirt").test()
        observer.awaitDone(6, TimeUnit.SECONDS)
        observer.assertError(SocketTimeoutException::class.java)
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_result_load_start"))
    }

    // --- trackAgentResultLoadFinished ---

    @Test
    fun trackAgentResultLoadFinished() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultLoadFinishedInternal("find me a red shirt", 25, intentResultId = "intent-123").test()
        observer.assertComplete()
        val request = awaitRequest()
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_result_load_finish"))

        val requestBody = getRequestBody(request)
        assertEquals("find me a red shirt", requestBody["intent"])
        assertEquals("25", requestBody["search_result_count"])
        assertEquals("intent-123", requestBody["intent_result_id"])
        assertEquals("true", requestBody["beacon"])
        assertEquals("Products", requestBody["section"])
    }

    @Test
    fun trackAgentResultLoadFinished500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultLoadFinishedInternal("find me a red shirt", 25).test()
        observer.assertError { true }
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_result_load_finish"))
    }

    @Test
    fun trackAgentResultLoadFinishedTimeout() {
        val mockResponse = MockResponse()
                .setResponseCode(500)
                .setBody("Internal server error")
                .setBodyDelay(5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultLoadFinishedInternal("find me a red shirt", 25).test()
        observer.awaitDone(6, TimeUnit.SECONDS)
        observer.assertError(SocketTimeoutException::class.java)
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_result_load_finish"))
    }

    // --- trackAgentResultClick ---

    @Test
    fun trackAgentResultClick() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultClickInternal(
                "find me a red shirt",
                "result-123",
                itemId = "item-456",
                itemName = "Red Shirt",
                variationId = "var-789",
                intentResultId = "intent-123"
        ).test()
        observer.assertComplete()
        val request = awaitRequest()
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_search_result_click"))

        val requestBody = getRequestBody(request)
        assertEquals("find me a red shirt", requestBody["intent"])
        assertEquals("result-123", requestBody["search_result_id"])
        assertEquals("item-456", requestBody["item_id"])
        assertEquals("Red Shirt", requestBody["item_name"])
        assertEquals("var-789", requestBody["variation_id"])
        assertEquals("intent-123", requestBody["intent_result_id"])
        assertEquals("true", requestBody["beacon"])
        assertEquals("Products", requestBody["section"])
    }

    @Test
    fun trackAgentResultClickWithSection() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultClickInternal(
                "find me a red shirt",
                "result-123",
                sectionName = "Search Suggestions"
        ).test()
        observer.assertComplete()
        val request = awaitRequest()

        val requestBody = getRequestBody(request)
        assertEquals("Search Suggestions", requestBody["section"])
    }

    @Test
    fun trackAgentResultClick500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultClickInternal("find me a red shirt", "result-123").test()
        observer.assertError { true }
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_search_result_click"))
    }

    @Test
    fun trackAgentResultClickTimeout() {
        val mockResponse = MockResponse()
                .setResponseCode(500)
                .setBody("Internal server error")
                .setBodyDelay(5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultClickInternal("find me a red shirt", "result-123").test()
        observer.awaitDone(6, TimeUnit.SECONDS)
        observer.assertError(SocketTimeoutException::class.java)
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_search_result_click"))
    }

    // --- trackAgentResultView ---

    @Test
    fun trackAgentResultView() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val items = listOf(
                ResultsImpressionItem("item-1", "Item One"),
                ResultsImpressionItem("item-2", "Item Two", variationId = "var-2")
        )
        val observer = ConstructorIo.trackAgentResultViewInternal(
                "find me a red shirt",
                "result-123",
                5,
                items,
                intentResultId = "intent-123"
        ).test()
        observer.assertComplete()
        val request = awaitRequest()
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_search_result_view"))

        val requestBody = getRequestBody(request)
        assertEquals("find me a red shirt", requestBody["intent"])
        assertEquals("result-123", requestBody["search_result_id"])
        assertEquals("5", requestBody["num_results_viewed"])
        assertEquals("intent-123", requestBody["intent_result_id"])
        assertEquals("true", requestBody["beacon"])
        assertEquals("Products", requestBody["section"])
        assert(requestBody["items"]!!.contains("item_id:item-1"))
        assert(requestBody["items"]!!.contains("item_name:Item One"))
        assert(requestBody["items"]!!.contains("item_id:item-2"))
        assert(requestBody["items"]!!.contains("variation_id:var-2"))
    }

    @Test
    fun trackAgentResultViewCapsItemsAt100() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val items = (1..150).map { ResultsImpressionItem("item-$it", "Item $it") }
        val observer = ConstructorIo.trackAgentResultViewInternal(
                "find me a red shirt",
                "result-123",
                150,
                items
        ).test()
        observer.assertComplete()
        val request = awaitRequest()

        val requestBody = getRequestBody(request)
        assert(requestBody["items"]!!.contains("item_id:item-100"))
        assert(!requestBody["items"]!!.contains("item_id:item-101"))
    }

    @Test
    fun trackAgentResultView500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultViewInternal("find me a red shirt", "result-123", 5).test()
        observer.assertError { true }
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_search_result_view"))
    }

    @Test
    fun trackAgentResultViewTimeout() {
        val mockResponse = MockResponse()
                .setResponseCode(500)
                .setBody("Internal server error")
                .setBodyDelay(5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentResultViewInternal("find me a red shirt", "result-123", 5).test()
        observer.awaitDone(6, TimeUnit.SECONDS)
        observer.assertError(SocketTimeoutException::class.java)
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_search_result_view"))
    }

    // --- trackAgentSearchSubmit ---

    @Test
    fun trackAgentSearchSubmit() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentSearchSubmitInternal(
                "find me a red shirt",
                "red shirt",
                "result-123",
                intentResultId = "intent-123"
        ).test()
        observer.assertComplete()
        val request = awaitRequest()
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_search_submit"))

        val requestBody = getRequestBody(request)
        assertEquals("find me a red shirt", requestBody["intent"])
        assertEquals("red shirt", requestBody["search_term"])
        assertEquals("result-123", requestBody["search_result_id"])
        assertEquals("intent-123", requestBody["intent_result_id"])
        assertEquals("true", requestBody["beacon"])
        assertEquals("Products", requestBody["section"])
    }

    @Test
    fun trackAgentSearchSubmitWithSection() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentSearchSubmitInternal(
                "find me a red shirt",
                "red shirt",
                "result-123",
                sectionName = "Search Suggestions"
        ).test()
        observer.assertComplete()
        val request = awaitRequest()

        val requestBody = getRequestBody(request)
        assertEquals("Search Suggestions", requestBody["section"])
    }

    @Test
    fun trackAgentSearchSubmit500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentSearchSubmitInternal("find me a red shirt", "red shirt", "result-123").test()
        observer.assertError { true }
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_search_submit"))
    }

    @Test
    fun trackAgentSearchSubmitTimeout() {
        val mockResponse = MockResponse()
                .setResponseCode(500)
                .setBody("Internal server error")
                .setBodyDelay(5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentSearchSubmitInternal("find me a red shirt", "red shirt", "result-123").test()
        observer.awaitDone(6, TimeUnit.SECONDS)
        observer.assertError(SocketTimeoutException::class.java)
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/behavioral_action/assistant_search_submit"))
    }

    // --- Analytics tags ---

    @Test
    fun trackAgentSubmitWithAnalyticsTags() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackAgentSubmitInternal("find me a red shirt", analyticsTags = mapOf("test" to "test1", "appVersion" to "150")).test()
        observer.assertComplete()
        val request = awaitRequest()

        val requestBody = getRequestBody(request)
        assertEquals("{appVersion:150,appPlatform:Android,test:test1}", requestBody["analytics_tags"])
    }

    private fun awaitRequest(): RecordedRequest {
        return mockServer.takeRequest(1, TimeUnit.SECONDS)
                ?: throw AssertionError("Expected request but timed out")
    }
}
