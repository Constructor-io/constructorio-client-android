package io.constructor.data

import io.constructor.data.model.AutocompleteResult
import io.constructor.data.remote.ConstructorApi
import io.constructor.util.RxSchedulersOverrideRule
import io.constructor.util.TestDataLoader
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result

class DataManagerTest {

    @Rule
    @JvmField val overrideSchedulersRule = RxSchedulersOverrideRule()

    private var constructorApi = mockk<ConstructorApi>()

    private var dataManager = DataManager(constructorApi)

    @Test
    fun getSuggestions() {
        every { constructorApi.getSuggestions("titanic") } returns Single.just(Result.response(Response.success(TestDataLoader.loadResponse())))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.isNotEmpty() && it.get()!!.size == 5
        }
    }

    @Test
    fun getSuggestionsBadServerResponse() {
        every { constructorApi.getSuggestions("titanic") } returns Single.just(Result.response(Response.error(500, ResponseBody.create(MediaType.parse("text/plain"), "Error"))))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.networkError
        }
    }

    @Test
    fun getSuggestionsException() {
        every { constructorApi.getSuggestions("titanic") } returns Single.just(Result.error<AutocompleteResult>(Exception()))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isError
        }
    }

    @Test
    fun getSuggestionsResponseContainsUnexpectedData() {
        every { constructorApi.getSuggestions("titanic") } returns Single.just(Result.response(Response.success(TestDataLoader.loadResponseWithUnknownData())))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.isNotEmpty() && it.get()!!.size == 5
        }
    }

    @Test
    fun getEmptySuggestions() {
        every { constructorApi.getSuggestions("titanic") } returns Single.just(Result.response(Response.success(TestDataLoader.loadEmptyResponse())))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isEmpty
        }
    }

    @Test
    fun trackSelect() {
        every { constructorApi.trackSelect(any(), any(),any()) } returns Completable.complete()
        dataManager.trackSelect("titanic")
        verify(exactly = 1) { constructorApi.trackSelect(any(), any(), any())}
    }

    @Test
    fun trackSelectError() {
        every { constructorApi.trackSelect(any(), any(),any()) } returns Completable.error(Exception())
        val observer = dataManager.trackSelect("titanic").test()
        observer.assertError {
            true
        }
        verify(exactly = 1) { constructorApi.trackSelect(any(), any(), any())}
    }

    @Test
    fun trackSearch() {
        every { constructorApi.trackSearch(any(), any(), any()) } returns Completable.complete()
        dataManager.trackSearch("titanic")
        verify(exactly = 1) { constructorApi.trackSearch(any(), any(), any())}
    }

    @Test
    fun trackError() {
        every { constructorApi.trackSearch(any(), any(), any()) } returns Completable.error(Exception())
        val observer = dataManager.trackSearch("titanic").test()
        observer.assertError {
            true
        }
        verify(exactly = 1) { constructorApi.trackSearch(any(), any(), any())}
    }

    @Test
    fun trackSessionStart() {
        every { constructorApi.trackSessionStart(any()) } returns Completable.complete()
        dataManager.trackSessionStart(arrayOf())
        verify(exactly = 1) { constructorApi.trackSessionStart(any())}
    }

    @Test
    fun trackSessionStartError() {
        every { constructorApi.trackSessionStart(any()) } returns Completable.error(Exception())
        val observer = dataManager.trackSessionStart(arrayOf()).test()
        observer.assertError {
            true
        }
        verify(exactly = 1) { constructorApi.trackSessionStart(any())}
    }

    @Test
    fun trackConversion() {
        every { constructorApi.trackConversion(any(), any(), any(), any()) } returns Completable.complete()
        dataManager.trackConversion("testTerm", "1")
        verify(exactly = 1) { constructorApi.trackConversion(any(), any(), any(), any())}
    }

    @Test
    fun trackConversionError() {
        every { constructorApi.trackConversion(any(), any(), any(), any()) } returns Completable.error(Exception())
        val observer = dataManager.trackConversion("testTerm", "1").test()
        observer.assertError {
            true
        }
        verify(exactly = 1) { constructorApi.trackConversion(any(), any(), any(), any())}
    }

    @Test
    fun trackSearchResultClickThrough() {
        every { constructorApi.trackSearchResultClickThrough(any(), any(), any(), any()) } returns Completable.complete()
        dataManager.trackSearchResultClickThrough("term", "1")
        verify(exactly = 1) { constructorApi.trackSearchResultClickThrough(any(), any(), any(), any())}
    }

    @Test
    fun trackSearchResultClickThroughError() {
        every { constructorApi.trackSearchResultClickThrough(any(), any(), any(), any()) } returns Completable.error(Exception())
        val observer = dataManager.trackSearchResultClickThrough("term", "1").test()
        observer.assertError { true }
        verify(exactly = 1) { constructorApi.trackSearchResultClickThrough(any(), any(), any(), any())}
    }

    @Test
    fun trackSearchResultLoaded() {
        every { constructorApi.trackSearchResultLoaded(any(), any(), any()) } returns Completable.complete()
        dataManager.trackSearchResultLoaded("term", 10, arrayOf())
        verify(exactly = 1) { constructorApi.trackSearchResultLoaded(any(), any(), any())}
    }

    @Test
    fun trackSearchResultLoadedError() {
        every { constructorApi.trackSearchResultLoaded(any(), any(), any()) } returns Completable.error(Exception())
        val observer = dataManager.trackSearchResultLoaded("term", 10, arrayOf()).test()
        observer.assertError { true }
        verify(exactly = 1) { constructorApi.trackSearchResultLoaded(any(), any(), any())}
    }

    @Test
    fun trackInputFocus() {
        every { constructorApi.trackInputFocus(any(), any()) } returns Completable.complete()
        dataManager.trackInputFocus("term", arrayOf())
        verify(exactly = 1) { constructorApi.trackInputFocus(any(), any()) }
    }

    @Test
    fun trackInputFocusError() {
        every { constructorApi.trackInputFocus(any(), any()) } returns Completable.error(Exception())
        val observer = dataManager.trackInputFocus("term", arrayOf()).test()
        observer.assertError { true }
        verify(exactly = 1) { constructorApi.trackInputFocus(any(), any()) }
    }

}