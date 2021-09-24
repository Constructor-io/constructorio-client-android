package io.constructor.core

import android.content.Context
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.TestDataLoader
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class ConstructorIoRecommendationsTest {

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

        every { preferencesHelper.apiKey } returns "golden-key"
        every { preferencesHelper.id } returns "guido-the-guid"
        every { preferencesHelper.serviceUrl } returns mockServer.hostName
        every { preferencesHelper.port } returns mockServer.port
        every { preferencesHelper.scheme } returns "http"
        every { preferencesHelper.defaultItemSection } returns "Products"
        every { preferencesHelper.getSessionId(any(), any()) } returns 79

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.userId } returns "player-one"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("dummyKey")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder, ctx)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getRecommendationResults() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getRecommendationResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.response?.results!!.size == 24
            it.get()!!.response?.results!![0].value == "LaCroix Sparkling Water Pure Cans - 12-12 Fl. Oz."
            it.get()!!.response?.results!![0].data.id == "960189161"
            it.get()!!.response?.results!![0].data.imageUrl == "https://d17bbgoo3npfov.cloudfront.net/images/farmstand-960189161.png"
            it.get()!!.response?.results!![0].data.metadata?.get("price") == 1.11
            it.get()!!.response?.resultCount == 225
        }
        val request = mockServer.takeRequest()
        val path = "/recommendations/v1/pods/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.9.1&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun getRecommendationResultsWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getRecommendationResults("titanic").test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
        val path = "/recommendations/v1/pods/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.9.1&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun getRecommendationResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getRecommendationResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
        val path = "/recommendations/v1/pods/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.9.1&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun getRecommendationResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getRecommendationResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.response?.results!!.isEmpty()
            it.get()!!.response?.resultCount == 0
        }
        val request = mockServer.takeRequest()
        val path = "/recommendations/v1/pods/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.9.1&_dt="
        assert(request.path.startsWith(path))
    }
}
