package io.constructor.data.local

import io.mockk.every
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PreferencesHelperTest {

    private val partyLikeIts1999 = 946684800000
    private val preferencesHelper = spyk(PreferencesHelper(RuntimeEnvironment.application.applicationContext, "test.prefs"))

    @Test
    fun saveAndRetrieveId() {
        preferencesHelper.id = "4a31451c-9a6b-417c-81d6-88d4669cfee4"
        assertEquals("4a31451c-9a6b-417c-81d6-88d4669cfee4", preferencesHelper.id)
    }

    @Test
    fun saveAndRetrieveApiKey() {
        preferencesHelper.apiKey = "test-key"
        assertEquals("test-key", preferencesHelper.apiKey)
    }

    @Test
    fun saveAndRetrieveDefaultItemSection() {
        preferencesHelper.defaultItemSection = "Sofas"
        assertEquals("Sofas", preferencesHelper.defaultItemSection)
    }

    @Test
    fun saveAndRetrieveGroupsShownForFirstTerm() {
        preferencesHelper.groupsShownForFirstTerm = 99
        assertEquals(99, preferencesHelper.groupsShownForFirstTerm)
    }

    @Test
    fun saveAndRetrieveLastSessionAccess() {
        preferencesHelper.lastSessionAccess = partyLikeIts1999
        assertEquals(partyLikeIts1999, preferencesHelper.lastSessionAccess)
    }

    @Test
    fun getSessionIdFirstTime() {
        assertEquals(1, preferencesHelper.getSessionId())
    }

    @Test
    fun getSessionIdAfter30Minutes() {
        preferencesHelper.resetSession(null)
        val currentTime = System.currentTimeMillis()
        every { preferencesHelper.lastSessionAccess } returns currentTime - TimeUnit.MINUTES.toMillis(31)
        assertEquals(2, preferencesHelper.getSessionId())
        assertEquals(3, preferencesHelper.getSessionId())
    }

    @Test
    fun getSessionIdWithForceIncrement() {
        preferencesHelper.resetSession(null)
        preferencesHelper.getSessionId(null, true);
        assertEquals(2, preferencesHelper.getSessionId())
    }

    @Test
    fun getSessionIdWithIncrementAction() {
        preferencesHelper.resetSession(null)
        val currentTime = System.currentTimeMillis()
        val dummyAction: (String) -> Unit = {
            assertEquals("2", it)
        }
        every { preferencesHelper.lastSessionAccess} returns currentTime - TimeUnit.MINUTES.toMillis(31)
        assertEquals(2, preferencesHelper.getSessionId(dummyAction))
    }

    @Test
    fun clear() {
        preferencesHelper.clear()
        assertEquals("", preferencesHelper.id)
        assertEquals("", preferencesHelper.apiKey)
        assertEquals("", preferencesHelper.defaultItemSection)
        assertEquals(2, preferencesHelper.groupsShownForFirstTerm)
    }

}