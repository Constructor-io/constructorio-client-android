package io.constructor.core

import android.content.Context
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.autocomplete.AutocompleteFacetsConfig
import io.constructor.data.model.autocomplete.AutocompleteResultsConfig
import io.constructor.data.model.browse.BrowseFacetsConfig
import io.constructor.data.model.recommendations.RecommendationsRequestsConfig
import io.constructor.data.model.search.SearchFacetsConfig
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConstructorIoIntegrationTest {

    @Rule
    @JvmField val overrideSchedulersRule = RxSchedulersOverrideRule()

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

        every { configMemoryHolder.userId } returns "player-three"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("key_K2hlXt5aVSwoI1Uw")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder, ctx)

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
    fun getAutocompleteResultsWithResultsPerSection() {
        val observer = constructorIo.getAutocompleteResults("pork", AutocompleteResultsConfig(mapOf("Products" to 3, "Search Suggestions" to 2))).test()
        observer.assertComplete().assertValue {
            it.get()?.sections?.get("Products")?.size == 3
            it.get()?.sections?.get("Search Suggestions")?.size == 2
            it.get()?.resultId!!.isNotEmpty()
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getAutocompleteResultsWithFiltersAgainstRealResponse() {
        val facetsConfig = AutocompleteFacetsConfig(mapOf("group_id" to listOf("544")))
        val observer = constructorIo.getAutocompleteResults("pork", null, facetsConfig).test()
        observer.assertComplete().assertValue {
            it.get()?.sections?.get("Products")!!.isNotEmpty()
            it.get()?.sections?.get("Search Suggestions")!!.isNotEmpty()
            it.get()?.resultId!!.isNotEmpty()
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
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getSearchResultsWithFiltersAgainstRealResponse() {
        val facetsConfig = SearchFacetsConfig(mapOf("Brand" to listOf("Smokehouse", "Applegate")))
        val observer = constructorIo.getSearchResults("pork", null, facetsConfig).test()
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
        val facetsConfig = BrowseFacetsConfig(mapOf("Brand" to listOf("Meal Mart", "Premio")))
        val observer = constructorIo.getBrowseResults("group_id", "544", null, facetsConfig).test()
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
    fun trackAutocompleteSelectAgainstRealResponse() {
        val observer = constructorIo.trackAutocompleteSelectInternal("pork", "pork", "Search Suggestions").test()
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
        val observer = constructorIo.trackSearchResultClickInternal("Boneless Pork Shoulder Roast", "prrst_shldr_bls", "pork").test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackConversionAgainstRealResponse() {
        val observer = constructorIo.trackConversionInternal("Boneless Pork Shoulder Roast", "prrst_shldr_bls", 1.99).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackPurchaseAgainstRealResponse() {
        val observer = constructorIo.trackPurchaseInternal(arrayOf("prrst_shldr_bls", "prrst_crwn"), 9.98, "45273", "Products").test()
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
        val observer = constructorIo.trackBrowseResultClickInternal("group_ids", "544", "prrst_shldr_bls", 5).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun getRecommendationResultsAgainstRealResponse() {
        val recommendationConfig = RecommendationsRequestsConfig("pdp5")
        val observer = constructorIo.getRecommendationResults(recommendationConfig).test()
        observer.assertComplete().assertValue {
            it.get()?.resultId !== null
            it.get()?.response?.pod !== null
            it.get()?.response?.results!!.isNotEmpty()
            it.get()?.response?.resultCount!! > 0
        }
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackRecommendationResultClickAgainstRealResponse() {
        val observer = constructorIo.trackRecommendationResultClickInternal("pdp5", "User Featured", "prrst_shldr_bls").test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackRecommendationResultsViewAgainstRealResponse() {
        val observer = constructorIo.trackRecommendationResultsViewInternal("pdp5", 4).test()
        observer.assertComplete()
        Thread.sleep(timeBetweenTests)
    }
}
