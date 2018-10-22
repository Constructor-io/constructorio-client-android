package io.constructor.data.memory

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfigMemoryHolderTest {

    private lateinit var configMemoryHolder: ConfigMemoryHolder

    @Before
    fun setUp() {
        configMemoryHolder = ConfigMemoryHolder()
    }

    @Test
    fun verifyTestCellsWrittenAndEncoded() {
        configMemoryHolder.testCellParams = listOf("1" to "2", "3" to "4")
        val params = configMemoryHolder.testCellParams
        assert(params[0]!!.first == "ef-1" && params[0]!!.second == "2")
        assert(params[1]!!.first == "ef-3" && params[1]!!.second == "4")
    }
}