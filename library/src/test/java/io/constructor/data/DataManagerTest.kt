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
    fun triggerSelectEvent() {
        every { constructorApi.triggerSelectEvent(any(), any(),any()) } returns Observable.just(Response.success(""))
        dataManager.triggerSelectEvent("titanic")
        verify(exactly = 1) { constructorApi.triggerSelectEvent(any(), any(), any())}
    }

    @Test
    fun triggerSearchEvent() {
        every { constructorApi.triggerSearchEvent(any(), any(), any()) } returns Observable.just(Response.success(""))
        dataManager.triggerSearchEvent("titanic")
        verify(exactly = 1) { constructorApi.triggerSearchEvent(any(), any(), any())}
    }

    @Test
    fun triggerSessionStartEvent() {
        every { constructorApi.triggerSessionStartEvent(any()) } returns Observable.just(Response.success(""))
        dataManager.triggerSessionStartEvent(arrayOf())
        verify(exactly = 1) { constructorApi.triggerSessionStartEvent(any())}
    }

    @Test
    fun triggerConversionEvent() {
        every { constructorApi.triggerConvertEvent(any(), any(), any()) } returns Observable.just(Response.success(""))
        dataManager.triggerConversionEvent("1")
        verify(exactly = 1) { constructorApi.triggerConvertEvent(any(), any(), any())}
    }

}