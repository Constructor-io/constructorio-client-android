package io.constructor.core

import io.constructor.data.interceptor.RequestInterceptor
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for the suppressNetworkExceptions feature that prevents crashes from
 * network exceptions (e.g., SocketTimeoutException) on OkHttp's background threads.
 */
class ConstructorIoNetworkExceptionSuppressionTest {

    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()
    private lateinit var interceptor: RequestInterceptor

    @Before
    fun setup() {
        every { preferencesHelper.apiKey } returns "copper-key"
        every { preferencesHelper.id } returns "wacko-the-guid"
        every { preferencesHelper.port } returns 443
        every { preferencesHelper.getSessionId() } returns 67

        every { configMemoryHolder.userId } returns "player-three"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        interceptor = RequestInterceptor(preferencesHelper, configMemoryHolder)
    }

    private fun createMockChain(url: String, throwException: Exception? = null): Interceptor.Chain {
        val request = Request.Builder().url(url).build()
        return mockk {
            every { request() } returns request
            every { proceed(any()) } answers {
                if (throwException != null) {
                    throw throwException
                }
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("".toResponseBody(null))
                    .build()
            }
        }
    }

    @Test
    fun `when suppression disabled, SocketTimeoutException should propagate`() {
        every { configMemoryHolder.suppressNetworkExceptions } returns false

        val chain = createMockChain(
            "http://test.com/v2/behavioral_action/conversion",
            SocketTimeoutException("timeout")
        )

        assertFailsWith<SocketTimeoutException> {
            interceptor.intercept(chain)
        }
    }

    @Test
    fun `when suppression enabled, behavioral v2 endpoint SocketTimeoutException should return 599`() {
        every { configMemoryHolder.suppressNetworkExceptions } returns true

        val chain = createMockChain(
            "http://test.com/v2/behavioral_action",
            SocketTimeoutException("timeout")
        )

        val response = interceptor.intercept(chain)

        assertEquals(599, response.code)
        assert(response.message.contains("Network Error"))
    }

    @Test
    fun `when suppression enabled, behavioral v1 endpoint IOException should return 599`() {
        every { configMemoryHolder.suppressNetworkExceptions } returns true

        val chain = createMockChain(
            "http://test.com/behavior",
            IOException("Connection reset")
        )

        val response = interceptor.intercept(chain)

        assertEquals(599, response.code)
    }

    @Test
    fun `when suppression enabled, autocomplete search endpoint should return 599`() {
        every { configMemoryHolder.suppressNetworkExceptions } returns true

        val chain = createMockChain(
            "http://test.com/autocomplete/test/search",
            SocketTimeoutException("timeout")
        )

        val response = interceptor.intercept(chain)

        assertEquals(599, response.code)
    }

    @Test
    fun `when suppression enabled, non-behavioral endpoint should return 599`() {
        every { configMemoryHolder.suppressNetworkExceptions } returns true

        val chain = createMockChain(
            "http://test.com/search/products",
            SocketTimeoutException("timeout")
        )

        val response = interceptor.intercept(chain)

        assertEquals(599, response.code)
        assert(response.message.contains("Network Error"))
    }

    @Test
    fun `when suppression enabled, autocomplete endpoint should return 599`() {
        every { configMemoryHolder.suppressNetworkExceptions } returns true

        val chain = createMockChain(
            "http://test.com/autocomplete/test",
            IOException("Connection refused")
        )

        val response = interceptor.intercept(chain)

        assertEquals(599, response.code)
    }

    @Test
    fun `when suppression enabled, non-network exception should still propagate`() {
        every { configMemoryHolder.suppressNetworkExceptions } returns true

        val chain = createMockChain(
            "http://test.com/v2/behavioral_action/conversion",
            IllegalStateException("unexpected error")
        )

        assertFailsWith<IllegalStateException> {
            interceptor.intercept(chain)
        }
    }

    @Test
    fun `successful request should return normally regardless of suppression setting`() {
        every { configMemoryHolder.suppressNetworkExceptions } returns true

        val chain = createMockChain("http://test.com/v2/behavioral_action/conversion")

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
    }
}
