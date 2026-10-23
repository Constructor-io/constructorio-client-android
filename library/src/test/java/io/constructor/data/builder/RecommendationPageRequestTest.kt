package io.constructor.data.builder

import org.junit.Test
import kotlin.test.assertEquals

class RecommendationPageRequestTest {
    private val pageId = "pdp_b2c"

    @Test
    fun recommendationPageRequestWithSharedValuesAndOverridesUsingBuilder() {
        val podOverrides = mapOf("similar_items" to RecommendationPagePodOverride(numResults = 12))
        val request = RecommendationPageRequest.Builder(pageId)
            .setItemIds(listOf("product-123"))
            .setVariationId("product-123-red")
            .setNumResults(10)
            .setFilters(mapOf("in_stock" to listOf("true")))
            .setFilterMatchTypes(mapOf("color" to "any"))
            .setPodOverrides(podOverrides)
            .build()

        assertEquals(listOf("product-123"), request.itemIds)
        assertEquals("product-123-red", request.variationId)
        assertEquals(10, request.numResults)
        assertEquals(mapOf("in_stock" to listOf("true")), request.filters)
        assertEquals(mapOf("color" to "any"), request.filterMatchTypes)
        assertEquals(podOverrides, request.podOverrides)
    }

    @Test
    fun recommendationPageRequestUsingDSL() {
        val request = RecommendationPageRequest.build(pageId) {
            numResults = 10
            podOverrides = mapOf("complete_the_look" to RecommendationPagePodOverride(numResults = 8))
        }

        assertEquals(pageId, request.pageId)
        assertEquals(10, request.numResults)
        assertEquals(8, request.podOverrides?.get("complete_the_look")?.numResults)
    }
}
