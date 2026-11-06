package io.constructor.data.builder

import io.constructor.data.model.common.ResultGroup
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchSubmitTrackingDataTest {
    private val searchTerm = "titanic"
    private val originalQuery = "titan"
    private val sectionName = "Content"
    private val resultGroup = ResultGroup("Movies", "movies")
    private val analyticsTags = mapOf("relatedSearchTerm" to "true")

    @Test
    fun searchSubmitTrackingDataUsingBuilderWithSection() {
        val request = SearchSubmitTrackingData.build(searchTerm, originalQuery) {
            setSection(sectionName)
        }
        assertEquals(sectionName, request.section)
    }

    @Test
    fun searchSubmitTrackingDataUsingBuilderWithoutSection() {
        val request = SearchSubmitTrackingData.Builder(searchTerm, originalQuery).build()
        assertNull(request.section)
    }

    @Test
    fun searchSubmitTrackingDataUsingBuilderWithAllParams() {
        val request = SearchSubmitTrackingData.Builder(searchTerm, originalQuery)
            .setResultGroup(resultGroup)
            .setAnalyticsTags(analyticsTags)
            .setSection(sectionName)
            .build()
        assertEquals(searchTerm, request.searchTerm)
        assertEquals(originalQuery, request.originalQuery)
        assertEquals(resultGroup, request.resultGroup)
        assertEquals(analyticsTags, request.analyticsTags)
        assertEquals(sectionName, request.section)
    }

    @Test
    fun searchSubmitTrackingDataUsingConstructorHasNullSection() {
        val request = SearchSubmitTrackingData(searchTerm = searchTerm, originalQuery = originalQuery)
        assertNull(request.section)
    }

    @Test
    fun searchSubmitTrackingDataPublicConstructorsUnchanged() {
        val defaultMarker = "kotlin.jvm.internal.DefaultConstructorMarker"
        val publicConstructors = SearchSubmitTrackingData::class.java.constructors
            .map { constructor -> constructor.parameterTypes.toList() }
        assertEquals(
            listOf(listOf(String::class.java, String::class.java, ResultGroup::class.java, Map::class.java)),
            publicConstructors.filter { params -> params.none { it.name == defaultMarker } }
        )
        // Synthetic default-args constructor keeps the released shape (4 params + int mask + marker)
        assertTrue(publicConstructors.any { params -> params.size == 6 && params.last().name == defaultMarker })
    }
}
