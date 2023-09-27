package io.constructor.core

import android.content.Context
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
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getBrowseResults() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults("group_id", "Beverages").test()
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
            "/browse/group_id/Beverages?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt"
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
        val path =
            "/browse/group_id/Beverages?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults("group_id", "Beverages").test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
        val path =
            "/browse/group_id/Beverages?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults("group_id", "Beverages").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.response?.results!!.isEmpty())
        assertTrue(browseResponse?.response?.facets!!.isEmpty())
        assertTrue(browseResponse?.response?.groups!!.isEmpty())
        assertTrue(browseResponse?.response?.resultCount == 0)

        val request = mockServer.takeRequest()
        val path =
            "/browse/group_id/Beverages?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithSection() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults(
            "group_id",
            "Beverages",
            null,
            null,
            null,
            null,
            null,
            null,
            "Sold Out"
        ).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/group_id/Beverages?section=Sold%20Out&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithFacets() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val facets = listOf(
            Pair("Brand", listOf("Signature Farms", "Del Monte")),
            Pair("Nutrition", listOf("Organic"))
        )
        val observer = constructorIo.getBrowseResults(
            "group_id",
            "Beverages",
            facets,
            null,
            null,
            null,
            null,
            null,
            null
        ).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/group_id/Beverages?filters%5BBrand%5D=Signature%20Farms&filters%5BBrand%5D=Del%20Monte&filters%5BNutrition%5D=Organic&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithHiddenFields() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults(
            "group_id",
            "Beverages",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            listOf("hiddenField1", "hiddenField2")
        ).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/group_id/Beverages?fmt_options%5Bhidden_fields%5D=hiddenField1&fmt_options%5Bhidden_fields%5D=hiddenField2&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithHiddenFacets() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val hiddenFacets = listOf("Brand", "price_US")
        val observer = constructorIo.getBrowseResults(
            "group_id",
            "Beverages",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            hiddenFacets
        ).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/group_id/Beverages?fmt_options%5Bhidden_facets%5D=Brand&fmt_options%5Bhidden_facets%5D=price_US&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithCollection() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults("collection_id", "test-collection").test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/collection_id/test-collection?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultWithGroupsSort() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseResults(
            filterName = "group_id",
            filterValue = "Beverages",
            groupsSortBy = "value",
            groupsSortOrder = "ascending"
        ).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/group_id/Beverages?fmt_options%5Bgroups_sort_by%5D=value&fmt_options%5Bgroups_sort_order%5D=ascending&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultWithPreFilterExpression() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val preFilterExpression = """{"and":[{"name":"Country","value":"US"}]}"""
        val observer = constructorIo.getBrowseResults(
            filterName = "group_id",
            filterValue = "Beverages",
            preFilterExpression = preFilterExpression
        ).test()
        val request = mockServer.takeRequest()
        val path = "/browse/group_id/Beverages?pre_filter_expression=%7B%22and%22%3A%5B%7B%22name%22%3A%22Country%22%2C%22value%22%3A%22US%22%7D%5D%7D&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithFiltersUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val filters = mapOf(
            "Brand" to listOf("Signature Farms", "Del Monte"),
            "Nutrition" to listOf("Organic")
        )
        val browseRequest = BrowseRequest.Builder("group_id", "Beverages")
            .setFilters(filters)
            .build()
        val observer = constructorIo.getBrowseResults(browseRequest).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/group_id/Beverages?filters%5BBrand%5D=Signature%20Farms&filters%5BBrand%5D=Del%20Monte&filters%5BNutrition%5D=Organic&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithGroupsSortUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val browseRequest = BrowseRequest.Builder("group_id", "Beverages")
            .setGroupsSortBy("value")
            .setGroupsSortOrder("ascending")
            .build()
        val observer = constructorIo.getBrowseResults(browseRequest).test()
        val request = mockServer.takeRequest()
        val path =
            "/browse/group_id/Beverages?fmt_options%5Bgroups_sort_by%5D=value&fmt_options%5Bgroups_sort_order%5D=ascending&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.25.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseResultsWithVariationsMapsUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val variationsMap = VariationsMap(
            dtype = "array",
            values = mapOf(
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            ),
            groupBy = listOf(mapOf("name" to "Country", "field" to "data.facets.Country")),
            filterBy = """{"and":[{"field":"data.brand","value":"Best Brand"}]}"""
        )
        val browseRequest = BrowseRequest.Builder("group_id", "Beverages")
            .setVariationsMap(variationsMap)
            .build()
        val observer = constructorIo.getBrowseResults(browseRequest).test()
        val request = mockServer.takeRequest()
        assertThat(request.requestUrl!!.encodedPath).isEqualTo("/browse/group_id/Beverages")
        with(request.requestUrl!!) {
            val queryParams = mapOf(
                "variations_map" to """{"dtype":"array","values":{"Price":{"aggregation":"min","field":"data.facets.price"},"Country":{"aggregation":"all","field":"data.facets.country"}},"group_by":[{"name":"Country","field":"data.facets.Country"}],"filter_by":{"and":[{"field":"data.brand","value":"Best Brand"}]}}""",
                "key" to "silver-key",
                "i" to "guapo-the-guid",
                "ui" to "player-two",
                "s" to "92",
                "c" to "cioand-2.25.2",
                "_dt" to "1"
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

    @Test
    fun getBrowseResultsWithPreFilterExpressionUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("browse_response.json"))
        mockServer.enqueue(mockResponse)
        val preFilterExpression = """{"and":[{"name":"Country","value":"US"}]}"""
        val browseRequest = BrowseRequest.Builder("group_id", "Beverages")
            .setPreFilterExpression(preFilterExpression)
            .build()
        val observer = constructorIo.getBrowseResults(browseRequest).test()
        val request = mockServer.takeRequest()
        assertThat(request.requestUrl!!.encodedPath).isEqualTo("/browse/group_id/Beverages")
        with(request.requestUrl!!) {
            val queryParams = mapOf(
                "pre_filter_expression" to preFilterExpression,
                "key" to "silver-key",
                "i" to "guapo-the-guid",
                "ui" to "player-two",
                "s" to "92",
                "c" to "cioand-2.25.2",
                "_dt" to "1"
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
