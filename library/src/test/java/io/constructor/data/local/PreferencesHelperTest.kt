package io.constructor.data.local

import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class PreferencesHelperTest {

    private var preferencesHelper = spyk(PreferencesHelper(RuntimeEnvironment.application.applicationContext, "test.prefs"))

    private var dummyAction: (String) -> Unit = {
        assertEquals("2", it)
    }

    @Test
    fun saveAndRetrieveToken() {
        preferencesHelper.token = "testToken"
        assertEquals("testToken", preferencesHelper.token)
    }

    @Test
    fun saveAndRetrieveDefaultItemSection() {
        preferencesHelper.defaultItemSection = "Products"
        assertEquals("Products", preferencesHelper.defaultItemSection)
    }

    @Test
    fun getSessionId() {
        val currentTime = System.currentTimeMillis()
        assertEquals(1, preferencesHelper.getSessionId())
        verify(exactly = 1) { preferencesHelper.resetSession() }
        assertEquals(1, preferencesHelper.getSessionId())
        every { preferencesHelper.lastSessionAccess } returns currentTime - TimeUnit.MINUTES.toMillis(31)
        assertEquals(2, preferencesHelper.getSessionId())
        assertEquals(3, preferencesHelper.getSessionId())
    }

    @Test
    fun verifySessionIdIncrementTriggerAction() {
        val currentTime = System.currentTimeMillis()
        assertEquals(1, preferencesHelper.getSessionId())
        verify(exactly = 1) { preferencesHelper.resetSession() }
        every { preferencesHelper.lastSessionAccess} returns currentTime - TimeUnit.MINUTES.toMillis(31)
        assertEquals(2, preferencesHelper.getSessionId(dummyAction))
    }

    @Test
    fun verifyTestCellsWrittenAndEncoded() {
        preferencesHelper.testCellParams = listOf("1" to "2", "3" to "4", null)
        val params = preferencesHelper.testCellParams
        assert(params[0]!!.first == "ef-1" && params[0]!!.second == "2")
        assert(params[1]!!.first == "ef-3" && params[1]!!.second == "4")
    }

    @Test
    fun saveAndRetrieveId() {
        preferencesHelper.id = "testId"
        assertEquals("testId", preferencesHelper.id)
    }

    @Test
    fun clearAllValues() {
        preferencesHelper.clear()
        assertEquals("", preferencesHelper.id)
    }

}