package io.constructor.data

import io.constructor.data.remote.ConstructorApi
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.TestDataLoader
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class DataManagerTest {

    @Rule
    @JvmField val overrideSchedulersRule = RxSchedulersOverrideRule()

    private var constructorApi = mockk<ConstructorApi>()

    private var dataManager = DataManager(constructorApi)

    @Test
    fun getSuggestions() {
        every { constructorApi.getSuggestions("titanic") } returns Single.just(Response.success(TestDataLoader.loadResponse()))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isNotEmpty() && it.size == 5
        }
    }

    @Test
    fun getSuggestionsResponseContainsUnexpectedData() {
        every { constructorApi.getSuggestions("titanic") } returns Single.just(Response.success(TestDataLoader.loadResponseWithUnknownData()))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isNotEmpty() && it.size == 5
        }
    }

    @Test
    fun getEmptySuggestions() {
        every { constructorApi.getSuggestions("titanic") } returns Single.just(Response.success(TestDataLoader.loadEmptyResponse()))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isEmpty()
        }
    }

    @Test
    fun trackSelect() {
        every { constructorApi.trackSelect(any(), any(),any()) } returns Observable.just(Response.success(""))
        dataManager.trackSelect("titanic")
        verify(exactly = 1) { constructorApi.trackSelect(any(), any(), any())}
    }

    @Test
    fun trackSearch() {
        every { constructorApi.trackSearch(any(), any(), any()) } returns Observable.just(Response.success(""))
        dataManager.trackSearch("titanic")
        verify(exactly = 1) { constructorApi.trackSearch(any(), any(), any())}
    }

    @Test
    fun trackSessionStart() {
        every { constructorApi.trackSessionStart(any()) } returns Observable.just(Response.success(""))
        dataManager.trackSessionStart(arrayOf())
        verify(exactly = 1) { constructorApi.trackSessionStart(any())}
    }

    @Test
    fun trackConversion() {
        every { constructorApi.trackConversion(any(), any(), any(), any()) } returns Observable.just(Response.success(""))
        dataManager.trackConversion("testTerm", "1")
        verify(exactly = 1) { constructorApi.trackConversion(any(), any(), any(), any())}
    }

    @Test
    fun trackSearchResultClickThrough() {
        every { constructorApi.trackSearchResultClickThrough(any(), any(), any(), any()) } returns Observable.just(Response.success(""))
        dataManager.trackSearchResultClickThrough("term", "1")
        verify(exactly = 1) { constructorApi.trackSearchResultClickThrough(any(), any(), any(), any())}
    }

    @Test
    fun trackSearchResultLoaded() {
        every { constructorApi.trackSearchResultLoaded(any(), any(), any()) } returns Observable.just(Response.success(""))
        dataManager.trackSearchResultLoaded("term", 10, arrayOf())
        verify(exactly = 1) { constructorApi.trackSearchResultLoaded(any(), any(), any())}
    }

    @Test
    fun trackInputFocus() {
        every { constructorApi.trackInputFocus(any(), any()) } returns Observable.just(Response.success(""))
        dataManager.trackInputFocus("term", arrayOf())
        verify(exactly = 1) { constructorApi.trackInputFocus(any(), any()) }
    }

}