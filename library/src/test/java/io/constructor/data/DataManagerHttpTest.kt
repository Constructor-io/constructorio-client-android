package io.constructor.data

import android.content.Context
import io.constructor.core.Constants
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.remote.ApiPaths
import io.constructor.data.remote.ConstructorApi
import io.constructor.injection.module.NetworkModule
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.TestDataLoader
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class DataManagerHttpTest {

    @Rule
    @JvmField val overrideSchedulersRule = RxSchedulersOverrideRule()

    private lateinit var constructorApi: ConstructorApi
    private lateinit var dataManager: DataManager
    private lateinit var mockServer: MockWebServer

    private val ctx = mockk<Context>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()

    @Before
    fun setup() {
        every { preferencesHelper.token } returns "123"
        every { preferencesHelper.id } returns "1"
        every { configMemoryHolder.testCellParams = any() } just Runs
        every { configMemoryHolder.userId } returns "id1"
        every { configMemoryHolder.testCellParams } returns emptyList()
        mockServer = MockWebServer()
        mockServer.start()

        val basePath = mockServer.url("")
        val networkModule = NetworkModule(ctx);
        val loggingInterceptor = networkModule.provideHttpLoggingInterceptor()
        val tokenInterceptor = networkModule.provideTokenInterceptor(preferencesHelper, configMemoryHolder)
        val moshi = networkModule.provideMoshi()

        // Intercept all requests to the Constructor API and point them to a mock web server
        val okHttpClient = networkModule.provideOkHttpClient(loggingInterceptor, tokenInterceptor).newBuilder().addInterceptor { chain ->
            var request = chain.request()
            val requestUrl = request.url()
            val newRequestUrl = HttpUrl.Builder().scheme(basePath.scheme())
                    .encodedQuery(requestUrl.encodedQuery())
                    .host(basePath.host())
                    .port(basePath.port())
                    .encodedPath(requestUrl.encodedPath()).build()
            request = request.newBuilder()
                    .url(newRequestUrl)
                    .build()
            chain.proceed(request)
        }.readTimeout(1, TimeUnit.SECONDS).build()
        val retrofit = networkModule.provideRetrofit(okHttpClient, moshi)

        constructorApi = retrofit.create(ConstructorApi::class.java)
        dataManager = DataManager(constructorApi, moshi)
        
    }

    @Test
    fun getAutocompleteResults() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.isNotEmpty() && it.get()!!.size == 5
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?key=123&i=1&ui=id1&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?key=123&i=1&ui=id1&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?key=123&i=1&ui=id1&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithUnexpectedResponse() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response_with_unexpected_data.json"))
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.isNotEmpty() && it.get()!!.size == 5
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?key=123&i=1&ui=id1&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun getAutocompleteResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("autocomplete_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isEmpty
        }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic?key=123&i=1&ui=id1&c=cioand-1.3.0&_dt="
        assert(request.path.startsWith(path))
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

    @Test
    fun getSearchResult() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("search_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getSearchResults("corn").test()
        observer.assertComplete().assertValue {
            it.get()!!.searchData.results!!.size == 20
        }
    }

    @Test
    fun getSearchResultsBadServerResponse() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getSearchResults("corn").test()
        observer.assertComplete().assertValue {
            it.networkError
        }
    }

    @Test
    fun getSearchResultsTimeoutException() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("search_response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getSearchResults("corn").test()
        observer.assertComplete().assertValue {
            it.isError
        }
    }

    @Test
    fun getSearchUnexpectedDataResponse() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("search_response_unexpected_data.json"))
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getSearchResults("corn").test()
        observer.assertComplete().assertValue {
            it.get()!!.searchData.resultCount == 23
        }
    }

    @Test
    fun getSearchResultsEmptyResponse() {
        val path = "/" + ApiPaths.URL_SEARCH.format("corn")
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("search_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getSearchResults("corn").test()
        observer.assertComplete().assertValue {
            it.get()!!.searchData.results!!.isEmpty()
        }
    }

}