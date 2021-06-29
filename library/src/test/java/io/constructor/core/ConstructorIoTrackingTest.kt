package io.constructor.core

import android.content.Context
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.common.ResultGroup
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal fun getRequestBody(request: RecordedRequest): Map<String, String> {
    val requestBodyString = request.body.readUtf8().drop(1).dropLast(1).replace("\"", "")
    // Split on "," not within an object array
    val requestBodyList = ArrayList(requestBodyString.split("(?<=\\d|]|\\w),".toRegex()))
    return requestBodyList.associate {
        // Split on ":" and rejoin for potential values with key/value fields
        val list = it.split(":")
        val key = list.elementAt(0)
        val value = list.slice(1 until list.size).joinToString(":")
        key to value
    }
}

class ConstructorIoTrackingTest {

    @Rule
    @JvmField val overrideSchedulersRule = RxSchedulersOverrideRule()

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

        every { preferencesHelper.apiKey } returns "copper-key"
        every { preferencesHelper.id } returns "wacko-the-guid"
        every { preferencesHelper.serviceUrl } returns mockServer.hostName
        every { preferencesHelper.port } returns mockServer.port
        every { preferencesHelper.scheme } returns "http"
        every { preferencesHelper.defaultItemSection } returns "Products"
        every { preferencesHelper.getSessionId(any(), any()) } returns 67

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.userId } returns "player-three"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("dummyKey")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder, ctx)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun trackSessionStart() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.trackSessionStartInternal().test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/behavior?action=session_start&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSessionStart500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSessionStartInternal().test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/behavior?action=session_start&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSessionStartTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSessionStartInternal().test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/behavior?action=session_start&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackInputFocus() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackInputFocusInternal("tita").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/behavior?term=tita&action=focus&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackInputFocus500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackInputFocusInternal("tita").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/behavior?term=tita&action=focus&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackInputFocusTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackInputFocusInternal("tita").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/behavior?term=tita&action=focus&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackAutocompleteSelect() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackAutocompleteSelectInternal("titanic", "tit", "Search Suggestions").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/select?section=Search%20Suggestions&original_query=tit&tr=click&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackAutocompleteSelectWithSectionAndResultID() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackAutocompleteSelectInternal("titanic", "tit", "Search Suggestions", ResultGroup("recommended", "123123"), "2346784").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/select?section=Search%20Suggestions&original_query=tit&tr=click&group%5Bgroup_id%5D=123123&group%5Bdisplay_name%5D=recommended&result_id=2346784&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackAutocompleteSelectWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackAutocompleteSelectInternal("titanic", "tit", "Search Suggestions").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/select?section=Search%20Suggestions&original_query=tit&tr=click&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackAutocompleteSelectWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackAutocompleteSelectInternal("titanic", "tit", "Search Suggestions").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/select?section=Search%20Suggestions&original_query=tit&tr=click&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt="
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchSubmit() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchSubmitInternal("titanic", "tit", null).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/search?original_query=tit&tr=search&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchSubmit500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchSubmitInternal("titanic", "tit", null).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/search?original_query=tit&tr=search&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchSubmitTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchSubmitInternal("titanic", "tit", null).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/search?original_query=tit&tr=search&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchResultLoaded() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultsLoadedInternal("titanic", 10).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/behavior?term=titanic&num_results=10&action=search-results&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchResultLoadedWithCustomerIDs() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultsLoadedInternal("titanic", 10, arrayOf("TIT-REP-1997", "QE2-REP-1969")).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/behavior?term=titanic&num_results=10&customer_ids=TIT-REP-1997%2CQE2-REP-1969&action=search-results&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchResultLoaded500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultsLoadedInternal("titanic", 10).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/behavior?term=titanic&num_results=10&action=search-results&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchResultLoadedTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultsLoadedInternal("titanic", 10).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/behavior?term=titanic&num_results=10&action=search-results&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchResultClick() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultClickInternal("titanic replica", "TIT-REP-1997", "titanic").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/click_through?name=titanic%20replica&customer_id=TIT-REP-1997&section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchResultClickWithSectionAndResultID() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultClickInternal("titanic replica", "TIT-REP-1997", "titanic", "Products","3467632").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/click_through?name=titanic%20replica&customer_id=TIT-REP-1997&section=Products&result_id=3467632&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchResultClick500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultClickInternal("titanic replica", "TIT-REP-1997", "titanic").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/click_through?name=titanic%20replica&customer_id=TIT-REP-1997&section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackSearchResultClickTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultClickInternal("titanic replica", "TIT-REP-1997", "titanic").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/click_through?name=titanic%20replica&customer_id=TIT-REP-1997&section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackConversion() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackConversionInternal("titanic replica", "TIT-REP-1997", 89.00).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/conversion?key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assertEquals("titanic replica", requestBody["item_name"])
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("89.00", requestBody["revenue"])
        assertEquals("TERM_UNKNOWN", requestBody["search_term"])
        assertEquals("Products", requestBody["section"])
        assertEquals("POST", request.method)
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackConversionWithConversionType() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackConversionInternal("titanic replica", "TIT-REP-1997", 89.00, "titanic", "Products", "Like").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/conversion?key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assertEquals("titanic replica", requestBody["item_name"])
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("Like", requestBody["conversion_type"])
        assertEquals("89.00", requestBody["revenue"])
        assertEquals("titanic", requestBody["search_term"])
        assertEquals("Products", requestBody["section"])
        assertEquals("POST", request.method)
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackConversion500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackConversionInternal("titanic replica", "TIT-REP-1997", 89.00).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/conversion?key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assertEquals("titanic replica", requestBody["item_name"])
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("89.00", requestBody["revenue"])
        assertEquals("TERM_UNKNOWN", requestBody["search_term"])
        assertEquals("Products", requestBody["section"])
        assertEquals("POST", request.method)
        assert(request.path.startsWith(path))

    }

    @Test
    fun trackConversionTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackConversionInternal("titanic replica", "TIT-REP-1997", 89.00).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackPurchase() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackPurchaseInternal(arrayOf("TIT-REP-1997", "QE2-REP-1969"), 12.99, "ORD-1312343").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/purchase?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assertEquals("[{item_id:TIT-REP-1997},{item_id:QE2-REP-1969}]", requestBody["items"])
        assertEquals("12.99", requestBody["revenue"])
        assertEquals("ORD-1312343", requestBody["order_id"])
        assertEquals("POST", request.method)
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackPurchase500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackPurchaseInternal(arrayOf("TIT-REP-1997", "QE2-REP-1969"), 12.99, "ORD-1312343").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/purchase?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assertEquals("[{item_id:TIT-REP-1997},{item_id:QE2-REP-1969}]", requestBody["items"])
        assertEquals("12.99", requestBody["revenue"])
        assertEquals("ORD-1312343", requestBody["order_id"])
        assertEquals("POST", request.method)
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackPurchaseTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackPurchaseInternal(arrayOf("TIT-REP-1997", "QE2-REP-1969"), 12.99,"ORD-1312343").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackPurchaseWithSection() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackPurchaseInternal(arrayOf("TIT-REP-1997", "QE2-REP-1969"), 12.99, "ORD-1312343", "Recommendations").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/purchase?section=Recommendations&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assertEquals("[{item_id:TIT-REP-1997},{item_id:QE2-REP-1969}]", requestBody["items"])
        assertEquals("12.99", requestBody["revenue"])
        assertEquals("ORD-1312343", requestBody["order_id"])
        assertEquals("Recommendations", requestBody["section"])
        assertEquals("POST", request.method)
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackBrowseResultLoaded() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultsLoadedInternal("group_id", "Movies", 10).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/browse_result_load?key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt="
        assertEquals("group_id", requestBody["filterName"])
        assertEquals("Movies", requestBody["filterValue"])
        assertEquals("10", requestBody["resultCount"])
        assertEquals("POST", request.method)
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackBrowseResultLoaded500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultsLoadedInternal("group_id", "Movies", 10).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/browse_result_load?key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt="
        assertEquals("group_id", requestBody["filterName"])
        assertEquals("Movies", requestBody["filterValue"])
        assertEquals("10", requestBody["resultCount"])
        assertEquals("POST", request.method)
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackBrowseResultLoadedTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultsLoadedInternal("group_id", "Movies", 10).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackBrowseResultClick() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultClickInternal("group_id", "Movies","TIT-REP-1997", 4).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/browse_result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assertEquals("group_id", requestBody["filterName"])
        assertEquals("Movies", requestBody["filterValue"])
        assertEquals("TIT-REP-1997", requestBody["customerId"])
        assertEquals("4", requestBody["resultPositionOnPage"])
        assertEquals("POST", request.method)
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackBrowseResultClickWithSectionAndResultID() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultClickInternal("group_id", "Movies","TIT-REP-1997", 4, "Products", "3467632").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/browse_result_click?section=Products&result_id=3467632&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assertEquals("group_id", requestBody["filterName"])
        assertEquals("Movies", requestBody["filterValue"])
        assertEquals("TIT-REP-1997", requestBody["customerId"])
        assertEquals("4", requestBody["resultPositionOnPage"])
        assertEquals("Products", requestBody["section"])
        assertEquals("3467632", requestBody["resultID"])
        assertEquals("POST", request.method)
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackBrowseResultClick500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultClickInternal("group_id", "Movies","TIT-REP-1997", 4).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/browse_result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.5.2&_dt=";
        assertEquals("group_id", requestBody["filterName"])
        assertEquals("Movies", requestBody["filterValue"])
        assertEquals("TIT-REP-1997", requestBody["customerId"])
        assertEquals("4", requestBody["resultPositionOnPage"])
        assertEquals("POST", request.method)
        assert(request.path.startsWith(path))
    }

    @Test
    fun trackBrowseResultClickTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultClickInternal("group_id", "Movies","TIT-REP-1997", 4).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

}
