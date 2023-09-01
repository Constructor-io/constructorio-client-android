package io.constructor.core

import android.content.Context
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.common.ResultGroup
import io.constructor.data.model.purchase.PurchaseItem
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
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals


internal fun getRequestBody(request: RecordedRequest): Map<String, String> {
    val requestBodyString = request.body.readUtf8().drop(1).dropLast(1).replace("\"", "")
    val stack = ArrayDeque<String>()
    val splitArr = arrayListOf<String>()
    var stringSum = ""

    // Stack implementation for parsing "," within stringified objects
    requestBodyString.forEach {
        val str = it.toString()

        if (str == "[" || str == "{") {
            stack.push(str)
        }

        if (stack.isEmpty()) {
            if (str == ",") {
                splitArr.add(stringSum)
                stringSum = ""
            } else {
                stringSum += it
            }
        } else {
            if (str == "]") {
                if (stack.peek() == "[") {
                    stack.pop()
                } else {
                    stack.push(str)
                }
            }

            if (str == "}") {
                if (stack.peek() == "{") {
                    stack.pop()
                } else {
                    stack.push(str)
                }
            }

            stringSum += (str)
        }
    }

    splitArr.add(stringSum)

    return splitArr.associate {
        val (key, value) = it.split(":", ignoreCase = true, limit = 2)
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
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun trackSessionStart() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.trackSessionStartInternal().test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/behavior?action=session_start&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSessionStart500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSessionStartInternal().test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/behavior?action=session_start&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSessionStartTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSessionStartInternal().test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/behavior?action=session_start&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackInputFocus() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackInputFocusInternal("tita").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/behavior?term=tita&action=focus&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackInputFocus500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackInputFocusInternal("tita").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/behavior?term=tita&action=focus&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackInputFocusTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackInputFocusInternal("tita").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/behavior?term=tita&action=focus&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackAutocompleteSelect() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackAutocompleteSelectInternal("titanic", "tit", "Search Suggestions").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/select?section=Search%20Suggestions&original_query=tit&tr=click&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackAutocompleteSelectWithSectionAndResultID() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackAutocompleteSelectInternal("titanic", "tit", "Search Suggestions", ResultGroup("recommended", "123123"), "2346784").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/select?section=Search%20Suggestions&original_query=tit&tr=click&result_id=2346784&group%5Bgroup_id%5D=123123&group%5Bdisplay_name%5D=recommended&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackAutocompleteSelectWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackAutocompleteSelectInternal("titanic", "tit", "Search Suggestions").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/select?section=Search%20Suggestions&original_query=tit&tr=click&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackAutocompleteSelectWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackAutocompleteSelectInternal("titanic", "tit", "Search Suggestions").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/select?section=Search%20Suggestions&original_query=tit&tr=click&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchSubmit() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchSubmitInternal("titanic", "tit", null).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/search?original_query=tit&tr=search&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchSubmit500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchSubmitInternal("titanic", "tit", null).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/search?original_query=tit&tr=search&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchSubmitTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchSubmitInternal("titanic", "tit", null).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/search?original_query=tit&tr=search&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchResultLoaded() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultsLoadedInternal("titanic", 10).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/behavior?term=titanic&num_results=10&action=search-results&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchResultLoadedWithCustomerIDs() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultsLoadedInternal("titanic", 10, arrayOf("TIT-REP-1997", "QE2-REP-1969")).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/behavior?term=titanic&num_results=10&customer_ids=TIT-REP-1997%2CQE2-REP-1969&action=search-results&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchResultLoaded500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultsLoadedInternal("titanic", 10).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/behavior?term=titanic&num_results=10&action=search-results&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchResultLoadedTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultsLoadedInternal("titanic", 10).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/behavior?term=titanic&num_results=10&action=search-results&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchResultClick() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultClickInternal("titanic replica", "TIT-REP-1997",null, "titanic").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/click_through?name=titanic%20replica&customer_id=TIT-REP-1997&section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchResultClickWithVariationId() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultClickInternal("titanic replica", "TIT-REP-1997","RED", "titanic").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/click_through?name=titanic%20replica&customer_id=TIT-REP-1997&variation_id=RED&section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchResultClickWithSectionAndResultID() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultClickInternal("titanic replica", "TIT-REP-1997", null,"titanic", "Products","3467632").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/click_through?name=titanic%20replica&customer_id=TIT-REP-1997&section=Products&result_id=3467632&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchResultClick500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultClickInternal("titanic replica", "TIT-REP-1997",null, "titanic").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/click_through?name=titanic%20replica&customer_id=TIT-REP-1997&section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackSearchResultClickTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackSearchResultClickInternal("titanic replica", "TIT-REP-1997",null, "titanic").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest()
        val path = "/autocomplete/titanic/click_through?name=titanic%20replica&customer_id=TIT-REP-1997&section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackConversion() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackConversionInternal("titanic replica", "TIT-REP-1997",null,89.00).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/v2/behavioral_action/conversion?key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        val requestBody = getRequestBody(request)
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("titanic replica", requestBody["item_name"])
        assertEquals("89.00", requestBody["revenue"])
        assertEquals("Products", requestBody["section"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackConversionWithVariationId() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackConversionInternal("titanic replica", "TIT-REP-1997","RED",89.00).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val path = "/v2/behavioral_action/conversion?key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        val requestBody = getRequestBody(request)
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("titanic replica", requestBody["item_name"])
        assertEquals("RED", requestBody["variation_id"])
        assertEquals("89.00", requestBody["revenue"])
        assertEquals("Products", requestBody["section"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackConversionWithConversionType() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackConversionInternal("titanic replica", "TIT-REP-1997", null,89.00, "titanic", "Products", "Like").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/conversion?key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("titanic replica", requestBody["item_name"])
        assertEquals("Like", requestBody["type"])
        assertEquals("89.00", requestBody["revenue"])
        assertEquals("titanic", requestBody["search_term"])
        assertEquals("Products", requestBody["section"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackConversion500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackConversionInternal("titanic replica", "TIT-REP-1997", null, 89.00).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/conversion?key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("titanic replica", requestBody["item_name"])
        assertEquals("89.00", requestBody["revenue"])
        assertEquals("Products", requestBody["section"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackConversionTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackConversionInternal("titanic replica", "TIT-REP-1997", null, 89.00).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackPurchase() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackPurchaseInternal(arrayOf(PurchaseItem("TIT-REP-1997"), PurchaseItem("QE2-REP-1969")), 12.99, "ORD-1312343").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/purchase?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("[{item_id:TIT-REP-1997},{item_id:QE2-REP-1969}]", requestBody["items"])
        assertEquals("ORD-1312343", requestBody["order_id"])
        assertEquals("12.99", requestBody["revenue"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackPurchaseWithQuantity() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackPurchaseInternal(arrayOf(PurchaseItem("TIT-REP-1997", null, 2), PurchaseItem("QE2-REP-1969")), 12.99, "ORD-1312343").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/purchase?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("[{item_id:TIT-REP-1997},{item_id:TIT-REP-1997},{item_id:QE2-REP-1969}]", requestBody["items"])
        assertEquals("ORD-1312343", requestBody["order_id"])
        assertEquals("12.99", requestBody["revenue"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackPurchaseWithVariationId() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackPurchaseInternal(arrayOf(PurchaseItem("TIT-REP-1997", "RED"), PurchaseItem("QE2-REP-1969")), 12.99, "ORD-1312343").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/purchase?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("[{item_id:TIT-REP-1997,variation_id:RED},{item_id:QE2-REP-1969}]", requestBody["items"])
        assertEquals("ORD-1312343", requestBody["order_id"])
        assertEquals("12.99", requestBody["revenue"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackPurchase500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackPurchaseInternal(arrayOf(PurchaseItem("TIT-REP-1997"), PurchaseItem("QE2-REP-1969")), 12.99, "ORD-1312343").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/purchase?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("[{item_id:TIT-REP-1997},{item_id:QE2-REP-1969}]", requestBody["items"])
        assertEquals("ORD-1312343", requestBody["order_id"])
        assertEquals("12.99", requestBody["revenue"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackPurchaseTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackPurchaseInternal(arrayOf(PurchaseItem("TIT-REP-1997"), PurchaseItem("QE2-REP-1969")), 12.99,"ORD-1312343").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackPurchaseWithSection() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackPurchaseInternal(arrayOf(PurchaseItem("TIT-REP-1997"), PurchaseItem("QE2-REP-1969")), 12.99, "ORD-1312343", "Recommendations").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/purchase?section=Recommendations&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("[{item_id:TIT-REP-1997},{item_id:QE2-REP-1969}]", requestBody["items"])
        assertEquals("ORD-1312343", requestBody["order_id"])
        assertEquals("12.99", requestBody["revenue"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackBrowseResultLoaded() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultsLoadedInternal("group_id", "Movies", 10).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/browse_result_load?key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("group_id", requestBody["filter_name"])
        assertEquals("Movies", requestBody["filter_value"])
        assertEquals("10", requestBody["result_count"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackBrowseResultLoaded500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultsLoadedInternal("group_id", "Movies", 10).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/browse_result_load?key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("group_id", requestBody["filter_name"])
        assertEquals("Movies", requestBody["filter_value"])
        assertEquals("10", requestBody["result_count"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
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
        val observer = ConstructorIo.trackBrowseResultClickInternal("group_id", "Movies","TIT-REP-1997", null,4, "Products", "123456").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/browse_result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("group_id", requestBody["filter_name"])
        assertEquals("Movies", requestBody["filter_value"])
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("4", requestBody["result_position_on_page"])
        assertEquals(null, requestBody["variation_id"])
        assertEquals("123456", requestBody["result_id"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackBrowseResultClickWithVariationId() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultClickInternal("group_id", "Movies","TIT-REP-1997", "RED",4).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/browse_result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("group_id", requestBody["filter_name"])
        assertEquals("Movies", requestBody["filter_value"])
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("4", requestBody["result_position_on_page"])
        assertEquals("RED", requestBody["variation_id"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackBrowseResultClickWithSectionAndResultID() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultClickInternal("group_id", "Movies","TIT-REP-1997", null, 4, "Products", "3467632").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/browse_result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("group_id", requestBody["filter_name"])
        assertEquals("Movies", requestBody["filter_value"])
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("4", requestBody["result_position_on_page"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackBrowseResultClick500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultClickInternal("group_id", "Movies","TIT-REP-1997", null,4).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/browse_result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("group_id", requestBody["filter_name"])
        assertEquals("Movies", requestBody["filter_value"])
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("4", requestBody["result_position_on_page"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackBrowseResultClickTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackBrowseResultClickInternal("group_id", "Movies","TIT-REP-1997", null, 4).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackItemDetailLoaded() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackItemDetailLoadedInternal("pencil", "123", "456", "Products").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/item_detail_load?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("pencil", requestBody["item_name"])
        assertEquals("123", requestBody["item_id"])
        assertEquals("456", requestBody["variation_id"])
        assertEquals("Products", requestBody["section"])
        assertEquals("Not Available", requestBody["url"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackItemDetailLoaded500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackItemDetailLoadedInternal("pencil", "123", "456", "Products").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/item_detail_load?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("pencil", requestBody["item_name"])
        assertEquals("123", requestBody["item_id"])
        assertEquals("456", requestBody["variation_id"])
        assertEquals("Products", requestBody["section"])
        assertEquals("Not Available", requestBody["url"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackItemDetailLoadedTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackItemDetailLoadedInternal("pencil", "123", "456", "Products").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackGenericResultClick() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackGenericResultClickInternal("pencil", "123", "456", "Products").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("pencil", requestBody["item_name"])
        assertEquals("123", requestBody["item_id"])
        assertEquals("456", requestBody["variation_id"])
        assertEquals("Products", requestBody["section"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackGenericResultClick500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackGenericResultClickInternal("pencil", "123", "456", "Products").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("pencil", requestBody["item_name"])
        assertEquals("123", requestBody["item_id"])
        assertEquals("456", requestBody["variation_id"])
        assertEquals("Products", requestBody["section"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackGenericResultClickTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackGenericResultClickInternal("pencil", "123", "456", "Products").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackRecommendationResultClick() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackRecommendationResultClickInternal("pdp5", "User Featured","TIT-REP-1997").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/recommendation_result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("pdp5", requestBody["pod_id"])
        assertEquals("User Featured", requestBody["strategy_id"])
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackRecommendationResultClickWithSectionAndResultID() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackRecommendationResultClickInternal("pdp5", "User Featured","TIT-REP-1997", null, "Search Suggestions", "3467632").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/recommendation_result_click?section=Search%20Suggestions&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("pdp5", requestBody["pod_id"])
        assertEquals("User Featured", requestBody["strategy_id"])
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackRecommendationResultClick500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackRecommendationResultClickInternal("pdp5", "User Featured","TIT-REP-1997").test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/recommendation_result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("pdp5", requestBody["pod_id"])
        assertEquals("User Featured", requestBody["strategy_id"])
        assertEquals("TIT-REP-1997", requestBody["item_id"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackRecommendationResultClickTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackRecommendationResultClickInternal("pdp5", "User Featured","TIT-REP-1997").test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackRecommendationResultsView() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackRecommendationResultsViewInternal("pdp5", 4).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/recommendation_result_view?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("pdp5", requestBody["pod_id"])
        assertEquals("4", requestBody["num_results_viewed"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackRecommendationResultsViewWithSectionAndResultID() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackRecommendationResultsViewInternal("pdp5", 4, 1, 4, "3467632", "Search Suggestions").test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/recommendation_result_view?section=Search%20Suggestions&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("pdp5", requestBody["pod_id"])
        assertEquals("4", requestBody["num_results_viewed"])
        assertEquals("1", requestBody["result_page"])
        assertEquals("4", requestBody["result_count"])
        assertEquals("3467632", requestBody["result_id"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackRecommendationResultsView500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackRecommendationResultsViewInternal("pdp5", 4).test()
        observer.assertError { true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/recommendation_result_view?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.25.1&_dt="
        assertEquals("pdp5", requestBody["pod_id"])
        assertEquals("4", requestBody["num_results_viewed"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackRecommendationResultsViewTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackRecommendationResultsViewInternal("pdp5", 4).test()
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackQuizResultClick() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackQuizResultClickInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", "shirt-a", "shirt-a--reg", "White shirt", null, null, 10, 10, 1, 10).test();
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/quiz_result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.22.4&_dt="
        assertEquals("coffee-quiz", requestBody["quiz_id"])
        assertEquals("23AECMA-1EFKCI", requestBody["quiz_version_id"])
        assertEquals("34NCUIEI-214CDN", requestBody["quiz_session_id"])
        assertEquals("1", requestBody["result_page"])
        assertEquals("10", requestBody["result_count"])
        assertEquals("10", requestBody["num_results_per_page"])
        assertEquals("10", requestBody["result_position_on_page"])
        assertEquals("shirt-a", requestBody["item_id"])
        assertEquals("White shirt", requestBody["item_name"])
        assertEquals("shirt-a--reg", requestBody["variation_id"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackQuizResultClickWithSectionAndResultID() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackQuizResultClickInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", "shirt-a", "shirt-a--reg", "White shirt", "Search Suggestions", "123", 10, 10, 1, 10).test();
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/quiz_result_click?section=Search%20Suggestions&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.22.4&_dt="
        assertEquals("coffee-quiz", requestBody["quiz_id"])
        assertEquals("23AECMA-1EFKCI", requestBody["quiz_version_id"])
        assertEquals("34NCUIEI-214CDN", requestBody["quiz_session_id"])
        assertEquals("1", requestBody["result_page"])
        assertEquals("10", requestBody["result_count"])
        assertEquals("10", requestBody["num_results_per_page"])
        assertEquals("10", requestBody["result_position_on_page"])
        assertEquals("shirt-a", requestBody["item_id"])
        assertEquals("White shirt", requestBody["item_name"])
        assertEquals("shirt-a--reg", requestBody["variation_id"])
        assertEquals("123", requestBody["result_id"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackQuizResultClick500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        val observer = ConstructorIo.trackQuizResultClickInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", "shirt-a", "shirt-a--reg", "White shirt", null, null, 10, 10, 1, 10).test();
        observer.assertError{ true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/quiz_result_click?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.22.4&_dt="
        assertEquals("coffee-quiz", requestBody["quiz_id"])
        assertEquals("23AECMA-1EFKCI", requestBody["quiz_version_id"])
        assertEquals("34NCUIEI-214CDN", requestBody["quiz_session_id"])
        assertEquals("1", requestBody["result_page"])
        assertEquals("10", requestBody["result_count"])
        assertEquals("10", requestBody["num_results_per_page"])
        assertEquals("10", requestBody["result_position_on_page"])
        assertEquals("shirt-a", requestBody["item_id"])
        assertEquals("White shirt", requestBody["item_name"])
        assertEquals("shirt-a--reg", requestBody["variation_id"])
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackQuizResultClickTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackQuizResultClickInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", "shirt-a", "shirt-a--reg", "White shirt", null, null, 10, 10, 1, 10).test();
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackQuizConversion() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackQuizConversionInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", null, null, null, "shirt-a", "shirt-a--reg", "White shirt", null, "129.99").test();
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/quiz_conversion?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.22.4&_dt="
        assertEquals("coffee-quiz", requestBody["quiz_id"])
        assertEquals("23AECMA-1EFKCI", requestBody["quiz_version_id"])
        assertEquals("34NCUIEI-214CDN", requestBody["quiz_session_id"])
        assertEquals("shirt-a", requestBody["item_id"])
        assertEquals("White shirt", requestBody["item_name"])
        assertEquals("shirt-a--reg", requestBody["variation_id"])
        assertEquals("129.99", requestBody["revenue"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackQuizConversionWithAllOptionalParameters() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackQuizConversionInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", "quizConversion", "quizConversion", true, "shirt-a", "shirt-a--reg", "White shirt", "Search Suggestions", "129.99").test();
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/quiz_conversion?section=Search%20Suggestions&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.22.4&_dt="
        assertEquals("coffee-quiz", requestBody["quiz_id"])
        assertEquals("23AECMA-1EFKCI", requestBody["quiz_version_id"])
        assertEquals("34NCUIEI-214CDN", requestBody["quiz_session_id"])
        assertEquals("shirt-a", requestBody["item_id"])
        assertEquals("White shirt", requestBody["item_name"])
        assertEquals("shirt-a--reg", requestBody["variation_id"])
        assertEquals("129.99", requestBody["revenue"])
        assertEquals("quizConversion", requestBody["display_name"])
        assertEquals("quizConversion", requestBody["type"])
        assertEquals("true", requestBody["is_custom_type"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackQuizConversion500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        val observer = ConstructorIo.trackQuizConversionInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", "quizConversion", "quizConversion", true, "shirt-a", "shirt-a--reg", "White shirt", "Search Suggestions", "129.99").test();
        observer.assertError{ true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/quiz_conversion?section=Search%20Suggestions&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.22.4&_dt="
        assertEquals("coffee-quiz", requestBody["quiz_id"])
        assertEquals("23AECMA-1EFKCI", requestBody["quiz_version_id"])
        assertEquals("34NCUIEI-214CDN", requestBody["quiz_session_id"])
        assertEquals("shirt-a", requestBody["item_id"])
        assertEquals("White shirt", requestBody["item_name"])
        assertEquals("shirt-a--reg", requestBody["variation_id"])
        assertEquals("129.99", requestBody["revenue"])
        assertEquals("quizConversion", requestBody["display_name"])
        assertEquals("quizConversion", requestBody["type"])
        assertEquals("true", requestBody["is_custom_type"])
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackQuizConversionTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackQuizConversionInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", "quizConversion", "quizConversion", true, "shirt-a", "shirt-a--reg", "White shirt", "Search Suggestions", "129.99").test();
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }

    @Test
    fun trackQuizResultLoad() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackQuizResultLoadInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", null, null, 1, 10).test()
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/quiz_result_load?section=Products&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.22.4&_dt="
        assertEquals("coffee-quiz", requestBody["quiz_id"])
        assertEquals("23AECMA-1EFKCI", requestBody["quiz_version_id"])
        assertEquals("34NCUIEI-214CDN", requestBody["quiz_session_id"])
        assertEquals("10", requestBody["result_count"])
        assertEquals("1", requestBody["result_page"])
        assertEquals("Not Available", requestBody["url"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackQuizResultLoadWithAllOptionalParameters() {
        val mockResponse = MockResponse().setResponseCode(204)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackQuizResultLoadInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", "Search Suggestions", "123", 1, 10).test();
        observer.assertComplete()
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/quiz_result_load?section=Search%20Suggestions&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.22.4&_dt="
        assertEquals("coffee-quiz", requestBody["quiz_id"])
        assertEquals("23AECMA-1EFKCI", requestBody["quiz_version_id"])
        assertEquals("34NCUIEI-214CDN", requestBody["quiz_session_id"])
        assertEquals("10", requestBody["result_count"])
        assertEquals("1", requestBody["result_page"])
        assertEquals("Not Available", requestBody["url"])
        assertEquals("123", requestBody["result_id"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackQuizResultLoad500() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        val observer = ConstructorIo.trackQuizResultLoadInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", "Search Suggestions", "123", 1, 10).test();
        observer.assertError{ true }
        val request = mockServer.takeRequest()
        val requestBody = getRequestBody(request)
        val path = "/v2/behavioral_action/quiz_result_load?section=Search%20Suggestions&key=copper-key&i=wacko-the-guid&ui=player-three&s=67&c=cioand-2.22.4&_dt="
        assertEquals("coffee-quiz", requestBody["quiz_id"])
        assertEquals("23AECMA-1EFKCI", requestBody["quiz_version_id"])
        assertEquals("34NCUIEI-214CDN", requestBody["quiz_session_id"])
        assertEquals("10", requestBody["result_count"])
        assertEquals("1", requestBody["result_page"])
        assertEquals("Not Available", requestBody["url"])
        assertEquals("123", requestBody["result_id"])
        assertEquals("POST", request.method)
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun trackQuizResultLoadTimeout() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockResponse.throttleBody(0, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = ConstructorIo.trackQuizResultLoadInternal("coffee-quiz", "23AECMA-1EFKCI", "34NCUIEI-214CDN", "Search Suggestions", "123", 1, 10).test();
        observer.assertError(SocketTimeoutException::class.java)
        val request = mockServer.takeRequest(10, TimeUnit.SECONDS)
        assertEquals(null, request)
    }
}
