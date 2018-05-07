package io.constructor.mapper

import io.constructor.util.TestDataLoader
import org.junit.Test
import kotlin.test.assertEquals

class MapperTest {

    @Test
    fun mapDataToViewModel() {
        val suggestions = TestDataLoader.loadResponse()!!.sections.suggestions
        val viewModels = Mapper.toSuggestionsViewModel(suggestions)
        assertEquals(10, viewModels.size)
    }
}