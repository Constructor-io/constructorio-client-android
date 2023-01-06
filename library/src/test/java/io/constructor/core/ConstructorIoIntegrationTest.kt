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
        val facet = hashMapOf("storeLocation" to listOf("CA"))
        val observer =
            constructorIo.getAutocompleteResults("pork", facet.map { it.key to it.value }).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsCRTAgainstRealResponse() {
        runBlocking {
            val autocompleteResults = constructorIo.getAutocompleteResultsCRT("pork")
            assertTrue(autocompleteResults.sections!!.isNotEmpty())
            assertTrue(autocompleteResults.resultId!!.isNotEmpty())
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsCRTWithFiltersAgainstRealResponse() {
        runBlocking {
            val facet = hashMapOf("storeLocation" to listOf("CA"))
            val autocompleteResults = constructorIo.getAutocompleteResultsCRT("pork", facet.map { it.key to it.value })
            assertTrue(autocompleteResults.sections!!.isNotEmpty())
            assertTrue(autocompleteResults.resultId!!.isNotEmpty())
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
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId!!.isNotEmpty())
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.facets!!.isNotEmpty())
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
        assertTrue(searchResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)
        assertEquals(searchResponse?.response?.results?.first()?.isSlotted, true)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithFiltersAgainstRealResponse() {
        val facet = hashMapOf(
            "group_id" to listOf("544"),
            "Claims" to listOf("Raised Without Antibiotics")
        )
        val observer = constructorIo.getSearchResults("pork", facet.map { it.key to it.value }).test()
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
            val searchResults = constructorIo.getSearchResultsCRT("pork")
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
            val facet = hashMapOf("Claims" to listOf("Organic"))
            val searchResults = constructorIo.getSearchResultsCRT("pork", facet.map { it.key to it.value })
            assertTrue(searchResults.resultId !== null)
            assertTrue(searchResults.response!!.facets!!.isNotEmpty())
            assertTrue(searchResults.response!!.groups!!.isNotEmpty())
            assertTrue(searchResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(searchResults.response!!.resultCount!! > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsAgainstRealResponse() {
        val observer = constructorIo.getBrowseResults("group_id", "744").test()
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
            val browseResults = constructorIo.getBrowseResultsCRT("group_id", "744")
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
            val facet = hashMapOf("Claims" to listOf("Organic"))
            val browseResults = constructorIo.getBrowseResultsCRT("group_id", "744", facet.map { it.key to it.value })
            assertTrue(browseResults.resultId !== null)
            assertTrue(browseResults.response!!.facets!!.isNotEmpty())
            assertTrue(browseResults.response!!.groups!!.isNotEmpty())
            assertTrue(browseResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(browseResults.response!!.resultCount > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseItemsResultsCRTAgainstRealResponse() {
        runBlocking {
            val browseResults = constructorIo.getBrowseItemsResultsCRT(listOf("dai_pid_2003438"))
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
        val request = BrowseItemsRequest.Builder(listOf("dai_pid_2003438"))
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
        val request = BrowseItemsRequest.Builder(listOf("bstp_fajita_local")).setVariationsMap(variationsMap).build()
        val observer = constructorIo.getBrowseItemsResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        val returnedVariationsMap = searchResponse?.response?.results!![0].variationsMap as? Map<*, *>
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.facets!!.isNotEmpty())
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
        assertTrue(searchResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)
        assertTrue(returnedVariationsMap!!.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseItemsResultsCRTWithFiltersAgainstRealResponse() {
        runBlocking {
            val facet = hashMapOf("Claims" to listOf("Low Fat"))
            val browseResults = constructorIo.getBrowseItemsResultsCRT(listOf("dai_pid_2003438"), facet.map { it.key to it.value })
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
            val request = BrowseItemsRequest.Builder(listOf("dai_pid_2003438")).build()
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
            val facet = hashMapOf("Claims" to listOf("Low Fat"))
            val browseResults = constructorIo.getBrowseItemsResultsCRT(listOf("dai_pid_2003438"), facet.map { it.key to it.value })
            assertTrue(browseResults.resultId !== null)
            assertTrue(browseResults.response!!.facets!!.isNotEmpty())
            assertTrue(browseResults.response!!.groups!!.isNotEmpty())
            assertTrue(browseResults.response!!.filterSortOptions!!.isNotEmpty())
            assertTrue(browseResults.response!!.resultCount > 0)
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
    fun getRecommendationResultsAgainstRealResponse() {
        val facet = hashMapOf("Claims" to listOf("Organic"))
        val observer = constructorIo.getRecommendationResults("pdp3", facet.map { it.key to it.value }).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val recommendationResponse = observer.values()[0].get()
        assertTrue(recommendationResponse?.resultId !== null)
        assertTrue(recommendationResponse?.response?.pod !== null)
        assertTrue(recommendationResponse?.response?.results !== null)
        assertTrue(recommendationResponse?.response?.resultCount!! >= 0)
        assertEquals(recommendationResponse?.response?.results?.first()?.isSlotted, false)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsCRTAgainstRealResponse() {
        runBlocking {
            val facet = hashMapOf("Claims" to listOf("Organic"))
            val recommendationResults = constructorIo.getRecommendationResultsCRT("pdp3", facet.map { it.key to it.value })
            assertTrue(recommendationResults.resultId !== null)
            assertTrue(recommendationResults.response?.pod !== null)
            assertTrue(recommendationResults.response?.results!!.isNotEmpty())
            assertTrue(recommendationResults.response?.resultCount!! > 0)
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackRecommendationResultClickAgainstRealResponse() {
        val observer = constructorIo.trackRecommendationResultClickInternal(
            "pdp3",
            "filtered_items",
            "prrst_shldr_bls"
        ).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackRecommendationResultsViewAgainstRealResponse() {
        val observer = constructorIo.trackRecommendationResultsViewInternal("pdp3", 4).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsWithHiddenFieldsAgainstRealResponse() {
        val hiddenFields = listOf("hiddenField1", "hiddenField2")
        val observer = constructorIo.getAutocompleteResults("pork", null, null, hiddenFields).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val autocompleteResponse = observer.values()[0].get()
        assertTrue(autocompleteResponse?.resultId !== null)
        assertTrue(autocompleteResponse?.sections!!.isNotEmpty())
        assertTrue(autocompleteResponse?.sections?.get("Products")?.first()?.data?.metadata?.get("hiddenField1") !== null)
        assertTrue(autocompleteResponse?.sections?.get("Products")?.first()?.data?.metadata?.get("hiddenField2") !== null)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithHiddenFieldsAgainstRealResponse() {
        val hiddenFields = listOf("hiddenField1", "hiddenField2")
        val observer = constructorIo.getSearchResults(
            term =  "pork",
            hiddenFields = hiddenFields
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.results?.first()?.data?.metadata?.get("hiddenField1") !== null)
        assertTrue(searchResponse?.response?.results?.first()?.data?.metadata?.get("hiddenField2") !== null)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithHiddenFacetsAgainstRealResponse() {
        val hiddenFacets = listOf("Brand")
        val observer = constructorIo.getSearchResults(
            term = "pork",
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
            term = "pork",
            groupsSortBy = "value",
            groupsSortOrder = "ascending"
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
        assertEquals(searchResponse?.response?.groups?.get(0)?.displayName, "Dairy")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithGroupsSortValueDescendingAgainstRealResponse() {
        val observer = constructorIo.getSearchResults(
            term = "pork",
            groupsSortBy = "value",
            groupsSortOrder = "descending"
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
        assertEquals(searchResponse?.response?.groups?.get(0)?.displayName, "Meat & Poultry")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithHiddenFieldsAgainstRealResponse() {
        val hiddenFields = listOf("hiddenField1", "hiddenField2")
        val observer = constructorIo.getBrowseResults(
            filterName = "group_id",
            filterValue = "431",
            hiddenFields = hiddenFields
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.results!!.isNotEmpty())
        assertTrue(browseResponse?.response?.results?.first()?.data?.metadata?.get("hiddenField1") !== null)
        assertTrue(browseResponse?.response?.results?.first()?.data?.metadata?.get("hiddenField2") !== null)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithHiddenFacetsAgainstRealResponse() {
        val hiddenFacets = listOf("Brand")
        val observer = constructorIo.getBrowseResults(
            filterName = "group_id",
            filterValue = "431",
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
        val observer = constructorIo.getBrowseResults("collection_id", "test-collection").test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.facets!!.isNotEmpty())
        assertEquals(browseResponse?.response?.collection?.id, "test-collection")
        assertEquals(browseResponse?.response?.collection?.displayName, "test collection")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsAgainstRealResponseUsingRequestBuilder() {
        val request = AutocompleteRequest.Builder("pork").build()
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
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request =
            AutocompleteRequest.Builder("angus beef").setVariationsMap(variationsMap).build()
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
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request =
            AutocompleteRequest.Builder("angus beef").setVariationsMap(variationsMap).build()
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
        val request = SearchRequest.Builder("pork").build()
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
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request = SearchRequest.Builder("angus beef").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        val returnedVariationsMap = searchResponse?.response?.results!![0].variationsMap as? List<*>
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.facets!!.isNotEmpty())
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
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
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request = SearchRequest.Builder("angus beef").setVariationsMap(variationsMap).build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        val returnedVariationsMap = searchResponse?.response?.results!![0].variationsMap as? Map<*, *>
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.facets!!.isNotEmpty())
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
        assertTrue(searchResponse?.response?.filterSortOptions!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)
        assertTrue(returnedVariationsMap!!.isNotEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultAgainstRealResponseWithResultSources() {
        val request = SearchRequest.Builder("angus beef").build()
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
        val request = BrowseRequest.Builder("group_id", "431").build()
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
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request =
            BrowseRequest.Builder("group_id", "544").setVariationsMap(variationsMap).build()
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
                "Price" to mapOf("aggregation" to "min", "field" to "data.facets.price"),
                "Country" to mapOf("aggregation" to "all", "field" to "data.facets.country")
            )
        )
        val request =
            BrowseRequest.Builder("group_id", "431").setVariationsMap(variationsMap).build()
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
        val request = BrowseRequest.Builder("group_id", "431").build()
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
        val filters = mapOf("group_id" to listOf("544"))
        val request = RecommendationsRequest.Builder("pdp3")
            .setFilters(filters)
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
    fun getBrowseResultsWithGroupsSortValueAscendingAgainstRealResponse() {
        val observer = constructorIo.getBrowseResults(
            filterName = "group_id",
            filterValue = "431",
            groupsSortBy = "value",
            groupsSortOrder = "ascending",
        ).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.groups!!.isNotEmpty())
        assertEquals(browseResponse?.response?.groups?.get(0)?.displayName, "Grocery")
        assertEquals(browseResponse?.response?.groups?.get(0)?.children?.get(0)?.displayName, "Baby")

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
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.resultId !== null)
        assertTrue(browseResponse?.response?.groups!!.isNotEmpty())
        assertEquals(browseResponse?.response?.groups?.get(0)?.displayName, "Grocery")
        assertEquals(browseResponse?.response?.groups?.get(0)?.children?.get(0)?.displayName, "Pet")

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsWithLabelsAgainstRealResponse() {
        val request = AutocompleteRequest.Builder("pork").build()
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
        val request = SearchRequest.Builder("pork").build()
        val observer = constructorIo.getSearchResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val searchResponse = observer.values()[0].get()
        assertTrue(searchResponse?.resultId !== null)
        assertTrue(searchResponse?.response?.results!!.isNotEmpty())
        assertTrue(searchResponse?.response?.facets!!.isNotEmpty())
        assertTrue(searchResponse?.response?.groups!!.isNotEmpty())
        assertTrue(searchResponse?.response?.resultCount!! > 0)
        assertEquals(searchResponse?.response?.results?.first()?.labels!!["is_sponsored"], true)

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getBrowseResultsWithLabelsAgainstRealResponse() {
        val request = BrowseRequest.Builder("group_id", "544").build()
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
    fun getRecommendationResultsWithLabelsAgainstRealResponse() {
        val filters = mapOf("group_id" to listOf("544"))
        val request = RecommendationsRequest.Builder("pdp3").setFilters(filters).build()
        val observer = constructorIo.getRecommendationResults(request).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val recommendationResponse = observer.values()[0].get()
        assertTrue(recommendationResponse?.resultId !== null)
        assertTrue(recommendationResponse?.response?.results!!.isNotEmpty())
        assertTrue(recommendationResponse?.response?.resultCount!! > 0)
        assertTrue(recommendationResponse?.response?.results?.first()?.labels!!.isNullOrEmpty())

        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultAgainstRealResponseWithRefinedContent() {
        val request = SearchRequest.Builder("superbowl").build()
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
