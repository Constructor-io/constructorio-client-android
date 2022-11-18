package io.constructor.core

import android.content.Context
import io.constructor.data.builder.AutocompleteRequest
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

class ConstructorIoAutocompleteTest {

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
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getAutocompleteResults() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            var suggestions = it.get()!!.sections?.get("Search Suggestions");
            suggestions?.isNotEmpty()!! && suggestions.size == 5
        }
        val request = mockServer.takeRequest()
        val path =
            "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.19.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithFacetFilter() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val facet = hashMapOf("storeLocation" to listOf("CA"))
        val observer =
            constructorIo.getAutocompleteResults("titanic", facet?.map { it.key to it.value })
                .test()
        observer.assertComplete().assertValue {
            var suggestions = it.get()!!.sections?.get("Search Suggestions");
            suggestions?.isNotEmpty()!! && suggestions.size == 5
        }
        val request = mockServer.takeRequest()
        val path =
            "/autocomplete/titanic?filters%5BstoreLocation%5D=CA&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.19.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithGroupIdFilter() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic", null, 101).test()
        observer.assertComplete().assertValue {
            var suggestions = it.get()!!.sections?.get("Search Suggestions");
            suggestions?.isNotEmpty()!! && suggestions.size == 5
        }
        val request = mockServer.takeRequest()
        val path =
            "/autocomplete/titanic?filters%5Bgroup_id%5D=101&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.19.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
        val path =
            "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.19.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
        val path =
            "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.19.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("autocomplete_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            var suggestions = it.get()!!.sections?.get("Search Suggestions");
            suggestions?.isEmpty()!!
        }
        val request = mockServer.takeRequest()
        val path =
            "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.19.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithHiddenFields() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults(
            "bbq",
            null,
            null,
            listOf("hiddenField1", "hiddenField2")
        ).test()
        val request = mockServer.takeRequest()
        val path =
            "/autocomplete/bbq?fmt_options%5Bhidden_fields%5D=hiddenField1&fmt_options%5Bhidden_fields%5D=hiddenField2&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.19.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithProperUrlEncoding() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("2% cheese").test()
        val request = mockServer.takeRequest()
        val path =
            "/autocomplete/2%25%20cheese?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.19.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithFiltersUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val filters = mapOf(
            "storeLocation" to listOf("CA"),
            "group_id" to listOf("101")
        )
        val autocompleteRequest = AutocompleteRequest.Builder("titanic")
            .setFilters(filters)
            .build()
        val observer = constructorIo.getAutocompleteResults(autocompleteRequest).test()
        observer.assertComplete().assertValue {
            var suggestions = it.get()!!.sections?.get("Search Suggestions");
            suggestions?.isNotEmpty()!! && suggestions.size == 5
        }
        val request = mockServer.takeRequest()
        val path =
            "/autocomplete/titanic?filters%5BstoreLocation%5D=CA&filters%5Bgroup_id%5D=101&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.19.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithNumResultsPerSectionUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val numResultsPerSection = mapOf(
            "Products" to 5,
            "Search Suggestions" to 10,
        )
        val autocompleteRequest = AutocompleteRequest.Builder("titanic")
            .setNumResultsPerSection(numResultsPerSection)
            .build()
        val observer = constructorIo.getAutocompleteResults(autocompleteRequest).test()
        observer.assertComplete().assertValue {
            var suggestions = it.get()!!.sections?.get("Search Suggestions");
            suggestions?.isNotEmpty()!! && suggestions.size == 5
        }
        val request = mockServer.takeRequest()
        val path =
            "/autocomplete/titanic?num_results_Products=5&num_results_Search%20Suggestions=10&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.19.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithVariationsMapUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
            .setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val variationsMap = VariationsMap(
            dtype = "array",
            values = mapOf(
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            ),
            groupBy = listOf(mapOf("name" to "Country", "field" to "data.facets.Country"))
        )
        val autocompleteRequest = AutocompleteRequest.Builder("titanic")
            .setVariationsMap(variationsMap)
            .build()
        val observer = constructorIo.getAutocompleteResults(autocompleteRequest).test()
        observer.assertComplete().assertValue {
            var suggestions = it.get()!!.sections?.get("Search Suggestions");
            suggestions?.isNotEmpty()!! && suggestions.size == 5
        }
        val request = mockServer.takeRequest()
        assertThat(request.requestUrl!!.encodedPath).isEqualTo("/autocomplete/titanic")
        with(request.requestUrl!!) {
            val queryParams = mapOf(
                "variations_map" to """{"dtype":"array","values":{"Price":{"aggregation":"min","field":"data.facets.price"},"Country":{"aggregation":"all","field":"data.facets.country"}},"group_by":[{"name":"Country","field":"data.facets.Country"}]}""",
                "key" to "golden-key",
                "i" to "guido-the-guid",
                "ui" to "player-one",
                "s" to "79",
                "c" to "cioand-2.19.0",
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
