package io.constructor.core

import android.content.Context
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.mockk.every
import io.mockk.mockk
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ConstructorIoRxErrorHandlerTest {

    @Rule
    @JvmField
    val overrideSchedulersRule = RxSchedulersOverrideRule()

    private lateinit var mockServer: MockWebServer
    private var constructorIo = ConstructorIo
    private val ctx = mockk<Context>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()

    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()

        every { ctx.applicationContext } returns ctx

        every { preferencesHelper.apiKey } returns "golden-key"
        every { preferencesHelper.id } returns "guido-the-guid"
        every { preferencesHelper.serviceUrl } returns mockServer.hostName
        every { preferencesHelper.port } returns mockServer.port
        every { preferencesHelper.scheme } returns "http"
        every { preferencesHelper.defaultItemSection } returns "Products"
        every { preferencesHelper.getSessionId(any(), any()) } returns 79

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.userId } returns "player-one"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("dummyKey")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
        constructorIo.setupRxJavaErrorHandler()
    }

    @After
    fun teardown() {
        mockServer.shutdown()
        RxJavaPlugins.reset()
    }

    @Test
    fun handlesUndeliverableIOException() {
        // Should not throw - IOException is handled gracefully
        RxJavaPlugins.onError(UndeliverableException(IOException("network timeout")))
    }

    @Test
    fun handlesUndeliverableInterruptedException() {
        // Should not throw - InterruptedException is handled gracefully
        RxJavaPlugins.onError(UndeliverableException(InterruptedException("thread interrupted")))
    }

    @Test
    fun handlesRawIOException() {
        // Should not throw - IOException without UndeliverableException wrapper
        RxJavaPlugins.onError(IOException("connection reset"))
    }

    @Test
    fun handlesUnexpectedException() {
        // Should not throw - unexpected exceptions are logged but don't crash
        RxJavaPlugins.onError(UndeliverableException(IllegalStateException("unexpected error")))
    }

    @Test
    fun handlesUndeliverableExceptionWithNullCause() {
        // Should not throw - UndeliverableException with no cause
        RxJavaPlugins.onError(UndeliverableException(null))
    }
}
