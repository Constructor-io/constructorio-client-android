package io.constructor.core

import android.content.Context
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.memory.ConfigMemoryHolder
import io.constructor.test.createTestDataManager
import io.constructor.util.RxSchedulersOverrideRule
import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConstructorIoIntegrationMediaTrackingTest {

    @Rule
    @JvmField
    val overrideSchedulersRule = RxSchedulersOverrideRule()

    private var constructorIo = ConstructorIo
    private val ctx = mockk<Context>()
    private val preferencesHelper = mockk<PreferencesHelper>()
    private val configMemoryHolder = mockk<ConfigMemoryHolder>()
    private val timeBetweenTests = 2000L

    private val testKey = "key_x6UnCVRZaJgIHFQD"
    private val testPlacementId = "home"
    private lateinit var bannerAdId: String

    @Before
    fun setup() {
        every { ctx.applicationContext } returns ctx

        every { preferencesHelper.apiKey } returns testKey
        every { preferencesHelper.id } returns "wacko-the-guid"
        every { preferencesHelper.scheme } returns "https"
        every { preferencesHelper.serviceUrl } returns "ac.cnstrc.com"
        every { preferencesHelper.mediaServiceUrl } returns "behavior.media-cnstrc.com"
        every { preferencesHelper.port } returns 443
        every { preferencesHelper.defaultItemSection } returns "Products"
        every { preferencesHelper.getSessionId(any(), any()) } returns 67

        every { configMemoryHolder.autocompleteResultCount } returns null
        every { configMemoryHolder.userId } returns "player-three"
        every { configMemoryHolder.defaultAnalyticsTags } returns mapOf("appVersion" to "123", "appPlatform" to "Android")
        every { configMemoryHolder.testCellParams } returns emptyList()
        every { configMemoryHolder.segments } returns emptyList()

        val config = ConstructorIoConfig(testKey)
        val dataManager = createTestDataManager(preferencesHelper, configMemoryHolder)

        constructorIo.testInit(ctx, config, dataManager, preferencesHelper, configMemoryHolder)

        bannerAdId = fetchBannerAdId()
    }

    private fun fetchBannerAdId(): String {
        val client = OkHttpClient()
        val url = "https://display.media-cnstrc.com/display-ads?key=$testKey&placement_ids=$testPlacementId"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            val json = JSONObject(body ?: "{}")
            val displayAds = json.getJSONObject("display_ads")
            val placementArray = displayAds.optJSONArray(testPlacementId)
            if (placementArray != null && placementArray.length() > 0) {
                return placementArray.getJSONObject(0).getString("banner_ad_id")
            }
            val placementObject = displayAds.optJSONObject(testPlacementId)
            if (placementObject != null) {
                return placementObject.getString("banner_ad_id")
            }
        }
        throw IllegalStateException("Unable to fetch banner ad id for placement $testPlacementId")
    }

    @Test
    fun trackMediaImpressionViewAgainstRealResponse() {
        val observer = ConstructorIo.trackMediaImpressionViewInternal(bannerAdId, testPlacementId).test()
        observer.assertComplete()
        observer.assertNoErrors()
        Thread.sleep(timeBetweenTests)
    }

    @Test
    fun trackMediaImpressionClickAgainstRealResponse() {
        val observer = ConstructorIo.trackMediaImpressionClickInternal(bannerAdId, testPlacementId).test()
        observer.assertComplete()
        observer.assertNoErrors()
        Thread.sleep(timeBetweenTests)
    }
}
