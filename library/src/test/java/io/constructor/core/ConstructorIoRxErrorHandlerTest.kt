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
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

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
    fun handlesSocketTimeoutException() {
        // Should not throw - SocketTimeoutException is a subclass of IOException
        RxJavaPlugins.onError(UndeliverableException(SocketTimeoutException("connect timed out")))
    }

    @Test
    fun forwardsUnexpectedExceptionToUncaughtExceptionHandler() {
        val original = Thread.currentThread().uncaughtExceptionHandler
        try {
            var caughtThrowable: Throwable? = null
            Thread.currentThread().uncaughtExceptionHandler =
                Thread.UncaughtExceptionHandler { _, throwable -> caughtThrowable = throwable }

            val error = IllegalStateException("unexpected error")
            RxJavaPlugins.onError(UndeliverableException(error))

            assertSame(error, caughtThrowable)
        } finally {
            Thread.currentThread().uncaughtExceptionHandler = original
        }
    }

    @Test
    fun forwardsNullCauseExceptionToUncaughtExceptionHandler() {
        val original = Thread.currentThread().uncaughtExceptionHandler
        try {
            var caughtThrowable: Throwable? = null
            Thread.currentThread().uncaughtExceptionHandler =
                Thread.UncaughtExceptionHandler { _, throwable -> caughtThrowable = throwable }

            val error = UndeliverableException(null)
            RxJavaPlugins.onError(error)

            // When cause is null, the UndeliverableException itself is forwarded
            assertSame(error, caughtThrowable)
        } finally {
            Thread.currentThread().uncaughtExceptionHandler = original
        }
    }
}
