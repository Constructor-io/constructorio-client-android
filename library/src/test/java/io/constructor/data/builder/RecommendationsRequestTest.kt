package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class RecommendationsRequestTest {
    private val podId = "product_detail_page"
    private val filtersToApply = mapOf(
        "Brand" to listOf("XYZ", "123"),
        "group_id" to listOf("123"),
    )
    private val itemIds = listOf("123")
    private val term = "some zero results query"
    private val numResults = 5
    private val section = "Recommendations Suggestions"

    @Test
    fun recommendationsRequestUsingBuilder() {
        val request = RecommendationsRequest.Builder(podId).build()
        assertEquals(request.podId, podId)
    }

    @Test
    fun recommendationsRequestWithFiltersUsingBuilder() {
        val request = RecommendationsRequest.Builder(podId)
            .setFilters(filtersToApply)
            .build()
        assertEquals(request.filters, filtersToApply)
    }

    @Test
    fun recommendationsRequestWithNumResultsUsingBuilder() {
        val request = RecommendationsRequest.Builder(podId)
            .setNumResults(numResults)
            .build()
        assertEquals(request.numResults, numResults)
    }

    @Test
    fun recommendationsRequestWithItemIdsUsingBuilder() {
        val request = RecommendationsRequest.Builder(podId)
            .setItemIds(itemIds)
            .build()
        assertEquals(request.itemIds, itemIds)
    }

    @Test
    fun recommendationsRequestWithTermUsingBuilder() {
        val request = RecommendationsRequest.Builder(podId)
            .setTerm(term)
            .build()
        assertEquals(request.term, term)
    }

    @Test
    fun recommendationsRequestWithSectionUsingBuilder() {
        val request = RecommendationsRequest.Builder(podId)
            .setSection(section)
            .build()
        assertEquals(request.section, section)
    }

    @Test
    fun recommendationsRequestWithParamsUsingDSL() {
        val request = RecommendationsRequest.build(podId) {
            filters = this@RecommendationsRequestTest.filtersToApply
            itemIds = this@RecommendationsRequestTest.itemIds
            term = this@RecommendationsRequestTest.term
            numResults = this@RecommendationsRequestTest.numResults
            section = this@RecommendationsRequestTest.section
        }

        assertEquals(request.podId, podId)
        assertEquals(request.filters, filtersToApply)
        assertEquals(request.itemIds, itemIds)
        assertEquals(request.term, term)
        assertEquals(request.numResults, numResults)
        assertEquals(request.section, section)
    }
}