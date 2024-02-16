package io.constructor.core

import android.content.Context
import io.constructor.data.builder.BrowseFacetOptionsRequest
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstructorioBrowseFacetOptionsTest {

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

        every { preferencesHelper.apiKey } returns "silver-key"
        every { preferencesHelper.id } returns "guapo-the-guid"
        every { preferencesHelper.serviceUrl } returns mockServer.hostName
        every { preferencesHelper.port } returns mockServer.port
        every { preferencesHelper.scheme } returns "http"
        every { preferencesHelper.defaultItemSection } returns "Products"
        every { preferencesHelper.getSessionId(any(), any()) } returns 92

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.userId } returns "player-two"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("dummyKey")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getBrowseFacetOptionsResults() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_facet_options.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseFacetOptions("brands").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertEquals(browseResponse?.response?.facets!!.size, 1)
        assertEquals(browseResponse?.response?.facets!![0].displayName, "Brand")
        assertEquals(browseResponse?.response?.facets!![0].type, "multiple")
        assertEquals(browseResponse?.response?.facets!![0].hidden, false)
        assertEquals(browseResponse?.response?.facets!![0].options?.size, 3)
        assertEquals(browseResponse?.response?.facets!![0].options!![0].displayName, "Brand1")
        assertEquals(browseResponse?.response?.facets!![0].options!![0].count, 2460)
        assertEquals(browseResponse?.response?.facets!![0].options!![0].value, "brand1")

        val request = mockServer.takeRequest()
        val path =
                "/browse/facet_options?facet_name=brands&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.28.0"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseFacetsResultsWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseFacetOptions("Brand").test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
    }

    @Test
    fun getBrowseFacetsResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_facet_options.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseFacetOptions("Brand").test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
    }

    @Test
    fun getBrowseFacetsResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_facet_options_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseFacetOptions("Brand").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.response?.facets!!.isEmpty())

        val request = mockServer.takeRequest()
    }

    @Test
    fun getBrowseFacetOptionsResultsWithOptionalParameters() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_facet_options.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseFacetOptions("Brands", true).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val request = mockServer.takeRequest()
        val path =
                "/browse/facet_options?fmt_options%5Bshow_hidden_facets%5D=true&facet_name=Brands&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.28.0"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseFacetsResultsUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_facets.json"))
        mockServer.enqueue(mockResponse)
        val browseFacetOptionsRequest = BrowseFacetOptionsRequest.Builder("Brands")
                .setShowHiddenFacets(true)
                .build()
        val observer = constructorIo.getBrowseFacetOptions(browseFacetOptionsRequest).test()
        val request = mockServer.takeRequest()
        val path =
                "/browse/facet_options?fmt_options%5Bshow_hidden_facets%5D=true&facet_name=Brands&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.28.0"
        assert(request.path!!.startsWith(path))
    }
}
