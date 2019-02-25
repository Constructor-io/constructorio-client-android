package io.constructor.data

import android.content.Context
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.constructor.data.interceptor.TokenInterceptor
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.remote.ApiPaths
import io.constructor.data.remote.ConstructorApi
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.TestDataLoader
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class DataManagerHttpTest {

    @Rule
    @JvmField val overrideSchedulersRule = RxSchedulersOverrideRule()

    private lateinit var constructorApi: ConstructorApi

    private val ctx = mockk<Context>()
    private val pref = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()

    private lateinit var dataManager: DataManager

    private lateinit var mockServer: MockWebServer

    @Before
    fun setup() {
        every { pref.token } returns "123"
        every { pref.id } returns "1"
        every { configMemoryHolder.testCellParams = any() } just Runs
        every { configMemoryHolder.userId } returns "id1"
        every { configMemoryHolder.testCellParams } returns emptyList()
        mockServer = MockWebServer()
        mockServer.start()

        val client = OkHttpClient.Builder().addInterceptor(TokenInterceptor(ctx, pref, configMemoryHolder)).readTimeout(4, TimeUnit.SECONDS).build()

        val moshi = Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        // Get an instance of Retrofit
        val retrofit = Retrofit.Builder()
                .baseUrl(mockServer.url("").toString())
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        constructorApi = retrofit.create(ConstructorApi::class.java)
        dataManager = DataManager(constructorApi)
    }

    @Test
    fun getSuggestions() {
        val path = "/" + ApiPaths.URL_GET_SUGGESTIONS.replace("{value}", "titanic")
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("response.json"))
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.isNotEmpty() && it.get()!!.size == 5
        }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
    }

    @Test
    fun getSuggestionsBadServerResponse() {
        val path = "/" + ApiPaths.URL_GET_SUGGESTIONS.replace("{value}", "titanic")
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
    }

    @Test
    fun getSuggestionsTimeoutException() {
        val path = "/" + ApiPaths.URL_GET_SUGGESTIONS.replace("{value}", "titanic")
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
    }

    @Test
    fun getSuggestionsUnexpectedDataResponse() {
        val path = "/" + ApiPaths.URL_GET_SUGGESTIONS.replace("{value}", "titanic")
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("response_with_unexpected_data.json"))
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.isNotEmpty() && it.get()!!.size == 5
        }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
    }

    @Test
    fun getSuggestionsEmptyResponse() {
        val path = "/" + ApiPaths.URL_GET_SUGGESTIONS.replace("{value}", "titanic")
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("empty_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isEmpty
        }
        val request = mockServer.takeRequest()
        assert(request.path.startsWith(path))
    }

}