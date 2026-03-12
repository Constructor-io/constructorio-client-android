package io.constructor.core

import io.reactivex.exceptions.UndeliverableException
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class ConstructorIoRxErrorHandlerTest {

    private var constructorIo = ConstructorIo

    @Before
    fun setup() {
        RxJavaPlugins.reset()
        constructorIo.setupRxJavaErrorHandler()
    }

    @After
    fun teardown() {
        RxJavaPlugins.reset()
    }

    @Test
    fun doesNotOverwriteExistingErrorHandler() {
        RxJavaPlugins.reset()
        val existingHandler = Consumer<Throwable> { }
        RxJavaPlugins.setErrorHandler(existingHandler)

        constructorIo.setupRxJavaErrorHandler()

        assertSame(existingHandler, RxJavaPlugins.getErrorHandler())
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
