package io.constructor.core

import android.content.Context
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.autocomplete.AutocompleteFacetsConfig
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

        every { configMemoryHolder.userId } returns "player-one"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("dummyKey")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder, ctx)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getAutocompleteResults() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            val suggestions = it.get()!!.sections?.get("Search Suggestions")
            suggestions?.isNotEmpty()!! && suggestions.size == 5
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.13.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithFilters() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val facetsConfig = AutocompleteFacetsConfig(mapOf("storeLocation" to listOf("CA")))
        val observer = constructorIo.getAutocompleteResults("titanic", null, facetsConfig).test()
        observer.assertComplete().assertValue {
            val suggestions = it.get()!!.sections?.get("Search Suggestions")
            suggestions?.isNotEmpty()!! && suggestions.size == 5
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?filters%5BstoreLocation%5D=CA&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.13.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithGroupIdFilter() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        val facetsConfig = AutocompleteFacetsConfig(mapOf("group_id" to listOf("101")))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic", null, facetsConfig).test()
        observer.assertComplete().assertValue {
            val suggestions = it.get()!!.sections?.get("Search Suggestions")
            suggestions?.isNotEmpty()!! && suggestions.size == 5
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?filters%5Bgroup_id%5D=101&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.13.0&_dt="
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
        val path = "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.13.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.13.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            val suggestions = it.get()!!.sections?.get("Search Suggestions")
            suggestions?.isEmpty()!!
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.13.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithHiddenFields() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        val facetsConfig = AutocompleteFacetsConfig(null, null, listOf("hiddenField1", "hiddenField2"))
        mockServer.enqueue(mockResponse)
        constructorIo.getAutocompleteResults("bbq", null, facetsConfig).test()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/bbq?hidden_fields=hiddenField1&hidden_fields=hiddenField2&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.13.0&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithNumberSections() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        constructorIo.getAutocompleteResults("bbq", mapOf("Search Suggestions" to 8, "Products" to 4)).test()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/bbq?num_results_Search%20Suggestions=8&num_results_Products=4&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.13.0&_dt="
        assert(request.path!!.startsWith(path))
    }
}