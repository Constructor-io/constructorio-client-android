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
        every { constructorApi.getSuggestions("titanic", any()) } returns Single.just(Result.response(Response.success(TestDataLoader.loadResponse())))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.isNotEmpty() && it.get()!!.size == 5
        }
    }

    @Test
    fun getSuggestionsBadServerResponse() {
        every { constructorApi.getSuggestions("titanic", any()) } returns Single.just(Result.response(Response.error(500, ResponseBody.create(MediaType.parse("text/plain"), "Error"))))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.networkError
        }
    }

    @Test
    fun getSuggestionsException() {
        every { constructorApi.getSuggestions("titanic", any()) } returns Single.just(Result.error<AutocompleteResult>(Exception()))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isError
        }
    }

    @Test
    fun getSuggestionsUnexpectedDataResponse() {
        every { constructorApi.getSuggestions("titanic", any()) } returns Single.just(Result.response(Response.success(TestDataLoader.loadResponseWithUnexpectedData())))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.get()!!.isNotEmpty() && it.get()!!.size == 5
        }
    }

    @Test
    fun getSuggestionsEmptyResponse() {
        every { constructorApi.getSuggestions("titanic", any()
        ) } returns Single.just(Result.response(Response.success(TestDataLoader.loadEmptyResponse())))
        val observer = dataManager.getAutocompleteResults("titanic").test()
        observer.assertComplete().assertValue {
            it.isEmpty
        }
    }

    @Test
    fun trackAutocompleteSelect() {
        every { constructorApi.trackAutocompleteSelect(any(), any(),any()) } returns Completable.complete()
        dataManager.trackAutocompleteSelect("titanic")
        verify(exactly = 1) { constructorApi.trackAutocompleteSelect(any(), any(), any())}
    }

    @Test
    fun trackAutocompleteSelectError() {
        every { constructorApi.trackAutocompleteSelect(any(), any(),any()) } returns Completable.error(Exception())
        val observer = dataManager.trackAutocompleteSelect("titanic").test()
        observer.assertError {
            true
        }
        verify(exactly = 1) { constructorApi.trackAutocompleteSelect(any(), any(), any())}
    }

    @Test
    fun trackSearchSubmit() {
        every { constructorApi.trackSearchSubmit(any(), any(), any()) } returns Completable.complete()
        dataManager.trackSearchSubmit("titanic")
        verify(exactly = 1) { constructorApi.trackSearchSubmit(any(), any(), any())}
    }

    @Test
    fun trackSearchSubmitError() {
        every { constructorApi.trackSearchSubmit(any(), any(), any()) } returns Completable.error(Exception())
        val observer = dataManager.trackSearchSubmit("titanic").test()
        observer.assertError {
            true
        }
        verify(exactly = 1) { constructorApi.trackSearchSubmit(any(), any(), any())}
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
        every { constructorApi.trackConversion(any(), any(), any(), any(), any()) } returns Completable.complete()
        dataManager.trackConversion("testTerm", "item1", "id1", "11.99")
        verify(exactly = 1) { constructorApi.trackConversion(any(), any(), any(), any(), any())}
    }

    @Test
    fun trackConversionError() {
        every { constructorApi.trackConversion(any(), any(), any(), any(), any()) } returns Completable.error(Exception())
        val observer = dataManager.trackConversion("testTerm", "item1", "id1").test()
        observer.assertError {
            true
        }
        verify(exactly = 1) { constructorApi.trackConversion(any(), any(), any(), any(), any())}
    }

    @Test
    fun trackSearchResultClick() {
        every { constructorApi.trackSearchResultTerm(any(), any(), any(), any()) } returns Completable.complete()
        dataManager.trackSearchResultClick("term", "id1", "term1")
        verify(exactly = 1) { constructorApi.trackSearchResultTerm(any(), any(), any(), any())}
    }

    @Test
    fun trackSearchResultClickError() {
        every { constructorApi.trackSearchResultTerm(any(), any(), any(), any()) } returns Completable.error(Exception())
        val observer = dataManager.trackSearchResultClick("term", "1", "term1").test()
        observer.assertError { true }
        verify(exactly = 1) { constructorApi.trackSearchResultTerm(any(), any(), any(), any())}
    }

    @Test
    fun trackSearchResultsLoaded() {
        every { constructorApi.trackSearchResultsLoaded(any(), any(), any()) } returns Completable.complete()
        dataManager.trackSearchResultsLoaded("term", 10, arrayOf())
        verify(exactly = 1) { constructorApi.trackSearchResultsLoaded(any(), any(), any())}
    }

    @Test
    fun trackSearchResultsLoadedError() {
        every { constructorApi.trackSearchResultsLoaded(any(), any(), any()) } returns Completable.error(Exception())
        val observer = dataManager.trackSearchResultsLoaded("term", 10, arrayOf()).test()
        observer.assertError { true }
        verify(exactly = 1) { constructorApi.trackSearchResultsLoaded(any(), any(), any())}
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

    @Test
    fun trackPurchase() {
        every { constructorApi.trackPurchase(any()) } returns Completable.complete()
        dataManager.trackPurchase(arrayOf())
        verify(exactly = 1) { constructorApi.trackPurchase(any()) }
    }

}