package io.constructor.data.builder

import io.constructor.data.model.common.TrackingItem
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchResultsLoadedDataTest {
    private val term = "tooth"
    private val resultCount = 789
    private val items = listOf(TrackingItem("1234567-AB", "RED", "campaign-1", "owner-1"))
    private val sectionName = "Search Suggestions"
    private val url = "https://constructor.io/search?q=tooth"
    private val analyticsTags = mapOf("test" to "test1")
    private val resultId = "179b8a0e-3799-4a31-be87-127b06871de2"
    private val resultPage = 3
    private val resultOffset = 20
    private val sortOrder = "ascending"
    private val sortBy = "price"
    private val selectedFilters = mapOf("brand" to listOf("XYZ"), "color" to listOf("black"))

    @Test
    fun searchResultsLoadedDataWithRequiredParams() {
        val request = SearchResultsLoadedData.build(term, resultCount)
        assertEquals(term, request.term)
        assertEquals(resultCount, request.resultCount)
        assertNull(request.items)
        assertNull(request.sectionName)
        assertEquals("Not Available", request.url)
        assertNull(request.analyticsTags)
        assertNull(request.resultId)
        assertNull(request.resultPage)
        assertNull(request.resultOffset)
        assertNull(request.sortOrder)
        assertNull(request.sortBy)
        assertNull(request.selectedFilters)
    }

    @Test
    fun searchResultsLoadedDataUsingDsl() {
        val request = SearchResultsLoadedData.build(term, resultCount) {
            this.items = this@SearchResultsLoadedDataTest.items
            this.sectionName = this@SearchResultsLoadedDataTest.sectionName
            this.url = this@SearchResultsLoadedDataTest.url
            this.analyticsTags = this@SearchResultsLoadedDataTest.analyticsTags
            this.resultId = this@SearchResultsLoadedDataTest.resultId
            this.resultPage = this@SearchResultsLoadedDataTest.resultPage
            this.sortOrder = this@SearchResultsLoadedDataTest.sortOrder
            this.sortBy = this@SearchResultsLoadedDataTest.sortBy
            this.selectedFilters = this@SearchResultsLoadedDataTest.selectedFilters
        }
        assertEquals(term, request.term)
        assertEquals(resultCount, request.resultCount)
        assertEquals(items, request.items)
        assertEquals(sectionName, request.sectionName)
        assertEquals(url, request.url)
        assertEquals(analyticsTags, request.analyticsTags)
        assertEquals(resultId, request.resultId)
        assertEquals(resultPage, request.resultPage)
        assertNull(request.resultOffset)
        assertEquals(sortOrder, request.sortOrder)
        assertEquals(sortBy, request.sortBy)
        assertEquals(selectedFilters, request.selectedFilters)
    }

    @Test
    fun searchResultsLoadedDataUsingBuilder() {
        val request = SearchResultsLoadedData.Builder(term, resultCount)
            .setItems(items)
            .setSectionName(sectionName)
            .setUrl(url)
            .setAnalyticsTags(analyticsTags)
            .setResultId(resultId)
            .setResultOffset(resultOffset)
            .setSortOrder(sortOrder)
            .setSortBy(sortBy)
            .setSelectedFilters(selectedFilters)
            .build()
        assertEquals(term, request.term)
        assertEquals(resultCount, request.resultCount)
        assertEquals(items, request.items)
        assertEquals(sectionName, request.sectionName)
        assertEquals(url, request.url)
        assertEquals(analyticsTags, request.analyticsTags)
        assertEquals(resultId, request.resultId)
        assertNull(request.resultPage)
        assertEquals(resultOffset, request.resultOffset)
        assertEquals(sortOrder, request.sortOrder)
        assertEquals(sortBy, request.sortBy)
        assertEquals(selectedFilters, request.selectedFilters)
    }

    @Test
    fun searchResultsLoadedDataUsingConstructor() {
        val request = SearchResultsLoadedData(
            term = term,
            resultCount = resultCount,
            items = items,
            sectionName = sectionName,
            url = url,
            analyticsTags = analyticsTags,
            resultId = resultId,
            resultPage = resultPage,
            sortOrder = sortOrder,
            sortBy = sortBy,
            selectedFilters = selectedFilters,
        )
        assertEquals(term, request.term)
        assertEquals(resultCount, request.resultCount)
        assertEquals(items, request.items)
        assertEquals(sectionName, request.sectionName)
        assertEquals(url, request.url)
        assertEquals(analyticsTags, request.analyticsTags)
        assertEquals(resultId, request.resultId)
        assertEquals(resultPage, request.resultPage)
        assertNull(request.resultOffset)
        assertEquals(sortOrder, request.sortOrder)
        assertEquals(sortBy, request.sortBy)
        assertEquals(selectedFilters, request.selectedFilters)
    }
}
