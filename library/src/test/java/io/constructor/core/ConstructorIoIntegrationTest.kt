package io.constructor.core

import android.content.Context
import io.constructor.data.builder.AutocompleteRequest
import io.constructor.data.builder.BrowseRequest
import io.constructor.data.builder.RecommendationsRequest
import io.constructor.data.builder.SearchRequest
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.common.VariationsMap
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConstructorIoIntegrationTest {

    @Rule
    @JvmField
    val overrideSchedulersRule = RxSchedulersOverrideRule()

    private var constructorIo = ConstructorIo
    private val ctx = mockk<Context>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()
    private val timeBetweenTests = 2000.toLong()

    @Before
    fun setup() {
        every { ctx.applicationContext } returns ctx

        every { preferencesHelper.apiKey } returns "key_K2hlXt5aVSwoI1Uw"
        every { preferencesHelper.id } returns "wacko-the-guid"
        every { preferencesHelper.scheme } returns "https"
        every { preferencesHelper.serviceUrl } returns "ac.cnstrc.com"
        every { preferencesHelper.port } returns 443
        every { preferencesHelper.defaultItemSection } returns "Products"
        every { preferencesHelper.getSessionId(any(), any()) } returns 67

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.userId } returns "player-three"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("key_K2hlXt5aVSwoI1Uw")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getAutocompleteResultsAgainstRealResponse() {
        val observer = constructorIo.getAutocompleteResults("pork").test()
        observer.assertComplete().assertValue {
            it.get()?.sections!!.isNotEmpty()
            it.get()?.resultId!!.isNotEmpty()
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsWithFiltersAgainstRealResponse() {
        val facet = hashMapOf("storeLocation" to listOf("CA"))
        val observer =
            constructorIo.getAutocompleteResults("pork", facet?.map { it.key to it.value }).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsCRTAgainstRealResponse() {
        runBlocking {
            launch {
                try {
                    val autocompleteResults = constructorIo.getAutocompleteResultsCRT("pork")
                    assertTrue(autocompleteResults.sections!!.isNotEmpty())
                    assertTrue(autocompleteResults.resultId!!.isNotEmpty())
                } catch (e: Exception) {
                    assertNull(e)
                }
            }
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsCRTWithFiltersAgainstRealResponse() {
        runBlocking {
            launch {
                try {
                    val facet = hashMapOf("storeLocation" to listOf("CA"))
                    val autocompleteResults = constructorIo.getAutocompleteResultsCRT("pork", facet?.map { it.key to it.value })
                    assertTrue(autocompleteResults.sections!!.isNotEmpty())
                    assertTrue(autocompleteResults.resultId!!.isNotEmpty())
                } catch (e: Exception) {
                    assertNull(e)
                }
            }
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackSessionStartAgainstRealResponse() {
        val observer = constructorIo.trackSessionStartInternal().test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackInputFocusAgainstRealResponse() {
        val observer = constructorIo.trackInputFocusInternal("pork").test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsAgainstRealResponse() {
        val observer = constructorIo.getSearchResults("pork").test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.filterSortOptions!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
        }
    }

    @Test
    fun getSearchResultsWithFiltersAgainstRealResponse() {
        val facet = hashMapOf("storeLocation" to listOf("CA"))
        val observer =
            constructorIo.getSearchResults("pork", facet?.map { it.key to it.value }).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsCRTAgainstRealResponse() {
        runBlocking {
            launch {
                try {
                    val searchResults = constructorIo.getSearchResultsCRT("pork")
                    assertTrue(searchResults.resultId !== null)
                    assertTrue(searchResults.response!!.facets!!.isNotEmpty())
                    assertTrue(searchResults.response!!.groups!!.isNotEmpty())
                    assertTrue(searchResults.response!!.filterSortOptions!!.isNotEmpty())
                    assertTrue(searchResults.response!!.resultCount!! > 0)
                } catch (e: Exception) {
                    assertNull(e)
                }
            }
        }
        Thread.sleep(timeBetweenTests)
    }


    @Test
    fun getSearchResultsCRTWithFiltersAgainstRealResponse() {
        runBlocking {
            launch {
                try {
                    val facet = hashMapOf("Claims" to listOf("Organic"))
                    val searchResults = constructorIo.getSearchResultsCRT("pork", facet?.map { it.key to it.value })
                    assertTrue(searchResults.resultId !== null)
                    assertTrue(searchResults.response!!.facets!!.isNotEmpty())
                    assertTrue(searchResults.response!!.groups!!.isNotEmpty())
                    assertTrue(searchResults.response!!.filterSortOptions!!.isNotEmpty())
                    assertTrue(searchResults.response!!.resultCount!! > 0)
                } catch (e: Exception) {
                    assertNull(e)
                }
            }
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsAgainstRealResponse() {
        val observer = constructorIo.getBrowseResults("group_id", "744").test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.filterSortOptions!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithFiltersAgainstRealResponse() {
        val facet = hashMapOf("storeLocation" to listOf("CA"))
        val observer =
            constructorIo.getBrowseResults("group_ids", "544", facet?.map { it.key to it.value })
                .test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsCRTAgainstRealResponse() {
        runBlocking {
            launch {
                try {
                    val browseResults = constructorIo.getBrowseResultsCRT("group_id", "744")
                    assertTrue(browseResults.resultId !== null)
                    assertTrue(browseResults.response!!.facets!!.isNotEmpty())
                    assertTrue(browseResults.response!!.groups!!.isNotEmpty())
                    assertTrue(browseResults.response!!.filterSortOptions!!.isNotEmpty())
                    assertTrue(browseResults.response!!.resultCount!! > 0)
                } catch (e: Exception) {
                    assertNull(e)
                }
            }
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsCRTWithFiltersAgainstRealResponse() {
        runBlocking {
            launch {
                try {
                    val facet = hashMapOf("Claims" to listOf("Organic"))
                    val browseResults = constructorIo.getBrowseResultsCRT("group_id", "744", facet?.map { it.key to it.value })
                    assertTrue(browseResults.resultId !== null)
                    assertTrue(browseResults.response!!.facets!!.isNotEmpty())
                    assertTrue(browseResults.response!!.groups!!.isNotEmpty())
                    assertTrue(browseResults.response!!.filterSortOptions!!.isNotEmpty())
                    assertTrue(browseResults.response!!.resultCount!! > 0)
                } catch (e: Exception) {
                    assertNull(e)
                }
            }
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackAutocompleteSelectAgainstRealResponse() {
        val observer =
            constructorIo.trackAutocompleteSelectInternal("pork", "pork", "Search Suggestions")
                .test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackSearchSubmitAgainstRealResponse() {
        val observer = constructorIo.trackSearchSubmitInternal("pork", "pork", null).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackSearchResultClickAgainstRealResponse() {
        val observer = constructorIo.trackSearchResultClickInternal(
            "Boneless Pork Shoulder Roast",
            "prrst_shldr_bls",
            "pork"
        ).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackConversionAgainstRealResponse() {
        val observer = constructorIo.trackConversionInternal(
            "Boneless Pork Shoulder Roast",
            "prrst_shldr_bls",
            1.99
        ).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackPurchaseAgainstRealResponse() {
        val observer = constructorIo.trackPurchaseInternal(
            arrayOf("prrst_shldr_bls", "prrst_crwn"),
            9.98,
            "45273",
            "Products"
        ).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackBrowseResultsLoadedAgainstRealResponse() {
        val observer = constructorIo.trackBrowseResultsLoadedInternal("group_ids", "544", 46).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackBrowseResultClickAgainstRealResponse() {
        val observer =
            constructorIo.trackBrowseResultClickInternal("group_ids", "544", "prrst_shldr_bls", 5)
                .test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsAgainstRealResponse() {
        val facet = hashMapOf("Claims" to listOf("Organic"))
        val observer = constructorIo.getRecommendationResults("pdp3", facet?.map { it.key to it.value }).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.pod !== null
            it.get()?.response?.results !== null
            it.get()?.response?.resultCount!! >= 0
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsCRTAgainstRealResponse() {
        runBlocking {
            launch {
                try {
                    val facet = hashMapOf("Claims" to listOf("Organic"))
                    val recommendationResults = constructorIo.getRecommendationResultsCRT("pdp3", facet?.map { it.key to it.value })
                    assertTrue(recommendationResults.resultId !== null)
                    assertTrue(recommendationResults.response?.pod !== null)
                    assertTrue(recommendationResults.response?.results!!.isNotEmpty())
                    assertTrue(recommendationResults.response?.resultCount!! > 0)
                } catch (e: Exception) {
                    assertNull(e)
                }
            }
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackRecommendationResultClickAgainstRealResponse() {
        val observer = constructorIo.trackRecommendationResultClickInternal(
            "pdp5",
            "User Featured",
            "prrst_shldr_bls"
        ).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackRecommendationResultsViewAgainstRealResponse() {
        val observer = constructorIo.trackRecommendationResultsViewInternal("pdp5", 4).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsWithHiddenFieldsAgainstRealResponse() {
        val hiddenFields = listOf("hiddenField1", "hiddenField2")
        val observer = constructorIo.getAutocompleteResults("pork", null, null, hiddenFields).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.sections!!.isNotEmpty()
            it.get()?.sections?.get("Products")
                ?.first()?.data?.metadata?.get("hiddenField1") !== null
            it.get()?.sections?.get("Products")
                ?.first()?.data?.metadata?.get("hiddenField2") !== null
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithHiddenFieldsAgainstRealResponse() {
        val hiddenFields = listOf("hiddenField1", "hiddenField2")
        val observer = constructorIo.getSearchResults(
            "pork",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            hiddenFields
        ).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.results?.first()?.data?.metadata?.get("hiddenField1") !== null
            it.get()?.response?.results?.first()?.data?.metadata?.get("hiddenField2") !== null
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithHiddenFacetsAgainstRealResponse() {
        val hiddenFacets = listOf("Brand")
        val observer = constructorIo.getSearchResults(
            "pork",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            hiddenFacets
        ).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.facets!!.isNotEmpty()
            val brandFacet =
                it.get()?.response?.facets?.find { facet -> facet.name.contains("Brand") }
            brandFacet !== null
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithGroupsSortValueAscendingAgainstRealResponse() {
        val observer = constructorIo.getSearchResults(
            term = "pork",
            groupsSortBy = "value",
            groupsSortOrder = "ascending"
        ).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.groups?.get(0)?.displayName == "Dairy"
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithGroupsSortValueDescendingAgainstRealResponse() {
        val observer = constructorIo.getSearchResults(
            term = "pork",
            groupsSortBy = "value",
            groupsSortOrder = "descending"
        ).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.groups?.get(0)?.displayName == "Meat & Poultry"
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithHiddenFieldsAgainstRealResponse() {
        val hiddenFields = listOf("hiddenField1", "hiddenField2")
        val observer = constructorIo.getBrowseResults(
            "group_id",
            "431",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            hiddenFields
        ).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.results?.first()?.data?.metadata?.get("hiddenField1") !== null
            it.get()?.response?.results?.first()?.data?.metadata?.get("hiddenField2") !== null
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithHiddenFacetsAgainstRealResponse() {
        val hiddenFacets = listOf("Brand")
        val observer = constructorIo.getBrowseResults(
            "group_id",
            "431",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            hiddenFacets
        ).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.facets!!.isNotEmpty()
            val brandFacet =
                it.get()?.response?.facets?.find { facet -> facet.name.contains("Brand") }
            brandFacet !== null
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithCollectionAgainstRealResponse() {
        val observer = constructorIo.getBrowseResults("collection_id", "test-collection").test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.collection?.id == "test-collection"
            it.get()?.response?.collection?.displayName == "test collection"
        }
    }

    @Test
    fun getAutocompleteResultsAgainstRealResponseUsingRequestBuilder() {
        val request = AutocompleteRequest.Builder("pork").build()
        val observer = constructorIo.getAutocompleteResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.sections!!.isNotEmpty()
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsAgainstRealResponseWithVariationsMapArrayUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "array",
            values = mapOf(
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request =
            AutocompleteRequest.Builder("angus beef").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getAutocompleteResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.sections!!.isNotEmpty()
            val returnedVariationsMap =
                it.get()?.sections!!["Products"]?.get(0)?.variationsMap as List<*>
            returnedVariationsMap.isNotEmpty()
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsAgainstRealResponseWithVariationsMapObjectUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "object",
            values = mapOf(
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request =
            AutocompleteRequest.Builder("angus beef").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getAutocompleteResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.sections!!.isNotEmpty()
            val returnedVariationsMap =
                it.get()?.sections!!["Products"]?.get(0)?.variationsMap as Map<*, *>
            returnedVariationsMap.isNotEmpty()
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultAgainstRealResponseUsingRequestBuilder() {
        val request = SearchRequest.Builder("pork").build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.filterSortOptions!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultAgainstRealResponseWithVariationsMapArrayUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "array",
            values = mapOf(
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request = SearchRequest.Builder("angus beef").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.filterSortOptions!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
            val returnedVariationsMap = it.get()?.response?.results!![0].variationsMap as? List<*>
            returnedVariationsMap!!.isNotEmpty()
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultAgainstRealResponseWithVariationsMapObjectUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "object",
            values = mapOf(
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request = SearchRequest.Builder("angus beef").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.filterSortOptions!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
            val returnedVariationsMap = it.get()?.response?.results!![0].variationsMap as? Map<*, *>
            returnedVariationsMap!!.isNotEmpty()
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultAgainstRealResponseWithResultSources() {
        val request = SearchRequest.Builder("angus beef").build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.filterSortOptions!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
            it.get()?.response?.resultSources!!.embeddingsMatch!!.count!! >= 0
            it.get()?.response?.resultSources!!.tokenMatch!!.count!! >= 0
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseRequest.Builder("group_id", "431").build()
        val observer = constructorIo.getBrowseResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.filterSortOptions!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultAgainstRealResponseWithVariationsArrayMapUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "array",
            values = mapOf(
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request =
            BrowseRequest.Builder("group_id", "544").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getBrowseResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.filterSortOptions!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
            val returnedVariationsMap = it.get()?.response?.results!![0].variationsMap as? List<*>
            returnedVariationsMap!!.isNotEmpty()
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultAgainstRealResponseWithVariationsObjectMapUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "object",
            values = mapOf(
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request =
            BrowseRequest.Builder("group_id", "431").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getBrowseResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.filterSortOptions!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
            val returnedVariationsMap = it.get()?.response?.results!![0].variationsMap as? Map<*, *>
            returnedVariationsMap!!.isNotEmpty()
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultAgainstRealResponseWithResultSources() {
        val request = BrowseRequest.Builder("group_id", "431").build()
        val observer = constructorIo.getBrowseResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.filterSortOptions!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
            it.get()?.response?.resultSources!!.embeddingsMatch!!.count!! >= 0
            it.get()?.response?.resultSources!!.tokenMatch!!.count!! >= 0
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsAgainstRealResponseUsingRequestBuilder() {
        val request = RecommendationsRequest.Builder("pdp5").build()
        val observer = constructorIo.getRecommendationResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.pod !== null
            it.get()?.response?.results !== null
            it.get()?.response?.resultCount!! >= 0
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithGroupsSortValueAscendingAgainstRealResponse() {
        val observer = constructorIo.getBrowseResults(
            filterName = "group_id",
            filterValue = "431",
            groupsSortBy = "value",
            groupsSortOrder = "ascending",
        ).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.groups?.get(0)?.displayName == "Grocery"
            it.get()?.response?.groups?.get(0)?.children?.get(0)?.displayName == "Baby"
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithGroupsSortValueDescendingAgainstRealResponse() {
        val observer = constructorIo.getBrowseResults(
            filterName = "group_id",
            filterValue = "431",
            groupsSortBy = "value",
            groupsSortOrder = "descending",
        ).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.groups?.get(0)?.displayName == "Grocery"
            it.get()?.response?.groups?.get(0)?.children?.get(0)?.displayName == "Pet"
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsWithLabelsAgainstRealResponse() {
        val request = AutocompleteRequest.Builder("pork").build()
        val observer = constructorIo.getAutocompleteResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.sections!!.isNotEmpty()
            it.get()?.sections?.get("Products")?.first()?.labels!!["is_sponsored"] == true
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithLabelsAgainstRealResponse() {
        val request = SearchRequest.Builder("pork").build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
            it.get()?.response?.results?.first()?.labels!!["is_sponsored"] == true
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithLabelsAgainstRealResponse() {
        val request = BrowseRequest.Builder("group_id", "544").build()
        val observer = constructorIo.getBrowseResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.facets!!.isNotEmpty()
            it.get()?.response?.groups!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
            it.get()?.response?.results?.first()?.labels!!["is_sponsored"] == true
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsWithLabelsAgainstRealResponse() {
        val filters = mapOf("group_id" to listOf("544"))
        val request = RecommendationsRequest.Builder("pdp3").setFilters(filters).build()
        val observer = constructorIo.getRecommendationResults(request).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
            it.get()?.response?.results?.first()?.labels!!.isNullOrEmpty()
        }
        Thread.sleep(timeBetweenTests)
    }
}
