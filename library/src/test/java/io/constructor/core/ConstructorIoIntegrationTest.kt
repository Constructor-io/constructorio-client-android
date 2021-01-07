package io.constructor.core

import android.content.Context
import android.content.SharedPreferences
import io.constructor.core.ConstructorIoConfig
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.data.model.common.ResultGroup
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstructorIoTestIntegration {

    @Rule
    @JvmField val overrideSchedulersRule = RxSchedulersOverrideRule()

    private var constructorIo = ConstructorIo
    private val ctx = mockk<Context>()
    private val sharedPreferences = mockk<SharedPreferences>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()

    @Before
    fun setup() {
        every { ctx.applicationContext } returns ctx
//        every { ctx.getSharedPreferences } returns sharedPreferences

//        every { preferencesHelper.apiKey } returns "copper-key"
//        every { preferencesHelper.id } returns "wacko-the-guid"
//        every { preferencesHelper.scheme } returns "http"
//        every { preferencesHelper.serviceUrl } returns "ac.cnstrc.com"
//        every { preferencesHelper.defaultItemSection } returns "Products"
//        every { preferencesHelper.getSessionId(any(), any()) } returns 67
//
//        every { configMemoryHolder.autocompleteResultCount } returns null
//        every { configMemoryHolder.userId } returns "player-three"
//        every { configMemoryHolder.testCellParams } returns emptyList()
//        every { configMemoryHolder.segments } returns emptyList()
//
//        val config = ConstructorIoConfig("dummyKey")
//        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder, ctx)
//
//        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun trackBrowseAgainstRealResponse() {
        val config = ConstructorIoConfig("key_K2hlXt5aVSwoI1Uw")
        val spy = spyk(ConstructorIo)
        spy.init(ctx, config)
        spy.trackBrowseResultClick("group_ids", "544", "prrst_shldr_bls", 5)
    }

}
