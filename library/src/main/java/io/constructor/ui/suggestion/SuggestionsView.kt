package io.constructor.ui.suggestion

import io.constructor.data.model.Suggestion
import io.constructor.features.base.MvpView
import io.reactivex.Observable


interface SuggestionsView : MvpView {

    fun showSuggestions(suggestionsResult: List<Suggestion>)
    fun queryChanged() : Observable<String>
    fun loading()
    fun onError(error: Throwable)
}