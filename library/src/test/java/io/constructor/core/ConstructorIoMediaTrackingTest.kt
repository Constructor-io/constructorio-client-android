package io.constructor.core

import android.content.Context
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
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

class ConstructorIoMediaTrackingTest {

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
        every { preferencesHelper.mediaServiceUrl } returns mockServer.hostName
        every { preferencesHelper.port } returns mockServer.port
        every { preferencesHelper.scheme } returns "http"
        every { preferencesHelper.defaultItemSection } returns "Products"
        every { preferencesHelper.getSessionId(any(), any()) } returns 67

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.defaultAnalyticsTags } returns mapOf("appVersion" to "123", "appPlatform" to "Android")
        every { configMemoryHolder.userId } returns "player-three"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("dummyKey", mediaServiceUrl = mockServer.hostName)
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun trackMediaImpressionView() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackMediaImpressionViewInternal("test-banner", "home").test()
        observer.assertComplete()
        val request = awaitRequest()
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith("/v2/ad_behavioral_action/display_ad_view"))
        val requestBody = getRequestBody(request)
        assertEquals("test-banner", requestBody["banner_ad_id"])
        assertEquals("home", requestBody["placement_id"])
        assertEquals("true", requestBody["beacon"])
    }

    @Test
    fun trackMediaImpressionClick() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackMediaImpressionClickInternal("test-banner", "home").test()
        observer.assertComplete()
        val request = awaitRequest()
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith("/v2/ad_behavioral_action/display_ad_click"))
        val requestBody = getRequestBody(request)
        assertEquals("test-banner", requestBody["banner_ad_id"])
        assertEquals("home", requestBody["placement_id"])
        assertEquals("true", requestBody["beacon"])
    }

    @Test
    fun trackMediaImpressionView500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackMediaImpressionViewInternal("test-banner", "home").test()
        observer.assertError { true }
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/ad_behavioral_action/display_ad_view"))
    }

    @Test
    fun trackMediaImpressionClick500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackMediaImpressionClickInternal("test-banner", "home").test()
        observer.assertError { true }
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/ad_behavioral_action/display_ad_click"))
    }

    @Test
    fun trackMediaImpressionViewTimeout() {
        val mockResponse = MockResponse()
                .setResponseCode(500)
                .setBody("Internal server error")
                .setBodyDelay(5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackMediaImpressionViewInternal("test-banner", "home").test()
        observer.awaitDone(6, TimeUnit.SECONDS)
        observer.assertError(SocketTimeoutException::class.java)
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/ad_behavioral_action/display_ad_view"))
    }

    @Test
    fun trackMediaImpressionClickTimeout() {
        val mockResponse = MockResponse()
                .setResponseCode(500)
                .setBody("Internal server error")
                .setBodyDelay(5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)

        val observer = ConstructorIo.trackMediaImpressionClickInternal("test-banner", "home").test()
        observer.awaitDone(6, TimeUnit.SECONDS)
        observer.assertError(SocketTimeoutException::class.java)
        val request = awaitRequest()
        assert(request.path!!.startsWith("/v2/ad_behavioral_action/display_ad_click"))
    }

    private fun awaitRequest(): RecordedRequest {
        return mockServer.takeRequest(1, TimeUnit.SECONDS)
                ?: throw AssertionError("Expected request but timed out")
    }
}
