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
import kotlin.test.assertTrue

class ConstructorIoResultsImpressionViewTrackingTest {

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

    @Test
    fun trackResultsImpressionView() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val items = listOf(
                ResultsImpressionItem("item-1", "Item One"),
                ResultsImpressionItem("item-2", "Item Two", variationId = "var-2")
        )
        val observer = ConstructorIo.trackResultsImpressionViewInternal(items).test()
        observer.assertComplete()
        val request = awaitRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path!!.startsWith("/v2/behavioral_action/impression_view"))

        val requestBody = getRequestBody(request)
        assertEquals("true", requestBody["beacon"])
        assertTrue(requestBody["items"]!!.contains("item_id:item-1"))
        assertTrue(requestBody["items"]!!.contains("item_name:Item One"))
        assertTrue(requestBody["items"]!!.contains("item_id:item-2"))
        assertTrue(requestBody["items"]!!.contains("variation_id:var-2"))
    }

    @Test
    fun trackResultsImpressionViewWithSearchTerm() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val items = listOf(ResultsImpressionItem("item-1", "Item One", slCampaignId = "camp-1", slCampaignOwner = "owner-a"))
        val observer = ConstructorIo.trackResultsImpressionViewInternal(items, searchTerm = "shoes").test()
        observer.assertComplete()
        val request = awaitRequest()

        val requestBody = getRequestBody(request)
        assertEquals("shoes", requestBody["search_term"])
        assertTrue(requestBody["items"]!!.contains("sl_campaign_id:camp-1"))
        assertTrue(requestBody["items"]!!.contains("sl_campaign_owner:owner-a"))
    }

    @Test
    fun trackResultsImpressionViewWithFilters() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val items = listOf(ResultsImpressionItem("item-1", "Item One"))
        val observer = ConstructorIo.trackResultsImpressionViewInternal(items, filterName = "category_id", filterValue = "shoes-123").test()
        observer.assertComplete()
        val request = awaitRequest()

        val requestBody = getRequestBody(request)
        assertEquals("category_id", requestBody["filter_name"])
        assertEquals("shoes-123", requestBody["filter_value"])
    }

    @Test
    fun trackResultsImpressionView500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)

        val items = listOf(ResultsImpressionItem("item-1", "Item One"))
        val observer = ConstructorIo.trackResultsImpressionViewInternal(items).test()
        observer.assertError { true }
        val request = awaitRequest()
        assertTrue(request.path!!.startsWith("/v2/behavioral_action/impression_view"))
    }

    @Test
    fun trackResultsImpressionViewTimeout() {
        val mockResponse = MockResponse()
                .setResponseCode(500)
                .setBody("Internal server error")
                .setBodyDelay(5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)

        val items = listOf(ResultsImpressionItem("item-1", "Item One"))
        val observer = ConstructorIo.trackResultsImpressionViewInternal(items).test()
        observer.awaitDone(6, TimeUnit.SECONDS)
        observer.assertError(SocketTimeoutException::class.java)
        val request = awaitRequest()
        assertTrue(request.path!!.startsWith("/v2/behavioral_action/impression_view"))
    }

    private fun awaitRequest(): RecordedRequest {
        return mockServer.takeRequest(1, TimeUnit.SECONDS)
                ?: throw AssertionError("Expected request but timed out")
    }
}
