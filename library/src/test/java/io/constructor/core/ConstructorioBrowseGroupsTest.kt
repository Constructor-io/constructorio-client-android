package io.constructor.core

import android.content.Context
import io.constructor.data.builder.BrowseGroupsRequest
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.TestDataLoader
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstructorioBrowseGroupsTest {

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

        every { preferencesHelper.apiKey } returns "silver-key"
        every { preferencesHelper.id } returns "guapo-the-guid"
        every { preferencesHelper.serviceUrl } returns mockServer.hostName
        every { preferencesHelper.port } returns mockServer.port
        every { preferencesHelper.scheme } returns "http"
        every { preferencesHelper.defaultItemSection } returns "Products"
        every { preferencesHelper.getSessionId(any(), any()) } returns 92

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.userId } returns "player-two"
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig("dummyKey")
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)
    }

    @Test
    fun getBrowseGroupsResults() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_groups.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseGroups().test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertEquals(browseResponse?.response?.groups!!.size, 1)
        assertEquals(browseResponse?.response?.groups!![0].displayName, "All")

        val request = mockServer.takeRequest()
        val path =
                "/browse/groups?key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.30.0"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseGroupsResultsWithServerError() {
        val mockResponse = MockResponse().setResponseCode(500).setBody("Internal server error")
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseGroups().test()
        observer.assertComplete().assertValue {
            it.networkError
        }
        val request = mockServer.takeRequest()
    }

    @Test
    fun getBrowseGroupsResultsWithTimeout() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_groups.json"))
        mockResponse.throttleBody(128, 5, TimeUnit.SECONDS)
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseGroups().test()
        observer.assertComplete().assertValue {
            it.isError
        }
        val request = mockServer.takeRequest()
    }

    @Test
    fun getBrowseFacetsResultsWithEmptyResponse() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_groups_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseGroups().test()
        observer.assertComplete()
        observer.assertNoErrors()

        val browseResponse = observer.values()[0].get()
        assertTrue(browseResponse?.response?.groups!!.isEmpty())

        val request = mockServer.takeRequest()
    }

    @Test
    fun getBrowseGroupsResultsWithOptionalParameters() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_groups_empty.json"))
        mockServer.enqueue(mockResponse)
        val observer = constructorIo.getBrowseGroups("Brand", 1).test()
        observer.assertComplete()
        observer.assertNoErrors()

        val request = mockServer.takeRequest()
        val path =
               "/browse/groups?filters%5Bgroup_id%5D=Brand&fmt_options%5Bgroups_max_depth%5D=1&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.30.0"
        assert(request.path!!.startsWith(path))
    }

    @Test
    fun getBrowseFacetsResultsUsingBuilder() {
        val mockResponse = MockResponse().setResponseCode(200)
                .setBody(TestDataLoader.loadAsString("browse_groups.json"))
        mockServer.enqueue(mockResponse)
        val browseGroupsRequest = BrowseGroupsRequest.Builder()
                .setGroupId("Brand")
                .setGroupsMaxDepth(1)
                .build()
        val observer = constructorIo.getBrowseGroups(browseGroupsRequest).test()
        val request = mockServer.takeRequest()
        val path =
                "/browse/groups?filters%5Bgroup_id%5D=Brand&fmt_options%5Bgroups_max_depth%5D=1&key=silver-key&i=guapo-the-guid&ui=player-two&s=92&c=cioand-2.30.0"
        assert(request.path!!.startsWith(path))
    }
}
