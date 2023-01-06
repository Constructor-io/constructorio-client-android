package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class BrowseItemsRequestTest {
    private val ids = listOf("dai_pid_2003438", "dai_pid_2003597")
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
    fun browseItemsRequestUsingBuilder() {
        val request = BrowseItemsRequest.Builder(ids).build()
        assertEquals(request.ids, ids)
    }

    @Test
    fun browseItemsRequestWithFiltersUsingBuilder() {
        val request = BrowseItemsRequest.Builder(ids)
                .setFilters(filtersToApply)
                .build()
        assertEquals(request.filters, filtersToApply)
    }

    @Test
    fun browseItemsRequestWithPageParamsUsingBuilder() {
        val request = BrowseItemsRequest.Builder(ids)
                .setPage(page)
                .setPerPage(perPage)
                .build()
        assertEquals(request.page, page)
        assertEquals(request.perPage, perPage)
    }

    @Test
    fun browseItemsRequestWithSortOptionUsingBuilder() {
        val request = BrowseItemsRequest.Builder(ids)
                .setSortBy(sortBy)
                .setSortOrder(sortOrder)
                .build()
        assertEquals(request.sortBy, sortBy)
        assertEquals(request.sortOrder, sortOrder)
    }

    @Test
    fun browseItemsRequestWithSectionUsingBuilder() {
        val request = BrowseItemsRequest.Builder(ids)
                .setSection(section)
                .build()
        assertEquals(request.section, section)
    }

    @Test
    fun browseItemsRequestWithHiddenFieldsUsingBuilder() {
        val request = BrowseItemsRequest.Builder(ids)
                .setHiddenFields(hiddenFields)
                .build()
        assertEquals(request.hiddenFields, hiddenFields)
    }

    @Test
    fun browseItemsRequestWithHiddenFacetsUsingBuilder() {
        val request = BrowseItemsRequest.Builder(ids)
                .setHiddenFacets(hiddenFacets)
                .build()
        assertEquals(request.hiddenFacets, hiddenFacets)
    }

    @Test
    fun browseItemsRequestWithGroupsSortOptionUsingBuilder() {
        val request = BrowseItemsRequest.Builder(ids)
                .setGroupsSortBy(groupsSortBy)
                .setGroupsSortOrder(groupsSortOrder)
                .build()
        assertEquals(request.groupsSortBy, groupsSortBy)
        assertEquals(request.groupsSortOrder, groupsSortOrder)
    }

    @Test
    fun browseItemsRequestWithParamsUsingDSL() {
        val request = BrowseItemsRequest.build(ids) {
            filters = this@BrowseItemsRequestTest.filtersToApply
            page = this@BrowseItemsRequestTest.page
            perPage = this@BrowseItemsRequestTest.perPage
            sortBy = this@BrowseItemsRequestTest.sortBy
            sortOrder = this@BrowseItemsRequestTest.sortOrder
            section = this@BrowseItemsRequestTest.section
            hiddenFields = this@BrowseItemsRequestTest.hiddenFields
            hiddenFacets = this@BrowseItemsRequestTest.hiddenFacets
            groupsSortBy = this@BrowseItemsRequestTest.groupsSortBy
            groupsSortOrder = this@BrowseItemsRequestTest.groupsSortOrder
        }

        assertEquals(request.ids, ids)
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