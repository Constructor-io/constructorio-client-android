package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class SearchRequestTest {
    private val query = "rose"
    private val filtersToApply = mapOf(
        "Brand" to listOf("XYZ", "123"),
        "group_id" to listOf("123"),
    )
    private val page = 2
    private val perPage = 30
    private val sortBy = "relevance"
    private val sortOrder = "ascending"
    private val section = "Search Suggestions"
    private val hiddenFields = listOf("hidden_field_1", "hidden_field_2")
    private val hiddenFacets = listOf("hidden_facet_1", "hidden_facet_2")
    private val groupsSortBy = "value"
    private val groupsSortOrder = "ascending"

    @Test
    fun searchRequestWithTermUsingBuilder() {
        val request = SearchRequest.Builder(query).build()
        assertEquals(request.term, query)
    }

    @Test
    fun searchRequestWithFiltersUsingBuilder() {
        val request = SearchRequest.Builder(query)
            .setFilters(filtersToApply)
            .build()
        assertEquals(request.filters, filtersToApply)
    }

    @Test
    fun searchRequestWithPageParamsUsingBuilder() {
        val request = SearchRequest.Builder(query)
            .setPage(page)
            .setPerPage(perPage)
            .build()
        assertEquals(request.page, page)
        assertEquals(request.perPage, perPage)
    }

    @Test
    fun searchRequestWithSortOptionUsingBuilder() {
        val request = SearchRequest.Builder(query)
            .setSortBy(sortBy)
            .setSortOrder(sortOrder)
            .build()
        assertEquals(request.sortBy, sortBy)
        assertEquals(request.sortOrder, sortOrder)
    }

    @Test
    fun searchRequestWithSectionUsingBuilder() {
        val request = SearchRequest.Builder(query)
                .setSection(section)
                .build()
        assertEquals(request.section, section)
    }

    @Test
    fun searchRequestWithHiddenFieldsUsingBuilder() {
        val request = SearchRequest.Builder(query)
                .setHiddenFields(hiddenFields)
                .build()
        assertEquals(request.hiddenFields, hiddenFields)
    }

    @Test
    fun searchRequestWithHiddenFacetsUsingBuilder() {
        val request = SearchRequest.Builder(query)
                .setHiddenFacets(hiddenFacets)
                .build()
        assertEquals(request.hiddenFacets, hiddenFacets)
    }

    @Test
    fun searchRequestWithGroupsSortOptionUsingBuilder() {
        val request = SearchRequest.Builder(query)
                .setGroupsSortBy(groupsSortBy)
                .setGroupsSortOrder(groupsSortOrder)
                .build()
        assertEquals(request.groupsSortBy, groupsSortBy)
        assertEquals(request.groupsSortOrder, groupsSortOrder)
    }

    @Test
    fun searchRequestWithParamsUsingDSL() {
        val request = SearchRequest.build(query) {
            filters = this@SearchRequestTest.filtersToApply
            page = this@SearchRequestTest.page
            perPage = this@SearchRequestTest.perPage
            sortBy = this@SearchRequestTest.sortBy
            sortOrder = this@SearchRequestTest.sortOrder
            section = this@SearchRequestTest.section
            hiddenFields = this@SearchRequestTest.hiddenFields
            hiddenFacets = this@SearchRequestTest.hiddenFacets
            groupsSortBy = this@SearchRequestTest.groupsSortBy
            groupsSortOrder = this@SearchRequestTest.groupsSortOrder
        }

        assertEquals(request.term, query)
        assertEquals(request.filters, filtersToApply)
        assertEquals(request.page, page)
        assertEquals(request.perPage, perPage)
        assertEquals(request.sortBy, sortBy)
        assertEquals(request.sortOrder, sortOrder)
        assertEquals(request.section, section)
        assertEquals(request.hiddenFields, hiddenFields)
        assertEquals(request.hiddenFacets, hiddenFacets)
        assertEquals(request.groupsSortBy, groupsSortBy)
        assertEquals(request.groupsSortOrder, groupsSortOrder)
    }
}