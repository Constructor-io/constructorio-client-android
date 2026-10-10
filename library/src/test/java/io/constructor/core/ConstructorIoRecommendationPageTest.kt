package io.constructor.core

import android.content.Context
import io.constructor.data.builder.RecommendationPagePodOverride
import io.constructor.data.builder.RecommendationPageRequest
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.TestDataLoader
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ConstructorIoRecommendationPageTest {

    @Rule
    @JvmField
    val overrideSchedulersRule = RxSchedulersOverrideRule()

    private lateinit var mockServer: MockWebServer
    private var constructorIo = ConstructorIo
    private val ctx = mockk<Context>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()
    private val pageResultId = "a1b2c3d4-0000-0000-0000-0000000000ff"

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

    private fun enqueuePageResponse() {
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_page_response.json")))
    }

    @Test
    fun getRecommendationPageResultsRequestsPageEndpointWithSharedParameters() {
        enqueuePageResponse()
        val request = RecommendationPageRequest.Builder("pdp_b2c")
            .setItemIds(listOf("product-123"))
            .setNumResults(10)
            .setFilters(mapOf("in_stock" to listOf("true")))
            .setFilterMatchTypes(mapOf("color" to "any"))
            .build()

        val observer = constructorIo.getRecommendationPageResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val url = mockServer.takeRequest().requestUrl!!
        assertEquals("/recommendations/v1/pages/pdp_b2c", url.encodedPath)
        assertEquals("golden-key", url.queryParameter("key"))
        assertEquals("guido-the-guid", url.queryParameter("i"))
        assertEquals("player-one", url.queryParameter("ui"))
        assertEquals("79", url.queryParameter("s"))
        assertEquals("product-123", url.queryParameter("item_id"))
        assertEquals("10", url.queryParameter("num_results"))
        assertEquals("true", url.queryParameter("filters[in_stock]"))
        assertEquals("any", url.queryParameter("filter_match_types[color]"))
        assertTrue(url.queryParameterNames.none { it.startsWith("pod_overrides") })
    }

    @Test
    fun getRecommendationPageResultsEncodesPodOverridesInBracketNotation() {
        enqueuePageResponse()
        val preFilterExpression = "{\"or\":[{\"name\":\"brand\",\"value\":\"acme\"}]}"
        val request = RecommendationPageRequest.Builder("pdp_b2c")
            .setItemIds(listOf("product-123"))
            .setNumResults(10)
            .setPodOverrides(mapOf(
                "similar_items" to RecommendationPagePodOverride(numResults = 0),
                "complete_the_look" to RecommendationPagePodOverride(
                    numResults = 8,
                    filters = mapOf("in_stock" to listOf("true"), "color" to listOf("red", "blue")),
                    filterMatchTypes = mapOf("color" to "all"),
                    preFilterExpression = preFilterExpression,
                    fmtOptions = mapOf("groups_max_depth" to 2),
                    hiddenFields = listOf("inventory", "margin"),
                ),
            ))
            .build()

        val observer = constructorIo.getRecommendationPageResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val recordedRequest = mockServer.takeRequest()
        val url = recordedRequest.requestUrl!!
        val prefix = "pod_overrides[complete_the_look]"
        assertEquals("10", url.queryParameter("num_results"))
        assertEquals("0", url.queryParameter("pod_overrides[similar_items][num_results]"))
        assertEquals("8", url.queryParameter("$prefix[num_results]"))
        assertEquals("true", url.queryParameter("$prefix[filters][in_stock]"))
        assertEquals(listOf("red", "blue"), url.queryParameterValues("$prefix[filters][color]"))
        assertEquals("all", url.queryParameter("$prefix[filter_match_types][color]"))
        assertEquals(preFilterExpression, url.queryParameter("$prefix[pre_filter_expression]"))
        assertEquals("2", url.queryParameter("$prefix[fmt_options][groups_max_depth]"))
        assertEquals(listOf("inventory", "margin"), url.queryParameterValues("$prefix[fmt_options][hidden_fields]"))
        assertTrue(recordedRequest.path!!.contains("pod_overrides%5Bsimilar_items%5D%5Bnum_results%5D=0"))
    }

    @Test
    fun getRecommendationPageResultsReturnsPerPodResultIds() {
        enqueuePageResponse()
        val observer = constructorIo.getRecommendationPageResults(RecommendationPageRequest.Builder("pdp_b2c").build()).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val page = observer.values()[0].get()!!
        val pods = page.response!!.pods!!
        assertEquals(pageResultId, page.resultId)
        assertEquals("pdp_b2c", page.response!!.pageId)
        assertEquals("pdp", page.response!!.pageType)
        assertEquals(listOf("similar_items", "complete_the_look"), pods.map { it.podId })
        assertEquals("a1b2c3d4-0000-0000-0000-000000000001", pods[0].resultId)
        assertEquals("Red Running Shoe", pods[0].response?.results?.get(0)?.value)
        assertEquals(1, pods[0].response?.resultCount)
        assertEquals("a1b2c3d4-0000-0000-0000-000000000002", pods[1].resultId)
        assertEquals(0, pods[1].response?.results?.size)
        pods.forEach { assertNotEquals(pageResultId, it.resultId) }

        val asPodResponse = pods[0].toRecommendationsResponse()
        assertEquals("a1b2c3d4-0000-0000-0000-000000000001", asPodResponse.resultId)
        assertEquals("similar_items", asPodResponse.request?.get("pod_id"))
        assertEquals(pods[0].response, asPodResponse.response)
        mockServer.takeRequest()
    }

    @Test
    fun getRecommendationPageResultsWithServerError() {
        mockServer.enqueue(MockResponse().setResponseCode(404).setBody("{\"message\":\"Recommendation page \\\"pdp_b2c\\\" was not found.\"}"))
        val observer = constructorIo.getRecommendationPageResults(RecommendationPageRequest.Builder("pdp_b2c").build()).test()
        observer.assertComplete().assertValue { it.networkError }
        assertEquals("/recommendations/v1/pages/pdp_b2c", mockServer.takeRequest().requestUrl!!.encodedPath)
    }

    @Test
    fun getRecommendationPageResultsCRTReturnsPerPodResultIds() {
        enqueuePageResponse()
        val page = runBlocking {
            constructorIo.getRecommendationPageResultsCRT(RecommendationPageRequest.Builder("pdp_b2c").setItemIds(listOf("product-123")).build())
        }

        assertEquals(pageResultId, page.resultId)
        assertEquals("a1b2c3d4-0000-0000-0000-000000000001", page.response?.pods?.get(0)?.resultId)
        assertEquals("/recommendations/v1/pages/pdp_b2c", mockServer.takeRequest().requestUrl!!.encodedPath)
    }

    @Test
    fun getRecommendationPageResultsRejectsVariationIdWithoutSingleItemId() {
        val request = RecommendationPageRequest.Builder("pdp_b2c").setVariationId("v1").build()

        assertFailsWith<IllegalArgumentException> {
            constructorIo.getRecommendationPageResults(request)
        }
    }
}
