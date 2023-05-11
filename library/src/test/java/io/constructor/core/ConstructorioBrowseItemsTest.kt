package io.constructor.core

import android.content.Context
import io.constructor.data.builder.BrowseItemsRequest
import io.constructor.data.builder.BrowseRequest
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.common.VariationsMap
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.TestDataLoader
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstructorioBrowseItemsTest {

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
    fun getBrowseItemsResults() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseItemsResults(listOf("123", "234", "234", "456", "678")).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertEquals(browseResponse?.response?.results?.size, 5)
        assertEquals(browseResponse?.response?.results!![0].value, "Crystal Geyser Natural Alpine Spring Water - 1 Gallon")
        assertEquals(browseResponse?.response?.results!![0].data.id, "108200440")
        assertEquals(browseResponse?.response?.results!![0].data.imageUrl, "https://d17bbgoo3npfov.cloudfront.net/images/farmstand-108200440.png")
        assertEquals(browseResponse?.response?.results!![0].data.metadata?.get("price"), 1.25)
        assertEquals(browseResponse?.response?.facets!!.size, 3)
        assertEquals(browseResponse?.response?.facets!![0].displayName, "Brand")
        assertEquals(browseResponse?.response?.facets!![0].type, "multiple")
        assertEquals(browseResponse?.response?.groups!!.size, 1)
        assertEquals(browseResponse?.response?.resultCount, 367)

        val request = mockServer.takeRequest()
        val path =
                "/browse/items?ids=123&ids=234&ids=234&ids=456&ids=678&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.19.4&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseItemsResults(listOf("123", "234", "234", "456", "678")).test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
        val path =
                "/browse/items?ids=123&ids=234&ids=234&ids=456&ids=678&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.19.4&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseItemsResults(listOf("123", "234", "234", "456", "678")).test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
        val path =
                "/browse/items?ids=123&ids=234&ids=234&ids=456&ids=678&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.19.4&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseItemsResults(listOf("123", "234", "234", "456", "678")).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.response?.results!!.isEmpty())
        assertTrue(browseResponse?.response?.facets!!.isEmpty())
        assertTrue(browseResponse?.response?.groups!!.isEmpty())
        assertTrue(browseResponse?.response?.resultCount == 0)

        val request = mockServer.takeRequest()
        val path =
                "/browse/items?ids=123&ids=234&ids=234&ids=456&ids=678&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.19.4&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseItemResultsWithOptionalParameters() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val facets = listOf(
                Pair("Brand", listOf("Signature Farms", "Del Monte")),
                Pair("Nutrition", listOf("Organic"))
        )
        val variationsMap = VariationsMap(
                dtype = "array",
                values = mapOf(
                        "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                        "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
                ),
                groupBy = listOf(mapOf("name" to "Country", "field" to "data.facets.Country"))
        )
        val observer = constructorIo.getBrowseItemsResults(listOf("123", "234", "234", "456", "678"), facets, 3, 40, "relevance", "descending", "Items", listOf("hiddenField1", "hiddenField2"), listOf("hiddenFacet1", "hiddenFacet2"), "value", "ascending", variationsMap ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val request = mockServer.takeRequest()
        val path =
                "/browse/items?filters%5BBrand%5D=Signature%20Farms&filters%5BBrand%5D=Del%20Monte&filters%5BNutrition%5D=Organic&fmt_options%5Bhidden_fields%5D=hiddenField1&fmt_options%5Bhidden_fields%5D=hiddenField2&variations_map=%7B%22dtype%22%3A%22array%22%2C%22values%22%3A%7B%22Price%22%3A%7B%22aggregation%22%3A%22min%22%2C%22field%22%3A%22data.facets.price%22%7D%2C%22Country%22%3A%7B%22aggregation%22%3A%22all%22%2C%22field%22%3A%22data.facets.country%22%7D%7D%2C%22group_by%22%3A%5B%7B%22name%22%3A%22Country%22%2C%22field%22%3A%22data.facets.Country%22%7D%5D%7D&page=3&num_results_per_page=40&sort_by=relevance&sort_order=descending&section=Items&fmt_options%5Bhidden_facets%5D=hiddenFacet1&fmt_options%5Bhidden_facets%5D=hiddenFacet2&fmt_options%5Bgroups_sort_by%5D=value&fmt_options%5Bgroups_sort_order%5D=ascending&ids=123&ids=234&ids=234&ids=456&ids=678&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.19.4&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseItemResultsUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val filters = mapOf(
                "Brand" to listOf("Signature Farms", "Del Monte"),
                "Nutrition" to listOf("Organic")
        )
        val variationsMap = VariationsMap(
                dtype = "array",
                values = mapOf(
                        "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                        "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
                ),
                groupBy = listOf(mapOf("name" to "Country", "field" to "data.facets.Country"))
        )
        val browseItemsRequest = BrowseItemsRequest.Builder(listOf("123", "234", "234", "456", "678"))
                .setFilters(filters)
                .setGroupsSortBy("value")
                .setGroupsSortOrder("ascending")
                .setSection("Items")
                .setSortOrder("descending")
                .setSortBy("relevance")
                .setVariationsMap(variationsMap)
                .setHiddenFacets(listOf("hiddenFacet1", "hiddenFacet2"))
                .setHiddenFields(listOf("hiddenField1", "hiddenField2"))
                .setPage(3)
                .setPerPage(40)
                .build()
        val observer = constructorIo.getBrowseItemsResults(browseItemsRequest).test()
        val request = mockServer.takeRequest()
        val path =
                "/browse/items?filters%5BBrand%5D=Signature%20Farms&filters%5BBrand%5D=Del%20Monte&filters%5BNutrition%5D=Organic&fmt_options%5Bhidden_fields%5D=hiddenField1&fmt_options%5Bhidden_fields%5D=hiddenField2&variations_map=%7B%22dtype%22%3A%22array%22%2C%22values%22%3A%7B%22Price%22%3A%7B%22aggregation%22%3A%22min%22%2C%22field%22%3A%22data.facets.price%22%7D%2C%22Country%22%3A%7B%22aggregation%22%3A%22all%22%2C%22field%22%3A%22data.facets.country%22%7D%7D%2C%22group_by%22%3A%5B%7B%22name%22%3A%22Country%22%2C%22field%22%3A%22data.facets.Country%22%7D%5D%7D&page=3&num_results_per_page=40&sort_by=relevance&sort_order=descending&section=Items&fmt_options%5Bhidden_facets%5D=hiddenFacet1&fmt_options%5Bhidden_facets%5D=hiddenFacet2&fmt_options%5Bgroups_sort_by%5D=value&fmt_options%5Bgroups_sort_order%5D=ascending&ids=123&ids=234&ids=234&ids=456&ids=678&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.19.4&_dt="
        assert(request.path!!.startsWith(path))
    }
}
