package io.constructor.mapper

import io.constructor.util.TestDataLoader
import org.junit.Test
import kotlin.test.assertEquals

class MapperTest {

    @Test
    fun mapDataToViewModel() {
        val response = TestDataLoader.loadResponse()
        val viewModels = response?.let { Mapper.toSuggestionsViewModel(it) }
        if (viewModels != null) {
            assertEquals(5, viewModels.size)
        }
    }
}