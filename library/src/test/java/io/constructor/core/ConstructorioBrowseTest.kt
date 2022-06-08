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

class ConstructorIoBrowseTest {

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
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder, ctx)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getBrowseResults() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults("group_id", "Beverages").test()
        observer.assertComplete().assertValue {
            it.get()!!.response?.results!!.size == 24
            it.get()!!.response?.results!![0].value == "Crystal Geyser Natural Alpine Spring Water - 1 Gallon"
            it.get()!!.response?.results!![0].data.id == "108200440"
            it.get()!!.response?.results!![0].data.imageUrl == "https://d17bbgoo3npfov.cloudfront.net/images/farmstand-108200440.png"
            it.get()!!.response?.results!![0].data.metadata?.get("price") == 1.25
            it.get()!!.response?.facets!!.size == 3
            it.get()!!.response?.facets!![0].displayName == "Brand"
            it.get()!!.response?.facets!![0].type == "multiple"
            it.get()!!.response?.groups!!.size == 1
            it.get()!!.response?.resultCount == 367
        }
        val request = mockServer.takeRequest()
        val path = "/browse/group_id/Beverages?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.14.2&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults("group_id", "Beverages").test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
        val path = "/browse/group_id/Beverages?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.14.2&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults("group_id", "Beverages").test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
        val path = "/browse/group_id/Beverages?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.14.2&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("browse_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults("group_id", "Beverages").test()
        observer.assertComplete().assertValue {
            it.get()!!.response?.results!!.isEmpty()
            it.get()!!.response?.facets!!.isEmpty()
            it.get()!!.response?.groups!!.isEmpty()
            it.get()!!.response?.resultCount == 0
        }
        val request = mockServer.takeRequest()
        val path = "/browse/group_id/Beverages?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.14.2&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithSection() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults("group_id", "Beverages", null, null, null, null, null, null, "Sold Out").test()
        val request = mockServer.takeRequest()
        val path = "/browse/group_id/Beverages?section=Sold%20Out&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.14.2&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithFacets() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val facets = listOf(Pair("Brand", listOf("Signature Farms", "Del Monte")), Pair("Nutrition", listOf("Organic")))
        val observer = constructorIo.getBrowseResults("group_id", "Beverages", facets, null, null , null, null, null, null).test()
        val request = mockServer.takeRequest()
        val path = "/browse/group_id/Beverages?filters%5BBrand%5D=Signature%20Farms&filters%5BBrand%5D=Del%20Monte&filters%5BNutrition%5D=Organic&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.14.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithHiddenFields() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults("group_id", "Beverages", null, null, null, null, null, null, null, listOf("hiddenField1", "hiddenField2")).test()
        val request = mockServer.takeRequest()
        val path = "/browse/group_id/Beverages?fmt_options%5Bhidden_fields%5D=hiddenField1&fmt_options%5Bhidden_fields%5D=hiddenField2&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.14.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithHiddenFacets() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val hiddenFacets = listOf("Brand", "price_US")
        val observer = constructorIo.getBrowseResults("group_id", "Beverages", null, null, null, null, null, null, null, null, hiddenFacets).test()
        val request = mockServer.takeRequest()
        val path = "/browse/group_id/Beverages?fmt_options%5Bhidden_facets%5D=Brand&fmt_options%5Bhidden_facets%5D=price_US&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.14.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithCollection() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults("collection_id", "test-collection").test()
        val request = mockServer.takeRequest()
        val path = "/browse/collection_id/test-collection?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.14.2&_dt="
        assert(request.path!!.startsWith(path))
    }
}