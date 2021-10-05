package io.constructor.data.memory

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
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

    @Test
    fun verifySegmentsWrittenAndEncoded() {
        configMemoryHolder.segments = listOf("mobile", "COUNTRY_US")
        val params = configMemoryHolder.segments
        assert(params[0] == "mobile")
        assert(params[1] == "COUNTRY_US")
    }
}