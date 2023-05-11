package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class BrowseFacetOptionsRequestTest {
    private val facetName = "Brand"
    private val showHiddenFacets = true

    @Test
    fun browseFacetOptionsRequestUsingBuilder() {
        val request = BrowseFacetOptionsRequest.Builder(facetName)
                .build()
        assertEquals(request.facetName, facetName)
    }

    @Test
    fun browseFacetOptionsRequestWithShowHiddenFacetsUsingBuilder() {
        val request = BrowseFacetOptionsRequest.Builder(facetName)
                .setShowHiddenFacets(true)
                .build()
        assertEquals(request.showHiddenFacets, showHiddenFacets)
    }

    @Test
    fun browseFacetOptionsRequestWithParamsUsingDSL() {
        val request = BrowseFacetOptionsRequest.build(facetName) {
            showHiddenFacets = this@BrowseFacetOptionsRequestTest.showHiddenFacets
        }

        assertEquals(request.showHiddenFacets, showHiddenFacets)
    }
}
