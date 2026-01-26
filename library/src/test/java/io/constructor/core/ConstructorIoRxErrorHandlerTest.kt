package io.constructor.core

import io.reactivex.plugins.RxJavaPlugins
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.test.assertEquals

/**
 * Tests for the RxJava global error handler that prevents crashes from
 * undeliverable exceptions (e.g., SocketTimeoutException on OkHttp background threads)
 */
class ConstructorIoRxErrorHandlerTest {

    private var errorCount = 0

    @Before
    fun setup() {
        // Reset state before each test
        RxJavaPlugins.reset()
        errorCount = 0

        // Use the actual error handler from ConstructorIo
        val internalHandler = ConstructorIo.createRxErrorHandler()
        RxJavaPlugins.setErrorHandler { throwable ->
            errorCount++
            internalHandler(throwable)
        }
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
    }

    @Test
    fun `SocketTimeoutException should be handled without crashing`() {
        // Simulate what happens when OkHttp throws SocketTimeoutException on a background thread
        val exception = SocketTimeoutException("timeout")

        // This would crash the app without the error handler
        RxJavaPlugins.onError(exception)

        // Verify the error was handled - if we get here, we didn't crash
        assertEquals(1, errorCount, "Error should have been handled")
    }

    @Test
    fun `IOException should be handled without crashing`() {
        val exception = IOException("Connection reset")

        RxJavaPlugins.onError(exception)

        assertEquals(1, errorCount, "Error should have been handled")
    }

    @Test
    fun `InterruptedException should be handled without crashing`() {
        val exception = InterruptedException("Thread interrupted")

        RxJavaPlugins.onError(exception)

        assertEquals(1, errorCount, "Error should have been handled")
    }

    @Test
    fun `other exceptions should be handled without crashing`() {
        val exception = IllegalStateException("Some other error")

        RxJavaPlugins.onError(exception)

        assertEquals(1, errorCount, "Error should have been handled")
    }

    @Test
    fun `multiple errors should all be handled without crashing`() {
        RxJavaPlugins.onError(SocketTimeoutException("timeout1"))
        RxJavaPlugins.onError(IOException("connection reset"))
        RxJavaPlugins.onError(SocketTimeoutException("timeout2"))

        assertEquals(3, errorCount, "All errors should be handled without crashing")
    }
}
