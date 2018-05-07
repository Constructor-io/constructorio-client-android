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

    @Test
    fun saveAndRetrieveToken() {
        preferencesHelper.saveToken("testToken")
        assertEquals("testToken", preferencesHelper.getToken())
    }

    @Test
    fun getSessionId() {
        val currentTime = System.currentTimeMillis()
        assertEquals(1, preferencesHelper.getSessionId())
        verify(exactly = 1) { preferencesHelper.resetSession() }
        assertEquals(1, preferencesHelper.getSessionId())
        every { preferencesHelper.getLastSessionAccess() } returns currentTime - TimeUnit.MINUTES.toMillis(31)
        assertEquals(2, preferencesHelper.getSessionId())
        assertEquals(3, preferencesHelper.getSessionId())

    }

    @Test
    fun saveAndRetrieveId() {
        preferencesHelper.saveId("testId")
        assertEquals("testId", preferencesHelper.getId())
    }

    @Test
    fun clearAllValues() {
        preferencesHelper.clear()
        assertEquals("", preferencesHelper.getId())
    }

}