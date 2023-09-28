package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class AutocompleteRequestTest {
    private val query = "rose"
    private val filters = mapOf(
        "Brand" to listOf("XYZ", "123"),
        "group_id" to listOf("123"),
    )
    private val sectionFilters = mapOf(
        "Search Suggestions" to mapOf("Brand" to listOf("XYZ", "123")),
        "Products" to mapOf("availability" to listOf("ABC", "987")),
    )
    private val numResultsPerSection = mapOf(
        "Products" to 5,
        "Search Suggestions" to 8,
    )
    private val hiddenFields = listOf("hidden_field_1", "hidden_field_2")


    @Test
    fun autocompleteRequestWithTermUsingBuilder() {
        val request = AutocompleteRequest.Builder(query).build()
        assertEquals(request.term, query)
    }

    @Test
    fun autocompleteRequestWithFiltersUsingBuilder() {
        val request = AutocompleteRequest.Builder(query)
            .setFilters(filters)
            .build()
        assertEquals(request.filters, filters)
    }

    @Test
    fun autocompleteRequestWithSectionFiltersUsingBuilder() {
        val request = AutocompleteRequest.Builder(query)
            .setFilters(filters)
            .setSectionFilters(sectionFilters)
            .build()
        assertEquals(request.filters, filters)
        assertEquals(request.sectionFilters, sectionFilters)
    }

    @Test
    fun autocompleteRequestWithNumResultsPerSectionUsingBuilder() {
        val request = AutocompleteRequest.Builder(query)
            .setNumResultsPerSection(numResultsPerSection)
            .build()
        assertEquals(request.numResultsPerSection, numResultsPerSection)
    }

    @Test
    fun autocompleteRequestWithHiddenFieldsUsingBuilder() {
        val request = AutocompleteRequest.Builder(query)
            .setHiddenFields(hiddenFields)
            .build()
        assertEquals(request.hiddenFields, hiddenFields)
    }

    @Test
    fun autocompleteRequestUsingDSL() {
        val request = AutocompleteRequest.build(query) {
            filters = this@AutocompleteRequestTest.filters
            numResultsPerSection = this@AutocompleteRequestTest.numResultsPerSection
            hiddenFields = this@AutocompleteRequestTest.hiddenFields
        }
        assertEquals(request.term, query)
        assertEquals(request.filters, filters)
        assertEquals(request.hiddenFields, hiddenFields)
    }
}