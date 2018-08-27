package io.constructor.core

import android.content.Context
import io.constructor.BuildConfig
import io.constructor.data.DataManager
import io.constructor.data.interceptor.TokenInterceptor
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.model.Group
import io.constructor.data.model.SuggestionViewModel
import io.constructor.util.broadcastIntent
import io.constructor.util.urlEncode
import io.mockk.*
import io.reactivex.Observable
import okhttp3.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals

class ConstructorIoTest {

    private val ctx = mockk<Context>()
    private val pref = mockk<PreferencesHelper>()
    private val data = mockk<DataManager>()
    private var constructorIo = ConstructorIo
    private val sampleMillis = "1520000000000"
    private val dummySuggestion = SuggestionViewModel("", Group("123", "Test name", null), "", null)

    @Before
    fun setUp() {
        every { ctx.applicationContext } returns ctx
        every { pref.token = any() } returns Unit
        every { pref.id } returns "1"
        every { pref.getSessionId() } returns 1
        every { pref.getSessionId(any()) } returns 1
        constructorIo.testInit(ctx, "dummyKey", data, pref)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun verifySelectUrl() {
        val expected = "https://ac.cnstrc.com/autocomplete/hot%20dogs/select?s=1&i=1&_dt=1520000000000&autocomplete_section=Search%20Suggestions&original_query=dog&group%5Bgroup_id%5D=Meat%20%26%20Seafood&group%5Bdisplay_name%5D=Meat%20%26%20Seafood&tr=click&c=cioand-${BuildConfig.VERSION_NAME}&autocomplete_key=testKey"
        val searchQuery = "dog"
        val term = "hot dogs"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("autocomplete")
                .addPathSegment(term)
                .addPathSegment("select")
                .addQueryParameter(Constants.QueryConstants.SESSION, "1")
                .addQueryParameter(Constants.QueryConstants.IDENTITY, "1")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
                .addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_SECTION, Constants.QueryValues.SEARCH_SUGGESTIONS)
                .addQueryParameter(Constants.QueryConstants.ORIGINAL_QUERY, searchQuery)
                .addEncodedQueryParameter(Constants.QueryConstants.GROUP_ID.urlEncode(), "Meat%20%26%20Seafood")
                .addEncodedQueryParameter(Constants.QueryConstants.GROUP_DISPLAY_NAME.urlEncode(), "Meat & Seafood".urlEncode())
                .addQueryParameter(Constants.QueryConstants.EVENT, Constants.QueryValues.EVENT_CLICK)
                .addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
                .addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_KEY, "testKey")
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifyGetSuggestionsUrl() {
        val expected = "https://ac.cnstrc.com/autocomplete/dog?autocomplete_key=testKey&_dt=1520000000000"
        val searchQuery = "dog"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("autocomplete")
                .addPathSegment(searchQuery)
                .addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_KEY, "testKey")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifySessionStartUrl() {
        val expected = "https://ac.cnstrc.com/behavior?c=cioand-0.1.0&s=1&action=session_start&autocomplete_key=testKey&_dt=1520000000000"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("behavior")
                .addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
                .addQueryParameter(Constants.QueryConstants.SESSION, "1")
                .addQueryParameter(Constants.QueryConstants.ACTION, "session_start")
                .addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_KEY, "testKey")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifySearchClickThroughEvent() {
        val expected = "https://ac.cnstrc.com/autocomplete/term/click_through?c=cioand-0.1.0&s=1&autocomplete_section=Products&autocomplete_key=testKey&_dt=1520000000000"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("autocomplete")
                .addPathSegment("term")
                .addPathSegment("click_through")
                .addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
                .addQueryParameter(Constants.QueryConstants.SESSION, "1")
                .addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_SECTION, "Products")
                .addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_KEY, "testKey")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifySearchLoadedEventUrl() {
        val expected = "https://ac.cnstrc.com/behavior?c=cioand-0.1.0&s=1&action=search-results&autocomplete_key=testKey&_dt=1520000000000"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("behavior")
                .addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
                .addQueryParameter(Constants.QueryConstants.SESSION, "1")
                .addQueryParameter(Constants.QueryConstants.ACTION, Constants.QueryValues.EVENT_SEARCH_RESULTS)
                .addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_KEY, "testKey")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifyInputFocusEvent() {
        val expected = "https://ac.cnstrc.com/behavior?c=cioand-0.1.0&i=user_id&s=1&action=focus&autocomplete_key=testKey&_dt=1520000000000"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("behavior")
                .addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
                .addQueryParameter(Constants.QueryConstants.IDENTITY, "user_id")
                .addQueryParameter(Constants.QueryConstants.SESSION, "1")
                .addQueryParameter(Constants.QueryConstants.ACTION, Constants.QueryValues.EVENT_INPUT_FOCUS)
                .addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_KEY, "testKey")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun trackSelectSuccess() {
        staticMockk("io.constructor.util.ExtensionsKt").use {
            every { ctx.broadcastIntent(any(), any()) } returns Unit
            every { data.trackSelect(any(), any(), any()) } returns Observable.just(Response.success(""))
            constructorIo.trackSelect("doggy dog", dummySuggestion)
            verify(exactly = 1) { ctx.broadcastIntent(any(), any()) }
        }
    }

    @Test
    fun trackSelectError() {
        staticMockk("io.constructor.util.ExtensionsKt").use {
            every { ctx.broadcastIntent(any(), any()) } returns Unit
            every { data.trackSelect(any(), any(), any()) } returns Observable.just(Response.error(400, ResponseBody.create(MediaType.parse("text/plain"), "")))
            constructorIo.trackSelect("doggy dog", dummySuggestion)
            verify(exactly = 0) { ctx.broadcastIntent(any(), any()) }
        }
    }

    @Test
    fun trackSearchSuccess() {
        staticMockk("io.constructor.util.ExtensionsKt").use {
            every { ctx.broadcastIntent(any(), any()) } returns Unit
            every { data.trackSearch(any(), any(), any()) } returns Observable.just(Response.success(""))
            constructorIo.trackSearch("doggy dog", dummySuggestion)
            verify(exactly = 1) { ctx.broadcastIntent(any(), any()) }
        }
    }

    @Test
    fun verifySearchUrl() {
        val expected = "https://ac.cnstrc.com/autocomplete/hot%20dogs/search?s=1&i=1&_dt=1520000000000&original_query=dog&group%5Bgroup_id%5D=Meat%20%26%20Seafood&group%5Bdisplay_name%5D=Meat%20%26%20Seafood&tr=search&c=cioand-${BuildConfig.VERSION_NAME}&autocomplete_key=testKey"
        val originalQuery = "dog"
        val term = "hot dogs"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("autocomplete")
                .addPathSegment(term)
                .addPathSegment("search")
                .addQueryParameter(Constants.QueryConstants.SESSION, "1")
                .addQueryParameter(Constants.QueryConstants.IDENTITY, "1")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
                .addQueryParameter(Constants.QueryConstants.ORIGINAL_QUERY, originalQuery)
                .addEncodedQueryParameter(Constants.QueryConstants.GROUP_ID.urlEncode(), "Meat%20%26%20Seafood")
                .addEncodedQueryParameter(Constants.QueryConstants.GROUP_DISPLAY_NAME.urlEncode(), "Meat & Seafood".urlEncode())
                .addQueryParameter(Constants.QueryConstants.EVENT, Constants.QueryValues.EVENT_SEARCH)
                .addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
                .addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_KEY, "testKey")
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifyTestCellParamsAddedToRequest() {
        val mockServer = MockWebServer()
        every { pref.token } returns "123"
        every { pref.id } returns "1"
        every { pref.testCellParams = any() } just Runs
        every { pref.testCellParams } returns listOf("ef-1" to "2", "ef-3" to "4")
        constructorIo.setTestCellValues("1" to "2", "3" to "4")
        verify(exactly = 1) { pref.testCellParams = any() }
        mockServer.start()
        mockServer.enqueue(MockResponse())
        var client = OkHttpClient.Builder().addInterceptor(TokenInterceptor(ctx, pref)).build()
        client.newCall(Request.Builder().url(mockServer.url("/")).build()).execute()
        var recordedRequest = mockServer.takeRequest()
        assert(recordedRequest.path.contains("ef-1=2"))
    }

    @Test
    fun trackSearchError() {
        staticMockk("io.constructor.util.ExtensionsKt").use {
            every { ctx.broadcastIntent(any(), any()) } returns Unit
            every { data.trackSearch(any(), any(), any()) } returns Observable.just(Response.error(400, ResponseBody.create(MediaType.parse("text/plain"), "")))
            constructorIo.trackSearch("doggy dog", dummySuggestion)
            verify(exactly = 0) { ctx.broadcastIntent(any(), any()) }
        }
    }

    @Test
    fun trackConversion() {
        every { pref.defaultItemSection } returns "Products"
        every { data.trackConversion(any(), any(), any(), any()) } returns Observable.just(Response.success(""))
        constructorIo.trackConversion(itemId = "1")
        verify(exactly = 1) { data.trackConversion(any(), any(), any(), any()) }
    }

    @Test
    fun trackSearchResultClickThrough() {
        every { pref.defaultItemSection } returns "Products"
        every { data.trackSearchResultClickThrough(any(), any(), any(), any()) } returns Observable.just(Response.success(""))
        constructorIo.trackSearchResultClickThrough("1", "1")
        verify(exactly = 1) { data.trackSearchResultClickThrough(any(), any(), any(), any()) }
    }

    @Test
    fun getSessionId() {
        constructorIo.getSessionId()
        verify(exactly = 1) { pref.getSessionId() }
    }

    @Test
    fun getClientId() {
        constructorIo.getClientId()
        verify(exactly = 2) { pref.id }
    }

    @Test
    fun trackInputFocus() {
        every { data.trackInputFocus(any(), any()) } returns Observable.just(Response.success(""))
        constructorIo.trackInputFocus("1")
        verify(exactly = 1) { data.trackInputFocus(any(), any()) }
    }

}