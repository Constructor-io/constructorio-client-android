package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class AutocompleteRequestTest {

    private val query = "rose"

    @Test
    fun autocompleteRequestWithTermUsingBuilder() {
        val request = AutocompleteRequest.Builder()
            .setTerm(query)
            .build()
        assertEquals(request.term, query)
    }

    @Test
    fun autocompleteRequestWithFiltersUsingBuilder() {
        val filters = mapOf(
            "Brand" to listOf("XYZ", "123"),
            "group_id" to listOf("123"),
        )
        val request = AutocompleteRequest.Builder()
            .setTerm(query)
            .setFilters(filters)
            .build()
        assertEquals(request.filters, filters)
    }

    @Test
    fun autocompleteRequestWithNumResultsPerSectionUsingBuilder() {
        val numResultsPerSection = mapOf(
            "Products" to 5,
            "Search Suggestions" to 8,
        )
        val request = AutocompleteRequest.Builder()
            .setTerm(query)
            .setNumResultsPerSection(numResultsPerSection)
            .build()
        assertEquals(request.numResultsPerSection, numResultsPerSection)
    }

    @Test
    fun autocompleteRequestWithHiddenFieldsUsingBuilder() {
        val hiddenFields = listOf("hidden_field_1", "hidden_field_2")
        val request = AutocompleteRequest.Builder()
            .setTerm(query)
            .setHiddenFields(hiddenFields)
            .build()
        assertEquals(request.hiddenFields, hiddenFields)
    }

    @Test
    fun autocompleteRequestWithTermUsingDSL() {
        val request = AutocompleteRequest.build {
            term = query
        }
        assertEquals(request.term, query)
    }

    @Test
    fun autocompleteRequestWithFiltersUsingDSL() {
        val filtersToApply = mapOf(
                "Brand" to listOf("XYZ", "123"),
                "group_id" to listOf("123"),
        )
        val request = AutocompleteRequest.build {
            term = query
            filters = filtersToApply
        }
        assertEquals(request.term, query)
        assertEquals(request.filters, filtersToApply)
    }

    @Test
    fun autocompleteRequestWithNumResultsPerSectionUsingDSL() {
        val numResultsPerSectionToApply = mapOf(
                "Products" to 5,
                "Search Suggestions" to 8,
        )
        val request = AutocompleteRequest.build {
            term = query
            numResultsPerSection = numResultsPerSectionToApply
        }
        assertEquals(request.numResultsPerSection, numResultsPerSectionToApply)
    }

    @Test
    fun autocompleteRequestWithHiddenFieldsUsingDSL() {
        val hiddenFieldsToApply = listOf("hidden_field_1", "hidden_field_2")
        val request = AutocompleteRequest.build {
            term = query
            hiddenFields = hiddenFieldsToApply
        }
        assertEquals(request.hiddenFields, hiddenFieldsToApply)
    }
}