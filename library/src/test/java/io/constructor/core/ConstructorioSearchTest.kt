package io.constructor.core

import android.content.Context
import io.constructor.data.builder.SearchRequest
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

class ConstructorIoSearchTest {

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
    fun getSearchResults() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getSearchResults("corn").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertEquals(searchResponse?.response?.results?.size, 24)
        assertEquals(searchResponse?.response?.results!![0].value, "Del Monte Fresh Cut Corn Whole Kernel Golden Sweet with Natural Sea Salt - 15.25 Oz")
        assertEquals(searchResponse?.response?.results!![0].data.id, "121150086")
        assertEquals(searchResponse?.response?.results!![0].data.imageUrl, "https://d17bbgoo3npfov.cloudfront.net/images/farmstand-121150086.png")
        assertEquals(searchResponse?.response?.results!![0].data.metadata?.get("price"), 2.29)
        assertEquals(searchResponse?.response?.results!![0].matchedTerms!![0], "corn")
        assertEquals(searchResponse?.response?.facets!!.size, 3)
        assertEquals(searchResponse?.response?.facets!![0].displayName, "Brand")
        assertEquals(searchResponse?.response?.facets!![0].type, "multiple")
        assertEquals(searchResponse?.response?.groups!!.size, 1)
        assertEquals(searchResponse?.response?.resultCount, 225)

        val request = mockServer.takeRequest()
        val path =
            "/search/corn?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getSearchResults("corn").test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
        val path =
            "/search/corn?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getSearchResults("corn").test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
        val path =
            "/search/corn?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getSearchResults("corn").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.response?.results!!.isEmpty())
        assertTrue(searchResponse?.response?.results!!.isEmpty())
        assertTrue(searchResponse?.response?.results!!.isEmpty())
        assertEquals(searchResponse?.response?.resultCount, 0)

        val request = mockServer.takeRequest()
        val path =
            "/search/corn?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithRedirect() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response_redirect.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getSearchResults("bbq").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertEquals(searchResponse?.response?.redirect?.data?.url, "/mccormicks_farmstand.html?barbeque-and-grilling=1")
        assertEquals(searchResponse?.response?.redirect?.matchedTerms!![0], "bbq")

        val request = mockServer.takeRequest()
        val path =
            "/search/bbq?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithSection() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val observer =
            constructorIo.getSearchResults("bbq", null, null, null, null, null, null, "Sold Out")
                .test()
        val request = mockServer.takeRequest()
        val path =
            "/search/bbq?section=Sold%20Out&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithFacets() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val facets = listOf(
            Pair("Brand", listOf("Signature Farms", "Del Monte")),
            Pair("Nutrition", listOf("Organic"))
        )
        val observer =
            constructorIo.getSearchResults("bbq", facets, null, null, null, null, null, null).test()
        val request = mockServer.takeRequest()
        val path =
            "/search/bbq?filters%5BBrand%5D=Signature%20Farms&filters%5BBrand%5D=Del%20Monte&filters%5BNutrition%5D=Organic&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithHiddenFields() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getSearchResults(
            "bbq",
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
            "/search/bbq?fmt_options%5Bhidden_fields%5D=hiddenField1&fmt_options%5Bhidden_fields%5D=hiddenField2&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithHiddenFacets() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val hiddenFacets = listOf("Brand", "price_US")
        val observer = constructorIo.getSearchResults(
            "bbq",
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
            "/search/bbq?fmt_options%5Bhidden_facets%5D=Brand&fmt_options%5Bhidden_facets%5D=price_US&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithProperUrlEncoding() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getSearchResults("2% cheese").test()
        val request = mockServer.takeRequest()
        val path =
            "/search/2%25%20cheese?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithGroupsSort() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getSearchResults(
            term = "bbq",
            groupsSortBy = "value",
            groupsSortOrder = "descending"
        ).test()
        val request = mockServer.takeRequest()
        val path =
            "/search/bbq?fmt_options%5Bgroups_sort_by%5D=value&fmt_options%5Bgroups_sort_order%5D=descending&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithFiltersUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val filters = mapOf(
            "Brand" to listOf("Signature Farms", "Del Monte"),
            "Nutrition" to listOf("Organic")
        )
        val searchRequest = SearchRequest.Builder("bbq")
            .setFilters(filters)
            .build()
        val observer = constructorIo.getSearchResults(searchRequest).test()
        val request = mockServer.takeRequest()
        val path =
            "/search/bbq?filters%5BBrand%5D=Signature%20Farms&filters%5BBrand%5D=Del%20Monte&filters%5BNutrition%5D=Organic&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithGroupsSortUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val searchRequest = SearchRequest.Builder("bbq")
            .setGroupsSortBy("value")
            .setGroupsSortOrder("ascending")
            .build()
        val observer = constructorIo.getSearchResults(searchRequest).test()
        val request = mockServer.takeRequest()
        val path =
            "/search/bbq?fmt_options%5Bgroups_sort_by%5D=value&fmt_options%5Bgroups_sort_order%5D=ascending&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.20.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getSearchResultsWithVariationsMapUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val variationsMap = VariationsMap(
            dtype = "array",
            values = mapOf(
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            ),
            groupBy = listOf(mapOf("name" to "Country", "field" to "data.facets.Country"))
        )
        val searchRequest = SearchRequest.Builder("bbq")
            .setVariationsMap(variationsMap)
            .build()
        val observer = constructorIo.getSearchResults(searchRequest).test()
        val request = mockServer.takeRequest()

        assertThat(request.requestUrl!!.encodedPath).isEqualTo("/search/bbq")
        with(request.requestUrl!!) {
            val queryParams = mapOf(
                "variations_map" to """{"dtype":"array","values":{"Price":{"aggregation":"min","field":"data.facets.price"},"Country":{"aggregation":"all","field":"data.facets.country"}},"group_by":[{"name":"Country","field":"data.facets.Country"}]}""",
                "key" to "silver-key",
                "i" to "guapo-the-guid",
                "ui" to "player-two",
                "s" to "92",
                "c" to "cioand-2.20.0",
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
