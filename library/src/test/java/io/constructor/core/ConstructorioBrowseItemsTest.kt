package io.constructor.core

import android.content.Context
import io.constructor.data.builder.BrowseItemsRequest
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

class ConstructorIoBrowseItemsTest {

    @Rule
    @JvmField
    val overrideSchedulersRule = RxSchedulersOverrideRule()

    private lateinit var mockServer: MockWebServer
    private var constructorIo = ConstructorIo
    private val ctx = mockk<Context>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()
    private val ids = listOf("10001", "dai_pid_2003597")

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
        val browseItemsRequest = BrowseItemsRequest.Builder(ids)
            .build()
        val observer = constructorIo.getBrowseItemsResults(browseItemsRequest).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertEquals(browseResponse?.response?.results?.size, 5)
        assertEquals(browseResponse?.response?.results!![0].value, "Crystal Geyser Natural Alpine Spring Water - 1 Gallon")
        assertEquals(browseResponse.response?.results!![0].data.id, "108200440")
        assertEquals(browseResponse.response?.results!![0].data.imageUrl, "https://d17bbgoo3npfov.cloudfront.net/images/farmstand-108200440.png")
        assertEquals(browseResponse.response?.results!![0].data.metadata?.get("price"), 1.25)
        assertEquals(browseResponse.response?.facets!!.size, 3)
        assertEquals(browseResponse.response?.facets!![0].displayName, "Brand")
        assertEquals(browseResponse.response?.facets!![0].type, "multiple")
        assertEquals(browseResponse.response?.groups!!.size, 1)
        assertEquals(browseResponse.response?.resultCount, 367)

        val request = mockServer.takeRequest()
        val path =
            "/browse/items?ids=10001&ids=dai_pid_2003597&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.21.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseItemsResultsWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val browseItemsRequest = BrowseItemsRequest.Builder(ids)
            .build()
        val observer = constructorIo.getBrowseItemsResults(browseItemsRequest).test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
        val path =
            "/browse/items?ids=10001&ids=dai_pid_2003597&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.21.0&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseItemsResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val browseItemsRequest = BrowseItemsRequest.Builder(ids)
            .build()
        val observer = constructorIo.getBrowseItemsResults(browseItemsRequest).test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
        val path =
            "/browse/items?ids=10001&ids=dai_pid_2003597&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.21.0&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseItemsResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val browseItemsRequest = BrowseItemsRequest.Builder(ids)
            .build()
        val observer = constructorIo.getBrowseItemsResults(browseItemsRequest).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.response?.results!!.isEmpty())
        assertTrue(browseResponse.response?.facets!!.isEmpty())
        assertTrue(browseResponse.response?.groups!!.isEmpty())
        assertTrue(browseResponse.response?.resultCount == 0)

        val request = mockServer.takeRequest()
        val path =
            "/browse/items?ids=10001&ids=dai_pid_2003597&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.21.0&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseItemsResultsWithSection() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val browseItemsRequest = BrowseItemsRequest.Builder(ids)
            .setSection("Sold Out")
            .build()
        val observer = constructorIo.getBrowseItemsResults(browseItemsRequest).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/items?section=Sold%20Out&ids=10001&ids=dai_pid_2003597&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.21.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseItemsResultsWithFacets() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val facets = mapOf(
            "Brand" to listOf("Signature Farms", "Del Monte"),
            "Nutrition" to listOf("Organic")
        )
        val browseItemsRequest = BrowseItemsRequest.Builder(ids)
            .setFilters(facets)
            .build()
        val observer = constructorIo.getBrowseItemsResults(browseItemsRequest).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/items?filters%5BBrand%5D=Signature%20Farms&filters%5BBrand%5D=Del%20Monte&filters%5BNutrition%5D=Organic&ids=10001&ids=dai_pid_2003597&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.21.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseItemsResultsWithHiddenFields() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val browseItemsRequest = BrowseItemsRequest.Builder(ids)
            .setHiddenFields(listOf("hiddenField1", "hiddenField2"))
            .build()
        val observer = constructorIo.getBrowseItemsResults(browseItemsRequest).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/items?fmt_options%5Bhidden_fields%5D=hiddenField1&fmt_options%5Bhidden_fields%5D=hiddenField2&ids=10001&ids=dai_pid_2003597&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.21.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseItemsResultsWithHiddenFacets() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val hiddenFacets = listOf("Brand", "price_US")
        val browseItemsRequest = BrowseItemsRequest.Builder(ids)
            .setHiddenFacets(hiddenFacets)
            .build()
        val observer = constructorIo.getBrowseItemsResults(browseItemsRequest).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/items?fmt_options%5Bhidden_facets%5D=Brand&fmt_options%5Bhidden_facets%5D=price_US&ids=10001&ids=dai_pid_2003597&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.21.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultWithGroupsSort() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val browseItemsRequest = BrowseItemsRequest.Builder(ids)
            .setGroupsSortBy("value")
            .setGroupsSortOrder("ascending")
            .build()
        val observer = constructorIo.getBrowseItemsResults(browseItemsRequest).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/items?fmt_options%5Bgroups_sort_by%5D=value&fmt_options%5Bgroups_sort_order%5D=ascending&ids=10001&ids=dai_pid_2003597&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.21.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseItemsResultsWithVariationsMapsUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val variationsMap = VariationsMap(
            dtype = "array",
            values = mapOf(
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            ),
            groupBy = listOf(mapOf("name" to "Country", "field" to "data.facets.Country"))
        )
        val browseItemsRequest = BrowseItemsRequest.Builder(ids)
            .setVariationsMap(variationsMap)
            .build()
        val observer = constructorIo.getBrowseItemsResults(browseItemsRequest).test()
        val request = mockServer.takeRequest()
        assertThat(request.requestUrl!!.encodedPath).isEqualTo("/browse/items")
        with(request.requestUrl!!) {
            val queryParams = mapOf(
                "variations_map" to """{"dtype":"array","values":{"Price":{"aggregation":"min","field":"data.facets.price"},"Country":{"aggregation":"all","field":"data.facets.country"}},"group_by":[{"name":"Country","field":"data.facets.Country"}]}""",
                "key" to "silver-key",
                "i" to "guapo-the-guid",
                "ui" to "player-two",
                "s" to "92",
                "c" to "cioand-2.21.0",
                "_dt" to "1",
                "ids" to "10001",
            )
            assertThat(queryParameterNames).containsExactlyInAnyOrderElementsOf(queryParams.keys)

            queryParams.forEach { (key, value) ->
                if (key == "_dt") {
                    assertThat(queryParameter(key)).containsOnlyDigits()
                } else {
                    assertThat(queryParameter(key)).isEqualTo(value)
                }
            }
        }
    }
}
