package io.constructor.core

import android.content.Context
import io.constructor.BuildConfig
import io.constructor.data.DataManager
import io.constructor.data.interceptor.TokenInterceptor
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.Group
import io.constructor.data.model.SuggestionViewModel
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.broadcastIntent
import io.constructor.util.urlEncode
import io.mockk.*
import io.reactivex.Completable
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ConstructorIoTest {

    @Rule
    @JvmField val overrideSchedulersRule = RxSchedulersOverrideRule()

    private val ctx = mockk<Context>()
    private val pref = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()
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
        every { pref.getSessionId(any(), any()) } returns 1
        constructorIo.testInit(ctx, ConstructorIoConfig("dummyKey",
                testCells = listOf("1" to "2", "3" to "4")), data, pref, configMemoryHolder)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun verifySelectUrl() {
        val expected = "https://ac.cnstrc.com/autocomplete/hot%20dogs/select?s=1&i=1&_dt=1520000000000&autocomplete_section=Search%20Suggestions&original_query=dog&group%5Bgroup_id%5D=Meat%20%26%20Seafood&group%5Bdisplay_name%5D=Meat%20%26%20Seafood&tr=click&c=cioand-${BuildConfig.VERSION_NAME}&key=testKey"
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
                .addQueryParameter(Constants.QueryConstants.API_KEY, "testKey")
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifyGetSuggestionsUrl() {
        val expected = "https://ac.cnstrc.com/autocomplete/dog?key=testKey&_dt=1520000000000"
        val searchQuery = "dog"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("autocomplete")
                .addPathSegment(searchQuery)
                .addQueryParameter(Constants.QueryConstants.API_KEY, "testKey")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifySessionStartEventUrl() {
        val expected = "https://ac.cnstrc.com/behavior?c=${BuildConfig.CLIENT_VERSION}&s=1&action=session_start&key=testKey&_dt=1520000000000"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("behavior")
                .addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
                .addQueryParameter(Constants.QueryConstants.SESSION, "1")
                .addQueryParameter(Constants.QueryConstants.ACTION, "session_start")
                .addQueryParameter(Constants.QueryConstants.API_KEY, "testKey")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifySearchResultClickEventUrl() {
        val expected = "https://ac.cnstrc.com/autocomplete/term/click_through?c=${BuildConfig.CLIENT_VERSION}&s=1&autocomplete_section=Products&key=testKey&_dt=1520000000000"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("autocomplete")
                .addPathSegment("term")
                .addPathSegment("click_through")
                .addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
                .addQueryParameter(Constants.QueryConstants.SESSION, "1")
                .addQueryParameter(Constants.QueryConstants.AUTOCOMPLETE_SECTION, "Products")
                .addQueryParameter(Constants.QueryConstants.API_KEY, "testKey")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifySearchResultsLoadedEventUrl() {
        val expected = "https://ac.cnstrc.com/behavior?c=${BuildConfig.CLIENT_VERSION}&s=1&action=search-results&key=testKey&_dt=1520000000000"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("behavior")
                .addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
                .addQueryParameter(Constants.QueryConstants.SESSION, "1")
                .addQueryParameter(Constants.QueryConstants.ACTION, Constants.QueryValues.EVENT_SEARCH_RESULTS)
                .addQueryParameter(Constants.QueryConstants.API_KEY, "testKey")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifyInputFocusEvent() {
        val expected = "https://ac.cnstrc.com/behavior?c=${BuildConfig.CLIENT_VERSION}&i=user_id&s=1&action=focus&key=testKey&_dt=1520000000000"
        val urlBuilder = HttpUrl.Builder().scheme("https")
                .host("ac.cnstrc.com")
                .addPathSegment("behavior")
                .addQueryParameter(Constants.QueryConstants.CLIENT, BuildConfig.CLIENT_VERSION)
                .addQueryParameter(Constants.QueryConstants.IDENTITY, "user_id")
                .addQueryParameter(Constants.QueryConstants.SESSION, "1")
                .addQueryParameter(Constants.QueryConstants.ACTION, Constants.QueryValues.EVENT_INPUT_FOCUS)
                .addQueryParameter(Constants.QueryConstants.API_KEY, "testKey")
                .addQueryParameter(Constants.QueryConstants.TIMESTAMP, sampleMillis)
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun trackAutocompleteSelectSuccess() {
        staticMockk("io.constructor.util.ExtensionsKt").use {
            every { ctx.broadcastIntent(any(), any()) } returns Unit
            every { data.trackAutocompleteSelect(any(), any(), any()) } returns Completable.complete()
            constructorIo.trackAutocompleteSelect("doggy dog", "dog", "section1", dummySuggestion.group)
            verify(exactly = 1) { ctx.broadcastIntent(any(), any()) }
        }
    }

    @Test
    fun trackAutocompleteSelectError() {
        staticMockk("io.constructor.util.ExtensionsKt").use {
            every { ctx.broadcastIntent(any(), any()) } returns Unit
            every { data.trackAutocompleteSelect(any(), any(), any()) } returns Completable.error(Exception())
            constructorIo.trackAutocompleteSelect("doggy dog", "dog", "section1", dummySuggestion.group)
            verify(exactly = 0) { ctx.broadcastIntent(any(), any()) }
        }
    }

    @Test
    fun trackSearchSubmitSuccess() {
        staticMockk("io.constructor.util.ExtensionsKt").use {
            every { ctx.broadcastIntent(any(), any()) } returns Unit
            every { data.trackSearchSubmit(any(), any(), any()) } returns Completable.complete()
            constructorIo.trackSearchSubmit("doggy dog", "dog", dummySuggestion.group)
            verify(exactly = 1) { ctx.broadcastIntent(any(), any()) }
        }
    }

    @Test
    fun verifySearchUrl() {
        val expected = "https://ac.cnstrc.com/autocomplete/hot%20dogs/search?s=1&i=1&_dt=1520000000000&original_query=dog&group%5Bgroup_id%5D=Meat%20%26%20Seafood&group%5Bdisplay_name%5D=Meat%20%26%20Seafood&tr=search&c=cioand-${BuildConfig.VERSION_NAME}&key=testKey"
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
                .addQueryParameter(Constants.QueryConstants.API_KEY, "testKey")
        val urlString = urlBuilder.build().url().toString()
        assertEquals(expected, urlString)
    }

    @Test
    fun verifyTestCellParamsAddedToRequest() {
        val mockServer = MockWebServer()
        every { pref.token } returns "123"
        every { pref.id } returns "1"
        every { configMemoryHolder.testCellParams = any() } just Runs
        every { configMemoryHolder.userId } returns "uid"
        every { configMemoryHolder.autocompleteResultCount } returns mapOf(Constants.QueryValues.SEARCH_SUGGESTIONS to 10, Constants.QueryValues.PRODUCTS to 0)
        every { configMemoryHolder.testCellParams } returns listOf("ef-1" to "2", "ef-3" to "4")
        mockServer.start()
        mockServer.enqueue(MockResponse())
        var client = OkHttpClient.Builder().addInterceptor(TokenInterceptor(ctx, pref, configMemoryHolder)).build()
        client.newCall(Request.Builder().url(mockServer.url("/")).build()).execute()
        var recordedRequest = mockServer.takeRequest()
        assert(recordedRequest.path.contains("ef-1=2"))
        assert(recordedRequest.path.contains("ui=uid"))
    }

    @Test
    fun trackSearchSubmitError() {
        staticMockk("io.constructor.util.ExtensionsKt").use {
            every { ctx.broadcastIntent(any(), any()) } returns Unit
            every { data.trackSearchSubmit(any(), any(), any()) } returns Completable.error(Exception())
            constructorIo.trackSearchSubmit("doggy dog", "dog", dummySuggestion.group)
            verify(exactly = 0) { ctx.broadcastIntent(any(), any()) }
        }
    }

    @Test
    fun trackConversion() {
        every { pref.defaultItemSection } returns "Products"
        every { data.trackConversion(any(), any(), any(), any(), any()) } returns Completable.complete()
        constructorIo.trackConversion("corn", "id1", 11.99)
        verify(exactly = 1) { data.trackConversion("TERM_UNKNOWN", any(), any(), any(), any()) }
    }

    @Test
    fun trackSearchResultClick() {
        every { pref.defaultItemSection } returns "Products"
        every { data.trackSearchResultClick(any(), any(), any(), any()) } returns Completable.complete()
        constructorIo.trackSearchResultClick("1", "1")
        verify(exactly = 1) { data.trackSearchResultClick(any(), any(), any(), any()) }
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
        every { data.trackInputFocus(any(), any()) } returns Completable.complete()
        constructorIo.trackInputFocus("1")
        verify(exactly = 1) { data.trackInputFocus(any(), any()) }
    }

    @Test
    fun trackPurchase() {
        every { pref.defaultItemSection } returns "Products"
        every { data.trackPurchase(any(), any(), any()) } returns Completable.complete()
        constructorIo.trackPurchase(arrayOf("id1"), 12.99)
        verify(exactly = 1) { data.trackPurchase(any(), any(), any()) }
    }

}