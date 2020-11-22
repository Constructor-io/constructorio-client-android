package io.constructor.ui.suggestion

import io.constructor.data.model.autocomplete.AutocompleteResponse
import io.constructor.features.base.MvpView
import io.reactivex.Observable


interface SuggestionsView : MvpView {

    fun showSuggestions(response: AutocompleteResponse, groupsShownForFirstTerm: Int = Int.MAX_VALUE)
    fun queryChanged() : Observable<String>
    fun inputFocusChanged() : Observable<Pair<String?, Boolean>>
    fun loading()
    fun onError(error: Throwable)
}