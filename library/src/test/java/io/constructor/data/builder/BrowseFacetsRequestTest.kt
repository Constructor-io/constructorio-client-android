package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class BrowseFacetsRequestTest {
    private val page = 2
    private val offset = 10
    private val numResultsPerPage = 30
    private val showHiddenFacets = true

    @Test
    fun browseFacetsRequestWithPageUsingBuilder() {
        val request = BrowseFacetsRequest.Builder()
                .setPage(page)
                .build()
        assertEquals(request.page, page)
    }

    @Test
    fun browseFacetsRequestWithOffsetUsingBuilder() {
        val request = BrowseFacetsRequest.Builder()
                .setOffset(offset)
                .build()
        assertEquals(request.offset, offset)
    }

    @Test
    fun browseFacetsRequestWithNumResultsPerPageUsingBuilder() {
        val request = BrowseFacetsRequest.Builder()
                .setNumResultsPerPage(numResultsPerPage)
                .build()
        assertEquals(request.numResultsPerPage, numResultsPerPage)
    }

    @Test
    fun browseFacetsRequestWithShowHiddenFacetsUsingBuilder() {
        val request = BrowseFacetsRequest.Builder()
                .setShowHiddenFacets(showHiddenFacets)
                .build()
        assertEquals(request.showHiddenFacets, showHiddenFacets)
    }

    @Test
    fun browseFacetsRequestWithParamsUsingDSL() {
        val request = BrowseFacetsRequest.build() {
            page = this@BrowseFacetsRequestTest.page
            showHiddenFacets = this@BrowseFacetsRequestTest.showHiddenFacets
            numResultsPerPage = this@BrowseFacetsRequestTest.numResultsPerPage
            offset = this@BrowseFacetsRequestTest.offset
        }

        assertEquals(request.page, page)
        assertEquals(request.showHiddenFacets, showHiddenFacets)
        assertEquals(request.offset, offset)
        assertEquals(request.numResultsPerPage, numResultsPerPage)
    }
}
