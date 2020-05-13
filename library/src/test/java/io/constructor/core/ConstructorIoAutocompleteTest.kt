package io.constructor.core

import android.content.Context
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.TestDataLoader
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
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
        every { ctx.applicationContext } returns ctx
        every { preferencesHelper.apiKey } returns "golden-key"
        every { preferencesHelper.id } returns "guido-the-guid"
        every { preferencesHelper.getSessionId(any(), any()) } returns 79
        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.testCellParams = any() } just Runs
        every { configMemoryHolder.userId } returns "player-one"
        every { configMemoryHolder.testCellParams } returns emptyList()

        mockServer = MockWebServer()
        mockServer.start()
        val config = ConstructorIoConfig("dummyKey", testCells = listOf("flavor" to "chocolate", "topping" to "sprinkles"))
        val dataManager = createTestDataManager(mockServer, preferencesHelper, configMemoryHolder, ctx)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getAutocompleteResults() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.isNotEmpty() && it.get()!!.size == 5
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
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
        val path = "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
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
        val path = "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithUnexpectedResponse() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response_with_unexpected_data.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.isNotEmpty() && it.get()!!.size == 5
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isEmpty
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
    }
}