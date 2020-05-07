package io.constructor.core

import android.content.Context
import io.constructor.data.DataManager
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.remote.ApiPaths
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class ConstructorIoTest {

    @Rule
    @JvmField val overrideSchedulersRule = RxSchedulersOverrideRule()

    private lateinit var dataManager: DataManager
    private lateinit var mockServer: MockWebServer
    private var constructorIo = ConstructorIo
    private val ctx = mockk<Context>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()

    @Before
    fun setup() {
        every { ctx.applicationContext } returns ctx
        every { preferencesHelper.token } returns "123"
        every { preferencesHelper.id } returns "1"
        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.testCellParams = any() } just Runs
        every { configMemoryHolder.userId } returns "id1"
        every { configMemoryHolder.testCellParams } returns emptyList()

        val config = ConstructorIoConfig("dummyKey", testCells = listOf("1" to "2", "3" to "4"))

        mockServer = MockWebServer()
        mockServer.start()
        dataManager = createTestDataManager(mockServer, preferencesHelper, configMemoryHolder, ctx)
        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun trackAutocompleteSelect() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackAutocompleteSelect("titanic").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/select?key=123&i=1&ui=id1&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackAutocompleteSelectWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackAutocompleteSelect("titanic").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/select?key=123&i=1&ui=id1&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackAutocompleteSelectWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackAutocompleteSelect("titanic").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/select?key=123&i=1&ui=id1&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchSubmit() {
        val path = "/" + ApiPaths.URL_SEARCH_SUBMIT_EVENT.replace("{term}", "titanic")
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSearchSubmit("titanic").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchSubmit500() {
        val path = "/" + ApiPaths.URL_SEARCH_SUBMIT_EVENT.replace("{term}", "titanic")
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSearchSubmit("titanic").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchSubmitTimeout() {
        val path = "/" + ApiPaths.URL_SEARCH_SUBMIT_EVENT.replace("{term}", "titanic")
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSearchSubmit("titanic").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSessionStart() {
        val path = "/" + ApiPaths.URL_SESSION_START_EVENT
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSessionStart(arrayOf(Constants.QueryConstants.SESSION to "1")).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("s=1"))
    }

    @Test
    fun trackSessionStart500() {
        val path = "/" + ApiPaths.URL_SESSION_START_EVENT
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSessionStart(arrayOf(Constants.QueryConstants.SESSION to "1")).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("s=1"))
    }

    @Test
    fun trackSessionStartTimeout() {
        val path = "/" + ApiPaths.URL_SESSION_START_EVENT
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSessionStart(arrayOf(Constants.QueryConstants.SESSION to "1")).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("s=1"))
    }

    @Test
    fun trackConversion() {
        val path = "/" + ApiPaths.URL_CONVERSION_EVENT.replace("{term}", "titanic")
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackConversion("titanic", "ship", "cid").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("name=ship"))
        assert(request.path.contains("customer_id=cid"))
    }

    @Test
    fun trackConversion500() {
        val path = "/" + ApiPaths.URL_CONVERSION_EVENT.replace("{term}", "titanic")
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackConversion("titanic", "ship", "cid").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("name=ship"))
        assert(request.path.contains("customer_id=cid"))
    }

    @Test
    fun trackConversionTimeout() {
        val path = "/" + ApiPaths.URL_CONVERSION_EVENT.replace("{term}", "titanic")
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackConversion("titanic", "ship", "cid").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("name=ship"))
        assert(request.path.contains("customer_id=cid"))
    }

    @Test
    fun trackSearchResultClick() {
        val path = "/" + ApiPaths.URL_SEARCH_RESULT_CLICK_EVENT.replace("{term}", "titanic")
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSearchResultClick("ship", "cid", "titanic").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("name=ship"))
        assert(request.path.contains("customer_id=cid"))
    }

    @Test
    fun trackSearchResultClick500() {
        val path = "/" + ApiPaths.URL_SEARCH_RESULT_CLICK_EVENT.replace("{term}", "titanic")
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSearchResultClick("ship", "cid", "titanic").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("name=ship"))
        assert(request.path.contains("customer_id=cid"))
    }

    @Test
    fun trackSearchResultClickTimeout() {
        val path = "/" + ApiPaths.URL_SEARCH_RESULT_CLICK_EVENT.replace("{term}", "titanic")
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSearchResultClick("ship", "cid", "titanic").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("name=ship"))
        assert(request.path.contains("customer_id=cid"))
    }

    @Test
    fun trackSearchResultLoaded() {
        val path = "/" + ApiPaths.URL_BEHAVIOR
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSearchResultsLoaded("titanic", 10, arrayOf(Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_SEARCH_RESULTS)).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("${Constants.QueryConstants.ACTION}=${Constants.QueryValues.EVENT_SEARCH_RESULTS}"))
    }

    @Test
    fun trackSearchResultLoaded500() {
        val path = "/" + ApiPaths.URL_BEHAVIOR
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSearchResultsLoaded("titanic", 10, arrayOf(Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_SEARCH_RESULTS)).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("${Constants.QueryConstants.ACTION}=${Constants.QueryValues.EVENT_SEARCH_RESULTS}"))
    }

    @Test
    fun trackSearchResultLoadedTimeout() {
        val path = "/" + ApiPaths.URL_BEHAVIOR
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackSearchResultsLoaded("titanic", 10, arrayOf(Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_SEARCH_RESULTS)).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("${Constants.QueryConstants.ACTION}=${Constants.QueryValues.EVENT_SEARCH_RESULTS}"))
    }

    @Test
    fun trackInputFocus() {
        val path = "/" + ApiPaths.URL_BEHAVIOR
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackInputFocus("titanic", arrayOf(Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_INPUT_FOCUS)).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("${Constants.QueryConstants.ACTION}=${Constants.QueryValues.EVENT_INPUT_FOCUS}"))
    }

    @Test
    fun trackInputFocus500() {
        val path = "/" + ApiPaths.URL_BEHAVIOR
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackInputFocus("titanic", arrayOf(Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_INPUT_FOCUS)).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("${Constants.QueryConstants.ACTION}=${Constants.QueryValues.EVENT_INPUT_FOCUS}"))
    }

    @Test
    fun trackInputFocusTimeout() {
        val path = "/" + ApiPaths.URL_BEHAVIOR
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackInputFocus("titanic", arrayOf(Constants.QueryConstants.ACTION to Constants.QueryValues.EVENT_INPUT_FOCUS)).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("${Constants.QueryConstants.ACTION}=${Constants.QueryValues.EVENT_INPUT_FOCUS}"))
    }

    @Test
    fun trackPurchase() {
        val path = "/" + ApiPaths.URL_PURCHASE
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackPurchase(listOf("1", "2"), "12.99", arrayOf()).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("${Constants.QueryConstants.CUSTOMER_ID}=1"))
        assert(request.path.contains("${Constants.QueryConstants.CUSTOMER_ID}=2"))
    }

    @Test
    fun trackPurchase500() {
        val path = "/" + ApiPaths.URL_PURCHASE
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackPurchase(listOf("1", "2"), "12.99", arrayOf()).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("${Constants.QueryConstants.CUSTOMER_ID}=1"))
        assert(request.path.contains("${Constants.QueryConstants.CUSTOMER_ID}=2"))
    }

    @Test
    fun trackPurchaseTimeout() {
        val path = "/" + ApiPaths.URL_PURCHASE
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.trackPurchase(listOf("1", "2"), "12.99", arrayOf()).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
        assert(request.path.contains("${Constants.QueryConstants.CUSTOMER_ID}=1"))
        assert(request.path.contains("${Constants.QueryConstants.CUSTOMER_ID}=2"))
    }
}