package io.constructor.core

import android.content.Context
import io.constructor.data.builder.*
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.common.VariationsMap
import io.constructor.data.model.purchase.PurchaseItem
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
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

        every { preferencesHelper.apiKey } returns "ZqXaOfXuBWD4s3XzCI1q"
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

        val config = ConstructorIoConfig("ZqXaOfXuBWD4s3XzCI1q")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getAutocompleteResultsAgainstRealResponse() {
        val observer = constructorIo.getAutocompleteResults("item").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val autocompleteResponse = observer.values()[0].get()
        assertTrue(autocompleteResponse?.sections!!.isNotEmpty())
        assertTrue(autocompleteResponse?.resultId!!.isNotEmpty())
        assertEquals(autocompleteResponse?.sections!!["Products"]?.first()?.isSlotted, true)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsWithFiltersAgainstRealResponse() {
        val facet = hashMapOf("Brand" to listOf("XYZ"))
        val observer =
            constructorIo.getAutocompleteResults("item", facet.map { it.key to it.value }).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsCRTAgainstRealResponse() {
        runBlocking {
            val autocompleteResults = constructorIo.getAutocompleteResultsCRT("item")
            assertTrue(autocompleteResults.sections!!.isNotEmpty())
            assertTrue(autocompleteResults.resultId!!.isNotEmpty())
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsCRTWithFiltersAgainstRealResponse() {
        runBlocking {
            val facet = hashMapOf("Brand" to listOf("XYZ"))
            val autocompleteResults = constructorIo.getAutocompleteResultsCRT("item", facet.map { it.key to it.value })
            assertTrue(autocompleteResults.sections!!.isNotEmpty())
            assertTrue(autocompleteResults.resultId!!.isNotEmpty())
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsCRTWithVariationsMapAgainstRealResponse() {
        runBlocking {
            val variationsMap = VariationsMap(
                    dtype = "array",
                    values = mapOf(
                        "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
                    ),
                    groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
            )
            val autocompleteResults = constructorIo.getAutocompleteResultsCRT("Jacket", variationsMap = variationsMap)
            val returnedVariationsMap = autocompleteResults?.sections!!["Products"]?.get(0)?.variationsMap as List<*>
            assertTrue(autocompleteResults.sections!!.isNotEmpty())
            assertTrue(autocompleteResults.resultId!!.isNotEmpty())
            assertTrue(returnedVariationsMap.isNotEmpty())
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
        val observer = constructorIo.trackInputFocusInternal("item").test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsAgainstRealResponse() {
        val observer = constructorIo.getSearchResults("item").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId!!.isNotEmpty())
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.facets!!.isNotEmpty())
        assertTrue(searchResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)
        assertEquals(searchResponse?.response?.results?.first()?.isSlotted, true)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithFiltersAgainstRealResponse() {
        val facet = hashMapOf("Color" to listOf("Blue"))
        val observer =
            constructorIo.getSearchResults("item1", facet.map { it.key to it.value }).test()
        observer.assertComplete()
        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId!!.isNotEmpty())
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.facets!!.isNotEmpty())
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
        assertTrue(searchResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsCRTAgainstRealResponse() {
        runBlocking {
            val searchResults = constructorIo.getSearchResultsCRT("item1")
            assertTrue(searchResults.resultId !== null)
            assertTrue(searchResults.response!!.facets!!.isNotEmpty())
            assertTrue(searchResults.response!!.groups!!.isNotEmpty())
            assertTrue(searchResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(searchResults.response!!.resultCount!! > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsCRTWithFiltersAgainstRealResponse() {
        runBlocking {
            val facet = hashMapOf("Brand" to listOf("XYZ"))
            val searchResults = constructorIo.getSearchResultsCRT("item1", facet.map { it.key to it.value })
            assertTrue(searchResults.resultId !== null)
            assertTrue(searchResults.response!!.facets!!.isNotEmpty())
            assertTrue(searchResults.response!!.groups!!.isNotEmpty())
            assertTrue(searchResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(searchResults.response!!.resultCount!! > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsCRTWithVariationsMapAgainstRealResponse() {
        runBlocking {
            val variationsMap = VariationsMap(
                dtype = "array",
                values = mapOf(
                    "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
                ),
                groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
            )
            val searchResults = constructorIo.getSearchResultsCRT("jacket", variationsMap = variationsMap)
            val returnedVariationsMap = searchResults?.response?.results!![0].variationsMap as? List<*>
            assertTrue(searchResults.resultId !== null)
            assertTrue(searchResults.response!!.results!!.isNotEmpty())
            assertTrue(searchResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(searchResults.response!!.resultCount!! > 0)
            assertTrue(returnedVariationsMap!!.isNotEmpty())
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsAgainstRealResponse() {
        val observer = constructorIo.getBrowseResults("Brand", "XYZ").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId!!.isNotEmpty())
        assertTrue(browseResponse?.response?.results!!.isNotEmpty())
        assertTrue(browseResponse?.response?.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response?.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(browseResponse?.response?.resultCount!! > 0)
        assertEquals(browseResponse?.response?.results?.first()?.isSlotted, true)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithFiltersAgainstRealResponse() {
        val facet = hashMapOf("storeLocation" to listOf("CA"))
        val observer =
            constructorIo.getBrowseResults("group_ids", "544", facet.map { it.key to it.value })
                .test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsCRTAgainstRealResponse() {
        runBlocking {
            val browseResults = constructorIo.getBrowseResultsCRT("Brand", "XYZ")
            assertTrue(browseResults.resultId !== null)
            assertTrue(browseResults.response!!.facets!!.isNotEmpty())
            assertTrue(browseResults.response!!.groups!!.isNotEmpty())
            assertTrue(browseResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(browseResults.response!!.resultCount > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsCRTWithFiltersAgainstRealResponse() {
        runBlocking {
            val facet = hashMapOf("Color" to listOf("red"))
            val browseResults = constructorIo.getBrowseResultsCRT("Brand", "XYZ", facet.map { it.key to it.value })
            assertTrue(browseResults.resultId !== null)
            assertTrue(browseResults.response!!.facets!!.isNotEmpty())
            assertTrue(browseResults.response!!.groups!!.isNotEmpty())
            assertTrue(browseResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(browseResults.response!!.resultCount > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsCRTWithVariationsMapAgainstRealResponse() {
        runBlocking {
            val variationsMap = VariationsMap(
                    dtype = "array",
                    values = mapOf(
                            "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
                    ),
                    groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
            )
            val browseResults = constructorIo.getBrowseResultsCRT("Brand", "XYZ", variationsMap = variationsMap)
            val returnedVariationsMap = browseResults?.response?.results!![0].variationsMap as? List<*>
            assertTrue(browseResults.resultId !== null)
            assertTrue(browseResults.response!!.facets!!.isNotEmpty())
            assertTrue(browseResults.response!!.groups!!.isNotEmpty())
            assertTrue(browseResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(browseResults.response!!.resultCount > 0)
            assertTrue(returnedVariationsMap!!.isNotEmpty())
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseItemsResultsCRTAgainstRealResponse() {
        runBlocking {
            val browseResults = constructorIo.getBrowseItemsResultsCRT(listOf("10001"))
            assertTrue(browseResults.resultId !== null)
            assertTrue(browseResults.response!!.groups!!.isNotEmpty())
            assertTrue(browseResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(browseResults.response!!.resultCount > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseItemsResultsWithHiddenFacetsAgainstRealResponseWithRequestBuilder() {
        val hiddenFacets = listOf("Brand")
        val request = BrowseItemsRequest.Builder(listOf("10001"))
            .setHiddenFacets(hiddenFacets)
            .build()
        val observer = constructorIo.getBrowseItemsResults(request).test()

        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        val brandFacet = browseResponse?.response?.facets?.find { facet -> facet.name.contains("Brand")}
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.facets!!.isNotEmpty())
        assertTrue(brandFacet !== null)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseItemsResultsAgainstRealResponseWithVariationsMapObjectUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "object",
            values = mapOf(
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request = BrowseItemsRequest.Builder(listOf("10001")).setVariationsMap(variationsMap).build()
        val observer = constructorIo.getBrowseItemsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        val returnedVariationsMap = browseResponse?.response?.results!![0].variationsMap as? Map<*, *>
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.results!!.isNotEmpty())
        assertTrue(browseResponse?.response?.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response?.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(browseResponse?.response?.resultCount!! > 0)
        assertTrue(returnedVariationsMap!!.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseItemsResultsCRTWithFiltersAgainstRealResponse() {
        runBlocking {
            val facet = hashMapOf("Brand" to listOf("XYZ"))
            val browseResults = constructorIo.getBrowseItemsResultsCRT(listOf("10001"), facet.map { it.key to it.value })
            assertTrue(browseResults.resultId !== null)
            assertTrue(browseResults.response!!.facets!!.isNotEmpty())
            assertTrue(browseResults.response!!.groups!!.isNotEmpty())
            assertTrue(browseResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(browseResults.response!!.resultCount > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseItemsResultsAgainstRealResponseUsingRequestBuilder() {
        runBlocking {
            val request = BrowseItemsRequest.Builder(listOf("10001")).build()
            val observer = constructorIo.getBrowseItemsResults(request).test()
            observer.assertComplete()
            observer.assertNoErrors()
            val browseResponse = observer.values()[0].get()

            assertTrue(browseResponse?.resultId !== null)
            assertTrue(browseResponse?.response!!.groups!!.isNotEmpty())
            assertTrue(browseResponse?.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(browseResponse?.response!!.resultCount > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseItemsResultsCRTWithFiltersAgainstRealResponseUsingRequestBuilder() {
        runBlocking {
            val facet = hashMapOf("Brand" to listOf("XYZ"))
            val browseResults = constructorIo.getBrowseItemsResultsCRT(listOf("10001"), facet.map { it.key to it.value })
            assertTrue(browseResults.resultId !== null)
            assertTrue(browseResults.response!!.facets!!.isNotEmpty())
            assertTrue(browseResults.response!!.groups!!.isNotEmpty())
            assertTrue(browseResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(browseResults.response!!.resultCount > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsAgainstRealResponse() {
        val observer = constructorIo.getBrowseFacetsResults().test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.resultCount > 0)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsWithPageAgainstRealResponse() {
        val observer = constructorIo.getBrowseFacetsResults(page = 10).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isEmpty())
        assertTrue(browseResponse?.response!!.resultCount > 0)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsWithNumResultsPerPageAgainstRealResponse() {
        val observer = constructorIo.getBrowseFacetsResults(perPage = 1).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.size == 1)
        assertTrue(browseResponse?.response!!.resultCount > 0)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsWithShowHiddenFacetsAgainstRealResponse() {
        val observer = constructorIo.getBrowseFacetsResults(showHiddenFacets = true).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.resultCount > 2)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsWithOffsetAgainstRealResponse() {
        val observer = constructorIo.getBrowseFacetsResults(offset = 100).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isEmpty())
        assertTrue(browseResponse?.response!!.resultCount > 0)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseFacetsRequest.Builder().build()
        val observer = constructorIo.getBrowseFacetsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.resultCount > 0)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsWithPageAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseFacetsRequest.Builder()
                .setPage(10)
                .build()
        val observer = constructorIo.getBrowseFacetsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isEmpty())
        assertTrue(browseResponse?.response!!.resultCount > 0)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsWithNumResultsPerPageAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseFacetsRequest.Builder()
                .setNumResultsPerPage(1)
                .build()
        val observer = constructorIo.getBrowseFacetsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.size == 1)
        assertTrue(browseResponse?.response!!.resultCount > 0)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsWithShowHiddenFacetsAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseFacetsRequest.Builder()
                .setShowHiddenFacets(true)
                .build()
        val observer = constructorIo.getBrowseFacetsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.resultCount > 2)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsWithOffsetAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseFacetsRequest.Builder()
                .setOffset(100)
                .build()
        val observer = constructorIo.getBrowseFacetsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isEmpty())
        assertTrue(browseResponse?.response!!.resultCount > 0)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsCRTWithOffsetAgainstRealResponseUsingRequestBuilder() {
        runBlocking {
            val browseFacetsResponse = constructorIo.getBrowseFacetsResultsCRT(offset = 10)

            assertTrue(browseFacetsResponse?.resultId !== null)
            assertTrue(browseFacetsResponse?.response!!.facets!!.isEmpty())
            assertTrue(browseFacetsResponse?.response!!.resultCount > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsCRTWithPageAgainstRealResponseUsingRequestBuilder() {
        runBlocking {
            val browseFacetsResponse = constructorIo.getBrowseFacetsResultsCRT(page = 10)

            assertTrue(browseFacetsResponse?.resultId !== null)
            assertTrue(browseFacetsResponse?.response!!.facets!!.isEmpty())
            assertTrue(browseFacetsResponse?.response!!.resultCount > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsCRTWithNumResultsPerPageAgainstRealResponseUsingRequestBuilder() {
        runBlocking {
            val browseFacetsResponse = constructorIo.getBrowseFacetsResultsCRT(perPage = 1)

            assertTrue(browseFacetsResponse?.resultId !== null)
            assertTrue(browseFacetsResponse?.response!!.facets!!.size == 1)
            assertTrue(browseFacetsResponse?.response!!.resultCount > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetsResultsCRTWithShowHiddenFacetsAgainstRealResponseUsingRequestBuilder() {
        runBlocking {
            val browseFacetsResponse = constructorIo.getBrowseFacetsResultsCRT(showHiddenFacets = true)

            assertTrue(browseFacetsResponse?.resultId !== null)
            assertTrue(browseFacetsResponse?.response!!.facets!!.isNotEmpty())
            assertTrue(browseFacetsResponse?.response!!.resultCount > 2)
        }
        Thread.sleep(timeBetweenTests)
    }


    @Test
    fun getBrowseFacetOptionsResultsAgainstRealResponse() {
        val observer = constructorIo.getBrowseFacetOptionsResults("color").test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.facets!![0].options!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.facets!![0].displayName == "color")
        assertTrue(browseResponse?.response!!.facets!![0].name == "color")
        assertTrue(browseResponse?.response!!.facets!![0].type == "multiple")
        assertTrue(browseResponse?.response!!.facets!![0].hidden == false)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetOptionsResultsWithShowHiddenFacetsAgainstRealResponse() {
        val observer = constructorIo.getBrowseFacetOptionsResults("Brand", showHiddenFacets = true).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.facets!![0].options!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.facets!![0].displayName == "Brand")
        assertTrue(browseResponse?.response!!.facets!![0].name == "Brand")
        assertTrue(browseResponse?.response!!.facets!![0].type == "multiple")
        assertTrue(browseResponse?.response!!.facets!![0].hidden == true)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetOptionsResultsAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseFacetOptionsRequest.Builder("color").build()
        val observer = constructorIo.getBrowseFacetOptionsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.facets!![0].options!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.facets!![0].displayName == "color")
        assertTrue(browseResponse?.response!!.facets!![0].name == "color")
        assertTrue(browseResponse?.response!!.facets!![0].type == "multiple")
        assertTrue(browseResponse?.response!!.facets!![0].hidden == false)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetOptionsResultsWithShowHiddenFacetsAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseFacetOptionsRequest.Builder("Brand")
                .setShowHiddenFacets(true)
                .build()
        val observer = constructorIo.getBrowseFacetOptionsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.facets!![0].options!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.facets!![0].displayName == "Brand")
        assertTrue(browseResponse?.response!!.facets!![0].name == "Brand")
        assertTrue(browseResponse?.response!!.facets!![0].type == "multiple")
        assertTrue(browseResponse?.response!!.facets!![0].hidden == true)
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetOptionsResultsCRTAgainstRealResponse() {
        runBlocking {
            val browseFacetOptionsResponse = constructorIo.getBrowseFacetOptionsResultsCRT("color")

            assertTrue(browseFacetOptionsResponse?.resultId !== null)
            assertTrue(browseFacetOptionsResponse?.response!!.facets!!.isNotEmpty())
            assertTrue(browseFacetOptionsResponse?.response!!.facets!![0].options!!.isNotEmpty())
            assertTrue(browseFacetOptionsResponse?.response!!.facets!![0].displayName == "color")
            assertTrue(browseFacetOptionsResponse?.response!!.facets!![0].name == "color")
            assertTrue(browseFacetOptionsResponse?.response!!.facets!![0].type == "multiple")
            assertTrue(browseFacetOptionsResponse?.response!!.facets!![0].hidden == false)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetOptionsResultsCRTWithShowHiddenFacetsAgainstRealResponse() {
        runBlocking {
            val browseFacetOptionsResponse = constructorIo.getBrowseFacetOptionsResultsCRT("Brand", true)

            assertTrue(browseFacetOptionsResponse?.resultId !== null)
            assertTrue(browseFacetOptionsResponse?.response!!.facets!!.isNotEmpty())
            assertTrue(browseFacetOptionsResponse?.response!!.facets!![0].options!!.isNotEmpty())
            assertTrue(browseFacetOptionsResponse?.response!!.facets!![0].displayName == "Brand")
            assertTrue(browseFacetOptionsResponse?.response!!.facets!![0].name == "Brand")
            assertTrue(browseFacetOptionsResponse?.response!!.facets!![0].type == "multiple")
            assertTrue(browseFacetOptionsResponse?.response!!.facets!![0].hidden == true)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseGroupsResultsAgainstRealResponse() {
        val observer = constructorIo.getBrowseGroupsResults().test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.groups!![0].displayName == "All")
        assertTrue(browseResponse?.response!!.groups!![0].groupId == "All")
        assertTrue(browseResponse?.response!!.groups!![0].count == 10)
        assertTrue(browseResponse?.response!!.groups!![0].children!!.isNotEmpty())
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseGroupsResultsWithGroupIdAgainstRealResponse() {
        val observer = constructorIo.getBrowseGroupsResults("Brands").test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.groups!![0].displayName == "Brands")
        assertTrue(browseResponse?.response!!.groups!![0].groupId == "Brands")
        assertTrue(browseResponse?.response!!.groups!![0].count == 6)
        assertTrue(browseResponse?.response!!.groups!![0].children!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.groups!![0].parents!!.isNotEmpty())
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseGroupsResultsWithGroupsMaxDepthAgainstRealResponse() {
        val observer = constructorIo.getBrowseGroupsResults(groupsMaxDepth = 0).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.groups!![0].displayName == "All")
        assertTrue(browseResponse?.response!!.groups!![0].groupId == "All")
        assertTrue(browseResponse?.response!!.groups!![0].count == 10)
        assertTrue(browseResponse?.response!!.groups!![0].children!!.isEmpty())
        assertTrue(browseResponse?.response!!.groups!![0].parents!!.isEmpty())
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetGroupsAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseGroupsRequest.Builder().build()
        val observer = constructorIo.getBrowseGroupsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.groups!![0].displayName == "All")
        assertTrue(browseResponse?.response!!.groups!![0].groupId == "All")
        assertTrue(browseResponse?.response!!.groups!![0].count == 10)
        assertTrue(browseResponse?.response!!.groups!![0].children!!.isNotEmpty())
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetGroupsWithGroupIdAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseGroupsRequest.Builder()
                .setGroupId("Brands")
                .build()
        val observer = constructorIo.getBrowseGroupsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.groups!![0].displayName == "Brands")
        assertTrue(browseResponse?.response!!.groups!![0].groupId == "Brands")
        assertTrue(browseResponse?.response!!.groups!![0].count == 6)
        assertTrue(browseResponse?.response!!.groups!![0].children!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.groups!![0].parents!!.isNotEmpty())
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseFacetGroupsWithGroupsMaxDepthAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseGroupsRequest.Builder()
                .setGroupsMaxDepth(0)
                .build()
        val observer = constructorIo.getBrowseGroupsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()
        val browseResponse = observer.values()[0].get()

        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response!!.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response!!.groups!![0].displayName == "All")
        assertTrue(browseResponse?.response!!.groups!![0].groupId == "All")
        assertTrue(browseResponse?.response!!.groups!![0].count == 10)
        assertTrue(browseResponse?.response!!.groups!![0].children!!.isEmpty())
        assertTrue(browseResponse?.response!!.groups!![0].parents!!.isEmpty())
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseGroupsResultsCRTAgainstRealResponse() {
        runBlocking {
            val browseResponse = constructorIo.getBrowseGroupsResultsCRT()

            assertTrue(browseResponse?.resultId !== null)
            assertTrue(browseResponse?.resultId !== null)
            assertTrue(browseResponse?.response!!.groups!!.isNotEmpty())
            assertTrue(browseResponse?.response!!.groups!![0].displayName == "All")
            assertTrue(browseResponse?.response!!.groups!![0].groupId == "All")
            assertTrue(browseResponse?.response!!.groups!![0].count == 10)
            assertTrue(browseResponse?.response!!.groups!![0].children!!.isNotEmpty())
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseGroupsResultsCRTWithGroupIdAgainstRealResponse() {
        runBlocking {
            val browseResponse = constructorIo.getBrowseGroupsResultsCRT(groupId = "Brands")

            assertTrue(browseResponse?.resultId !== null)
            assertTrue(browseResponse?.response!!.groups!!.isNotEmpty())
            assertTrue(browseResponse?.response!!.groups!![0].displayName == "Brands")
            assertTrue(browseResponse?.response!!.groups!![0].groupId == "Brands")
            assertTrue(browseResponse?.response!!.groups!![0].count == 6)
            assertTrue(browseResponse?.response!!.groups!![0].children!!.isNotEmpty())
            assertTrue(browseResponse?.response!!.groups!![0].parents!!.isNotEmpty())
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseGroupsResultsCRTWithGroupsMaxDepthAgainstRealResponse() {
        runBlocking {
            val browseResponse = constructorIo.getBrowseGroupsResultsCRT(groupsMaxDepth = 0)

            assertTrue(browseResponse?.resultId !== null)
            assertTrue(browseResponse?.response!!.groups!!.isNotEmpty())
            assertTrue(browseResponse?.response!!.groups!![0].displayName == "All")
            assertTrue(browseResponse?.response!!.groups!![0].groupId == "All")
            assertTrue(browseResponse?.response!!.groups!![0].count == 10)
            assertTrue(browseResponse?.response!!.groups!![0].children!!.isEmpty())
            assertTrue(browseResponse?.response!!.groups!![0].parents!!.isEmpty())
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
            null,
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
            null,
            1.99
        ).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackConversionWithConversionTypeAgainstRealResponse() {
        val observer = constructorIo.trackConversionInternal(
                "Boneless Pork Shoulder Roast",
                "prrst_shldr_bls",
                null,
                1.99,
                conversionType = "add_to_cart",
        ).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackPurchaseAgainstRealResponse() {
        val observer = constructorIo.trackPurchaseInternal(
            arrayOf(PurchaseItem("prrst_shldr_bls"), PurchaseItem("prrst_crwn")),
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
            constructorIo.trackBrowseResultClickInternal("group_ids", "544", "prrst_shldr_bls",null,5)
                .test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackBrowseResultClickWithResultIdAgainstRealResponse() {
        val observer =
                constructorIo.trackBrowseResultClickInternal("group_ids", "544", "prrst_shldr_bls",null,5, "Products", "123")
                        .test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackItemDetailLoadedAgainstRealResponse() {
        val observer = constructorIo.trackItemDetailLoadedInternal("Pencil", "1234").test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackItemDetailLoadedWithOptionalParamsAgainstRealResponse() {
        val observer = constructorIo.trackItemDetailLoadedInternal("Pencil", "1234", "456", "Products", "test.com").test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackGenericResultClickAgainstRealResponse() {
        val observer = constructorIo.trackGenericResultClickInternal("Pencil", "1234", "456").test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsAgainstRealResponse() {
        val facet = hashMapOf("Brand" to listOf("XYZ"))
        val observer = constructorIo.getRecommendationResults("home_page_1", facet.map { it.key to it.value }).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val recommendationResponse = observer.values()[0].get()
        assertTrue(recommendationResponse?.resultId !== null)
        assertTrue(recommendationResponse?.response?.pod !== null)
        assertTrue(recommendationResponse?.response?.results !== null)
        assertTrue(recommendationResponse?.response?.resultCount!! >= 0)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsCRTAgainstRealResponse() {
        runBlocking {
            val facet = hashMapOf("Brand" to listOf("XYZ"))
            val recommendationResults = constructorIo.getRecommendationResultsCRT("home_page_1", facet.map { it.key to it.value })
            assertTrue(recommendationResults.resultId !== null)
            assertTrue(recommendationResults.response?.pod !== null)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsCRTWithVariationsMapAgainstRealResponse() {
        runBlocking {
            val facet = hashMapOf("Brand" to listOf("XYZ"))
            val variationsMap = VariationsMap(
                    dtype = "array",
                    values = mapOf(
                            "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
                    ),
                    groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
            )
            val recommendationResults = constructorIo.getRecommendationResultsCRT("filtered_items", facets = facet.map { it.key to it.value }, variationsMap = variationsMap)
            val returnedVariationsMap = recommendationResults?.response?.results!![0].variationsMap as? List<*>
            assertTrue(recommendationResults.resultId !== null)
            assertTrue(recommendationResults.response?.pod !== null)
            assertTrue(recommendationResults.response?.results !== null)
            assertTrue(recommendationResults.response?.resultCount!! >= 0)
            assertTrue(returnedVariationsMap!!.isNotEmpty())
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackRecommendationResultClickAgainstRealResponse() {
        val observer = constructorIo.trackRecommendationResultClickInternal(
            "home_page_1",
            "Use Featured",
            "prrst_shldr_bls"
        ).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackRecommendationResultsViewAgainstRealResponse() {
        val observer = constructorIo.trackRecommendationResultsViewInternal("home_page_1", 4).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsWithHiddenFieldsAgainstRealResponse() {
        val hiddenFields = listOf("testField", "hiddenField2")
        val observer = constructorIo.getAutocompleteResults("item1", null, null, hiddenFields).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val autocompleteResponse = observer.values()[0].get()
        assertTrue(autocompleteResponse?.resultId !== null)
        assertTrue(autocompleteResponse?.sections!!.isNotEmpty())
        assertTrue(autocompleteResponse?.sections?.get("Products")?.first()?.data?.metadata?.get("testField") !== null)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithHiddenFieldsAgainstRealResponse() {
        val hiddenFields = listOf("testField", "hiddenField2")
        val observer = constructorIo.getSearchResults(
            term =  "item1",
            hiddenFields = hiddenFields
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.results?.first()?.data?.metadata?.get("testField") !== null)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithHiddenFacetsAgainstRealResponse() {
        val hiddenFacets = listOf("Brand")
        val observer = constructorIo.getSearchResults(
            term = "item1",
            hiddenFacets = hiddenFacets
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        val brandFacet = searchResponse?.response?.facets?.find { facet -> facet.name.contains("Brand") }
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.facets!!.isNotEmpty())
        assertTrue(brandFacet !== null)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithGroupsSortValueAscendingAgainstRealResponse() {
        val observer = constructorIo.getSearchResults(
            term = "item1",
            groupsSortBy = "value",
            groupsSortOrder = "ascending"
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
        assertEquals(searchResponse?.response?.groups?.get(0)?.displayName, "All")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithGroupsSortValueDescendingAgainstRealResponse() {
        val observer = constructorIo.getSearchResults(
            term = "item1",
            groupsSortBy = "value",
            groupsSortOrder = "descending"
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
        assertEquals(searchResponse?.response?.groups?.get(0)?.displayName, "All")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithHiddenFieldsAgainstRealResponse() {
        val hiddenFields = listOf("testField", "hiddenField2")
        val observer = constructorIo.getBrowseResults(
            filterName = "group_id",
            filterValue = "All",
            hiddenFields = hiddenFields
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.results!!.isNotEmpty())
        assertTrue(browseResponse?.response?.results?.first()?.data?.metadata?.get("testField") !== null)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithHiddenFacetsAgainstRealResponse() {
        val hiddenFacets = listOf("Brand")
        val observer = constructorIo.getBrowseResults(
            filterName = "group_id",
            filterValue = "All",
            hiddenFacets = hiddenFacets,
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        val brandFacet = browseResponse?.response?.facets?.find { facet -> facet.name.contains("Brand")}
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.facets!!.isNotEmpty())
        assertTrue(brandFacet !== null)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithCollectionAgainstRealResponse() {
        val observer = constructorIo.getBrowseResults("collection_id", "test").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.facets!!.isNotEmpty())
        assertEquals(browseResponse?.response?.collection?.id, "test")
        assertEquals(browseResponse?.response?.collection?.displayName, "test")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsAgainstRealResponseUsingRequestBuilder() {
        val request = AutocompleteRequest.Builder("itemkkk").build()
        val observer = constructorIo.getAutocompleteResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val autocompleteResponse = observer.values()[0].get()
        assertTrue(autocompleteResponse?.resultId !== null)
        assertTrue(autocompleteResponse?.sections!!.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsAgainstRealResponseWithVariationsMapArrayUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "array",
            values = mapOf(
                "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
            ),
            groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
        )
        val request =
            AutocompleteRequest.Builder("Jacket").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getAutocompleteResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val autocompleteResponse = observer.values()[0].get()
        val returnedVariationsMap = autocompleteResponse?.sections!!["Products"]?.get(0)?.variationsMap as List<*>
        assertTrue(autocompleteResponse?.resultId !== null)
        assertTrue(autocompleteResponse?.sections!!.isNotEmpty())
        assertTrue(returnedVariationsMap.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsAgainstRealResponseWithVariationsMapObjectUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "object",
            values = mapOf(
                    "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
            ),
            groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
        )
        val request =
            AutocompleteRequest.Builder("jacket").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getAutocompleteResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val autocompleteResponse = observer.values()[0].get()
        val returnedVariationsMap = autocompleteResponse?.sections!!["Products"]?.get(0)?.variationsMap as Map<*, *>
        assertTrue(autocompleteResponse?.resultId !== null)
        assertTrue(autocompleteResponse?.sections!!.isNotEmpty())
        assertTrue(returnedVariationsMap.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultAgainstRealResponseUsingRequestBuilder() {
        val request = SearchRequest.Builder("item1").build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.facets!!.isNotEmpty())
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
        assertTrue(searchResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultAgainstRealResponseWithVariationsMapArrayUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "array",
            values = mapOf(
                    "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
            ),
            groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
        )
        val request = SearchRequest.Builder("jacket").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        val returnedVariationsMap = searchResponse?.response?.results!![0].variationsMap as? List<*>
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)
        assertTrue(returnedVariationsMap!!.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultAgainstRealResponseWithVariationsMapObjectUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "object",
            values = mapOf(
                    "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
            ),
            groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
        )
        val request = SearchRequest.Builder("jacket").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        val returnedVariationsMap = searchResponse?.response?.results!![0].variationsMap as? Map<*, *>
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)
        assertTrue(returnedVariationsMap!!.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultAgainstRealResponseWithResultSources() {
        val request = SearchRequest.Builder("item1").build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.facets!!.isNotEmpty())
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
        assertTrue(searchResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)
        assertTrue(searchResponse?.response?.resultSources!!.embeddingsMatch!!.count!! >= 0)
        assertTrue(searchResponse?.response?.resultSources!!.tokenMatch!!.count!! >= 0)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultAgainstRealResponseUsingRequestBuilder() {
        val request = BrowseRequest.Builder("Brand", "XYZ").build()
        val observer = constructorIo.getBrowseResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.results!!.isNotEmpty())
        assertTrue(browseResponse?.response?.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response?.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(browseResponse?.response?.resultCount!! > 0)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultAgainstRealResponseWithVariationsArrayMapUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "array",
            values = mapOf(
                    "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
            ),
            groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
        )
        val request =
            BrowseRequest.Builder("Brand", "XYZ").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getBrowseResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        val returnedVariationsMap = browseResponse?.response?.results!![0].variationsMap as? List<*>
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.results!!.isNotEmpty())
        assertTrue(browseResponse?.response?.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response?.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(browseResponse?.response?.resultCount!! > 0)
        assertTrue(returnedVariationsMap!!.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultAgainstRealResponseWithVariationsObjectMapUsingRequestBuilder() {
        val variationsMap = VariationsMap(
            dtype = "object",
            values = mapOf(
                    "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
            ),
            groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
        )
        val request =
            BrowseRequest.Builder("Brand", "XYZ").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getBrowseResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        val returnedVariationsMap = browseResponse?.response?.results!![0].variationsMap as? Map<*, *>
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.results!!.isNotEmpty())
        assertTrue(browseResponse?.response?.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response?.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(browseResponse?.response?.resultCount!! > 0)
        assertTrue(returnedVariationsMap!!.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultAgainstRealResponseWithResultSources() {
        val request = BrowseRequest.Builder("Brand", "XYZ").build()
        val observer = constructorIo.getBrowseResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.results!!.isNotEmpty())
        assertTrue(browseResponse?.response?.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response?.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(browseResponse?.response?.resultCount!! > 0)
        assertTrue(browseResponse?.response?.resultSources!!.embeddingsMatch!!.count!! >= 0)
        assertTrue(browseResponse?.response?.resultSources!!.tokenMatch!!.count!! >= 0)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsAgainstRealResponseUsingRequestBuilder() {
        val filters = mapOf("Brand" to listOf("XYZ"))
        val request = RecommendationsRequest.Builder("item_page_1")
            .setFilters(filters)
            .setItemIds(listOf("power_drill", "drill"))
            .build()
        val observer = constructorIo.getRecommendationResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val recommendationResponse = observer.values()[0].get()
        assertTrue(recommendationResponse?.resultId !== null)
        assertTrue(recommendationResponse?.response?.pod !== null)
        assertTrue(recommendationResponse?.response?.results !== null)
        assertTrue(recommendationResponse?.response?.resultCount!! >= 0)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsAgainstRealResponseWithVariationsMapArrayUsingRequestBuilder() {
        val filters = mapOf("Brand" to listOf("XYZ"))
        val variationsMap = VariationsMap(
                dtype = "array",
                values = mapOf(
                        "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
                ),
                groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
        )
        val request = RecommendationsRequest.Builder("filtered_items")
                .setFilters(filters)
                .setVariationsMap(variationsMap)
                .build()
        val observer = constructorIo.getRecommendationResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val recommendationResponse = observer.values()[0].get()
        val returnedVariationsMap = recommendationResponse?.response?.results!![0].variationsMap as? List<*>
        assertTrue(recommendationResponse?.resultId !== null)
        assertTrue(recommendationResponse?.response?.pod !== null)
        assertTrue(recommendationResponse?.response?.results !== null)
        assertTrue(recommendationResponse?.response?.resultCount!! >= 0)
        assertTrue(returnedVariationsMap!!.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsAgainstRealResponseWithVariationsMapObjectUsingRequestBuilder() {
        val filters = mapOf("Brand" to listOf("XYZ"))
        val variationsMap = VariationsMap(
                dtype = "object",
                values = mapOf(
                        "size" to mapOf("aggregation" to "first", "field" to "data.facets.size"),
                ),
                groupBy = listOf(mapOf("name" to "variation", "field" to "data.variation_id")),
        )
        val request = RecommendationsRequest.Builder("filtered_items")
                .setFilters(filters)
                .setVariationsMap(variationsMap)
                .build()
        val observer = constructorIo.getRecommendationResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val recommendationResponse = observer.values()[0].get()
        val returnedVariationsMap = recommendationResponse?.response?.results!![0].variationsMap as? Map<*, *>
        assertTrue(recommendationResponse?.resultId !== null)
        assertTrue(recommendationResponse?.response?.pod !== null)
        assertTrue(recommendationResponse?.response?.results !== null)
        assertTrue(recommendationResponse?.response?.resultCount!! >= 0)
        assertTrue(returnedVariationsMap!!.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithGroupsSortValueAscendingAgainstRealResponse() {
        val observer = constructorIo.getBrowseResults(
            filterName = "Brand",
            filterValue = "XYZ",
            groupsSortBy = "value",
            groupsSortOrder = "ascending",
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.groups!!.isNotEmpty())
        assertEquals(browseResponse?.response?.groups?.get(0)?.displayName, "All")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithGroupsSortValueDescendingAgainstRealResponse() {
        val observer = constructorIo.getBrowseResults(
            filterName = "Brand",
            filterValue = "XYZ",
            groupsSortBy = "value",
            groupsSortOrder = "descending",
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.groups!!.isNotEmpty())
        assertEquals(browseResponse?.response?.groups?.get(0)?.displayName, "All")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsWithLabelsAgainstRealResponse() {
        val request = AutocompleteRequest.Builder("item").build()
        val observer = constructorIo.getAutocompleteResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val autocompleteResponse = observer.values()[0].get()
        assertTrue(autocompleteResponse?.resultId !== null)
        assertTrue(autocompleteResponse?.sections!!.isNotEmpty())
        assertEquals(autocompleteResponse?.sections?.get("Products")?.first()?.labels!!["is_sponsored"], true)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithLabelsAgainstRealResponse() {
        val request = SearchRequest.Builder("item").build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.facets!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)
        assertEquals(searchResponse?.response?.results?.first()?.labels!!["is_sponsored"], true)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithLabelsAgainstRealResponse() {
        val request = BrowseRequest.Builder("Brand", "XYZ").build()
        val observer = constructorIo.getBrowseResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.results!!.isNotEmpty())
        assertTrue(browseResponse?.response?.facets!!.isNotEmpty())
        assertTrue(browseResponse?.response?.groups!!.isNotEmpty())
        assertTrue(browseResponse?.response?.resultCount!! > 0)
        assertEquals(browseResponse?.response?.results?.first()?.labels!!["is_sponsored"], true)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultAgainstRealResponseWithRefinedContent() {
        val request = SearchRequest.Builder("item").build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)
        assertTrue(searchResponse?.response?.refinedContent?.first()?.data!!.isNotEmpty())
        assertEquals(searchResponse?.response?.refinedContent?.first()?.data?.get("body"), "Content 1 Body")
        assertEquals(searchResponse?.response?.refinedContent?.first()?.data?.get("header"), "Content 1 Header")
        assertEquals(searchResponse?.response?.refinedContent?.first()?.data?.get("assetUrl"), "https://constructor.io/wp-content/uploads/2022/09/groceryshop-2022-r2.png")
        assertEquals(searchResponse?.response?.refinedContent?.first()?.data?.get("altText"), "Content 1 desktop alt text")
        assertEquals(searchResponse?.response?.refinedContent?.first()?.data?.get("ctaLink"), "https://constructor.io/wp-content/uploads/2022/09/groceryshop-2022-r2.png")
        assertEquals(searchResponse?.response?.refinedContent?.first()?.data?.get("ctaText"), "Content 1 CTA Button")
        assertEquals(searchResponse?.response?.refinedContent?.first()?.data?.get("tag-1"), "tag-1-value")
        assertEquals(searchResponse?.response?.refinedContent?.first()?.data?.get("tag-2"), "tag-2-value")
        assertTrue(searchResponse?.response?.refinedContent?.first()?.data?.get("arbitraryDataObject") !== null)

        Thread.sleep(timeBetweenTests)
    }
}
