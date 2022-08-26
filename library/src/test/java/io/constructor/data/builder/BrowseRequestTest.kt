package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class BrowseRequestTest {
    private val filterName = "group_id"
    private val filterValue = "123"
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
    fun browseRequestUsingBuilder() {
        val request = BrowseRequest.Builder(filterName, filterValue).build()
        assertEquals(request.filterName, filterName)
        assertEquals(request.filterValue, filterValue)
    }

    @Test
    fun browseRequestWithFiltersUsingBuilder() {
        val request = BrowseRequest.Builder(filterName, filterValue)
                .setFilters(filtersToApply)
                .build()
        assertEquals(request.filters, filtersToApply)
    }

    @Test
    fun browseRequestWithPageParamsUsingBuilder() {
        val request = BrowseRequest.Builder(filterName, filterValue)
                .setPage(page)
                .setPerPage(perPage)
                .build()
        assertEquals(request.page, page)
        assertEquals(request.perPage, perPage)
    }

    @Test
    fun browseRequestWithSortOptionUsingBuilder() {
        val request = BrowseRequest.Builder(filterName, filterValue)
                .setSortBy(sortBy)
                .setSortOrder(sortOrder)
                .build()
        assertEquals(request.sortBy, sortBy)
        assertEquals(request.sortOrder, sortOrder)
    }

    @Test
    fun browseRequestWithSectionUsingBuilder() {
        val request = BrowseRequest.Builder(filterName, filterValue)
                .setSection(section)
                .build()
        assertEquals(request.section, section)
    }

    @Test
    fun browseRequestWithHiddenFieldsUsingBuilder() {
        val request = BrowseRequest.Builder(filterName, filterValue)
                .setHiddenFields(hiddenFields)
                .build()
        assertEquals(request.hiddenFields, hiddenFields)
    }

    @Test
    fun browseRequestWithHiddenFacetsUsingBuilder() {
        val request = BrowseRequest.Builder(filterName, filterValue)
                .setHiddenFacets(hiddenFacets)
                .build()
        assertEquals(request.hiddenFacets, hiddenFacets)
    }

    @Test
    fun browseRequestWithGroupsSortOptionUsingBuilder() {
        val request = BrowseRequest.Builder(filterName, filterValue)
                .setGroupsSortBy(groupsSortBy)
                .setGroupsSortOrder(groupsSortOrder)
                .build()
        assertEquals(request.groupsSortBy, groupsSortBy)
        assertEquals(request.groupsSortOrder, groupsSortOrder)
    }

    @Test
    fun browseRequestWithParamsUsingDSL() {
        val request = BrowseRequest.build(filterName, filterValue) {
            filters = this@BrowseRequestTest.filtersToApply
            page = this@BrowseRequestTest.page
            perPage = this@BrowseRequestTest.perPage
            sortBy = this@BrowseRequestTest.sortBy
            sortOrder = this@BrowseRequestTest.sortOrder
            section = this@BrowseRequestTest.section
            hiddenFields = this@BrowseRequestTest.hiddenFields
            hiddenFacets = this@BrowseRequestTest.hiddenFacets
            groupsSortBy = this@BrowseRequestTest.groupsSortBy
            groupsSortOrder = this@BrowseRequestTest.groupsSortOrder
        }

        assertEquals(request.filterName, filterName)
        assertEquals(request.filterValue, filterValue)
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