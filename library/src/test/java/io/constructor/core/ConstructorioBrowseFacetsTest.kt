package io.constructor.core

import android.content.Context
import io.constructor.data.builder.BrowseFacetsRequest
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

class ConstructorioBrowseFacetsTest {

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
    fun getBrowseFacetsResults() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_facets.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseFacets().test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertEquals(browseResponse?.response?.facets!!.size, 5)
        assertEquals(browseResponse?.response?.facets!![0].displayName, "Brand")
        assertEquals(browseResponse?.response?.facets!![0].type, "multiple")
        assertEquals(browseResponse?.response?.facets!![0].hidden, false)
        assertEquals(browseResponse?.response?.resultCount, 9)

        val request = mockServer.takeRequest()
        val path =
                "/browse/facets?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.34.0"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseFacetsResultsWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseFacets().test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
    }

    @Test
    fun getBrowseFacetsResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_facets.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseFacets().test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
    }

    @Test
    fun getBrowseFacetsResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_facets_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseFacets().test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.response?.facets!!.isEmpty())
        assertTrue(browseResponse?.response?.resultCount == 9)

        val request = mockServer.takeRequest()
    }

    @Test
    fun getBrowseFacetsResultsWithOptionalParameters() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_facets_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseFacets( 5, 20, 10, true).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val request = mockServer.takeRequest()
        val path =
                "/browse/facets?page=5&offset=10&num_results_per_page=20&fmt_options%5Bshow_hidden_facets%5D=true&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.34.0"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseFacetsResultsUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_facets.json"))
        mockServer.enqueue(mockResponse)
        val browseFacetsRequest = BrowseFacetsRequest.Builder()
                .setPage(5)
                .setNumResultsPerPage(20)
                .setOffset(10)
                .setShowHiddenFacets(true)
                .build()
        val observer = constructorIo.getBrowseFacets(browseFacetsRequest).test()
        val request = mockServer.takeRequest()
        val path =
                "/browse/facets?page=5&offset=10&num_results_per_page=20&fmt_options%5Bshow_hidden_facets%5D=true&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.34.0"
        assert(request.path!!.startsWith(path))
    }
}
