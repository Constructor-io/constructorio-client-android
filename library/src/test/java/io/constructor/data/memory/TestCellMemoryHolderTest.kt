package io.constructor.data.memory

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TestCellMemoryHolderTest {

    private lateinit var testCellMemoryHolder: TestCellMemoryHolder

    @Before
    fun setUp() {
        testCellMemoryHolder = TestCellMemoryHolder()
    }

    @Test
    fun verifyTestCellsWrittenAndEncoded() {
        testCellMemoryHolder.testCellParams = listOf("1" to "2", "3" to "4")
        val params = testCellMemoryHolder.testCellParams
        assert(params[0]!!.first == "ef-1" && params[0]!!.second == "2")
        assert(params[1]!!.first == "ef-3" && params[1]!!.second == "4")
    }
}