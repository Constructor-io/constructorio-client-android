package io.constructor.core

import android.content.Context
import io.constructor.data.builder.RecommendationsRequest
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.common.VariationsMap
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.TestDataLoader
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstructorIoRecommendationsTest {

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

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.userId } returns "player-one"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("dummyKey")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getRecommendationResults() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_response.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getRecommendationResults("titanic").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val recommendationResponse = observer.values()[0].get()
        assertEquals(recommendationResponse?.response?.results?.size, 24)
        assertEquals(recommendationResponse?.response?.results!![0].value, "LaCroix Sparkling Water Pure Cans - 12-12 Fl. Oz.")
        assertEquals(recommendationResponse?.response?.results!![0].data.id, "960189161")
        assertEquals(recommendationResponse?.response?.results!![0].data.imageUrl, "https://d17bbgoo3npfov.cloudfront.net/images/farmstand-960189161.png")
        assertEquals(recommendationResponse?.response?.results!![0].data.metadata?.get("price"), 1.11)
        assertEquals(recommendationResponse?.response?.results!![0].strategy?.id, "bestsellers")
        assertEquals(recommendationResponse?.response?.resultCount, 225)

        val request = mockServer.takeRequest()
        val path = "/recommendations/v1/pods/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.35.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getRecommendationResultsWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getRecommendationResults("titanic").test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
        val path = "/recommendations/v1/pods/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.35.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getRecommendationResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_response.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getRecommendationResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
        val path = "/recommendations/v1/pods/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.35.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getRecommendationResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_response_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getRecommendationResults("titanic").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val recommendationResponse = observer.values()[0].get()
        assertTrue(recommendationResponse?.response?.results!!.isEmpty())
        assertEquals(recommendationResponse?.response?.resultCount, 0)

        val request = mockServer.takeRequest()
        val path = "/recommendations/v1/pods/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.35.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getRecommendationResultsUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_response.json"))
        mockServer.enqueue(mockResponse)
        val recommendationsRequest = RecommendationsRequest.Builder("titanic").build()
        val observer = constructorIo.getRecommendationResults(recommendationsRequest).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val recommendationResponse = observer.values()[0].get()
        assertEquals(recommendationResponse?.response?.results?.size, 24)
        assertEquals(recommendationResponse?.response?.results!![0].value, "LaCroix Sparkling Water Pure Cans - 12-12 Fl. Oz.")
        assertEquals(recommendationResponse?.response?.results!![0].data.id, "960189161")
        assertEquals(recommendationResponse?.response?.results!![0].data.imageUrl, "https://d17bbgoo3npfov.cloudfront.net/images/farmstand-960189161.png")
        assertEquals(recommendationResponse?.response?.results!![0].data.metadata?.get("price"), 1.11)
        assertEquals(recommendationResponse?.response?.resultCount, 225)

        val request = mockServer.takeRequest()
        val path = "/recommendations/v1/pods/titanic?key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.35.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getRecommendationResultsWithMultipleItemIdsUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_response.json"))
        mockServer.enqueue(mockResponse)
        val recommendationsRequest = RecommendationsRequest.Builder("titanic")
            .setItemIds(listOf("item_id_1", "item_id_2"))
            .build()
        val observer = constructorIo.getRecommendationResults(recommendationsRequest).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val recommendationResponse = observer.values()[0].get()
        assertEquals(recommendationResponse?.response?.results?.size, 24)
        assertEquals(recommendationResponse?.response?.results!![0].value, "LaCroix Sparkling Water Pure Cans - 12-12 Fl. Oz.")
        assertEquals(recommendationResponse?.response?.results!![0].data.id, "960189161")
        assertEquals(recommendationResponse?.response?.results!![0].data.imageUrl, "https://d17bbgoo3npfov.cloudfront.net/images/farmstand-960189161.png")
        assertEquals(recommendationResponse?.response?.results!![0].data.metadata?.get("price"), 1.11)
        assertEquals(recommendationResponse?.response?.resultCount, 225)

        val request = mockServer.takeRequest()
        val path = "/recommendations/v1/pods/titanic?item_id=item_id_1&item_id=item_id_2&key=golden-key&i=guido-the-guid&ui=player-one&s=79&c=cioand-2.35.2&_dt="
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getRecommendationResultsWithVariationsMapUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_response.json"))
        mockServer.enqueue(mockResponse)
        val variationsMap = VariationsMap(
                dtype = "array",
                values = mapOf(
                        "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                        "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
                ),
                groupBy = listOf(mapOf("name" to "Country", "field" to "data.facets.Country"))
        )
        val recommendationsRequest = RecommendationsRequest.Builder("titanic")
                .setVariationsMap(variationsMap)
                .build()
        val observer = constructorIo.getRecommendationResults(recommendationsRequest).test()
        observer.assertComplete().assertValue {
            var recommendationResponse = it.get()
            recommendationResponse?.response?.results?.isNotEmpty()!!
        }
        observer.assertNoErrors()

        val request = mockServer.takeRequest()
        assertThat(request.requestUrl!!.encodedPath).isEqualTo("/recommendations/v1/pods/titanic")
        with(request.requestUrl!!) {
            val queryParams = mapOf(
                    "variations_map" to """{"dtype":"array","values":{"Price":{"aggregation":"min","field":"data.facets.price"},"Country":{"aggregation":"all","field":"data.facets.country"}},"group_by":[{"name":"Country","field":"data.facets.Country"}]}""",
                    "key" to "golden-key",
                    "i" to "guido-the-guid",
                    "ui" to "player-one",
                    "s" to "79",
                    "c" to "cioand-2.35.2",
                    "_dt" to "1"
            )
            assertThat(queryParameterNames).containsExactlyInAnyOrderElementsOf(queryParams.keys)

            queryParams.forEach { (key, value) ->
                if (key == "_dt") {
                    assertThat(queryParameter(key)).containsOnlyDigits()
                } else {
                    assertThat(queryParameter(key)).isEqualTo(value)
                }
            }
        }
    }

    @Test
    fun getRecommendationResultsWithPreFilterExpressionUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_response.json"))
        mockServer.enqueue(mockResponse)
        val preFilterExpression = """{"and":[{"name":"Country","value":"US"}]}"""
        val recommendationsRequest = RecommendationsRequest.Builder("titanic")
                .setPreFilterExpression(preFilterExpression)
                .build()
        val observer = constructorIo.getRecommendationResults(recommendationsRequest).test()
        observer.assertComplete().assertValue {
            var recommendationResponse = it.get()
            recommendationResponse?.response?.results?.isNotEmpty()!!
        }
        observer.assertNoErrors()

        val request = mockServer.takeRequest()
        assertThat(request.requestUrl!!.encodedPath).isEqualTo("/recommendations/v1/pods/titanic")
        with(request.requestUrl!!) {
            val queryParams = mapOf(
                    "pre_filter_expression" to preFilterExpression,
                    "key" to "golden-key",
                    "i" to "guido-the-guid",
                    "ui" to "player-one",
                    "s" to "79",
                    "c" to "cioand-2.35.2",
                    "_dt" to "1"
            )
            assertThat(queryParameterNames).containsExactlyInAnyOrderElementsOf(queryParams.keys)

            queryParams.forEach { (key, value) ->
                if (key == "_dt") {
                    assertThat(queryParameter(key)).containsOnlyDigits()
                } else {
                    assertThat(queryParameter(key)).isEqualTo(value)
                }
            }
        }
    }

    @Test
    fun getRecommendationResultsWithHiddenFieldsUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200).setBody(TestDataLoader.loadAsString("recommendation_response.json"))
        mockServer.enqueue(mockResponse)
        val hiddenFields = listOf("hiddenField1", "hiddenField2")
        val recommendationsRequest = RecommendationsRequest.Builder("titanic")
        .setHiddenFields(hiddenFields)
        .build()

        val observer = constructorIo.getRecommendationResults(recommendationsRequest).test()

        observer.assertComplete().assertValue {
            var recommendationResponse = it.get()
            recommendationResponse?.response?.results?.isNotEmpty()!!
        }
        observer.assertNoErrors()

        val request = mockServer.takeRequest()
                assertThat(request.requestUrl!!.encodedPath).isEqualTo("/recommendations/v1/pods/titanic")
        with(request.requestUrl!!) {
            val queryParams = mapOf(
                    "fmt_options[hidden_fields]" to hiddenFields,
                    "key" to "golden-key",
                    "i" to "guido-the-guid",
                    "ui" to "player-one",
                    "s" to "79",
                    "c" to "cioand-2.35.2",
                    "_dt" to "1"
            )
            assertThat(queryParameterNames).containsExactlyInAnyOrderElementsOf(queryParams.keys)

            queryParams.forEach { (key, value) ->
                if (key == "fmt_options[hidden_fields]") {
                    assertThat(queryParameterValues(key)).containsExactlyInAnyOrderElementsOf(
                        hiddenFields
                    )
                } else if (key == "_dt") {
                    assertThat(queryParameter(key)).containsOnlyDigits()
                } else {
                    assertThat(queryParameter(key)).isEqualTo(value)
                }
            }
        }
    }
}
