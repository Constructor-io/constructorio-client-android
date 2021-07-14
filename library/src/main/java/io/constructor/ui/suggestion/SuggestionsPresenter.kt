package io.constructor.ui.suggestion

import io.constructor.core.ConstructorIo
import io.constructor.data.local.PreferencesHelper
import io.constructor.features.base.BasePresenter
import io.constructor.injection.ConfigPersistent
import io.constructor.util.d
import io.constructor.util.io2ui
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * @suppress
 */
@ConfigPersistent
class SuggestionsPresenter @Inject
constructor(private val preferencesHelper: PreferencesHelper) : BasePresenter<SuggestionsView>() {

    protected var disposables: CompositeDisposable = CompositeDisposable()

    override fun attachView(mvpView: SuggestionsView) {
        super.attachView(mvpView)
        disposables.add(mvpView.queryChanged().debounce(300, TimeUnit.MILLISECONDS).io2ui().subscribe({ query ->
            getSuggestions(query)
        }, {error ->
            error.printStackTrace()
        }))
        disposables.add(mvpView.inputFocusChanged().subscribeOn(Schedulers.io()).subscribe({
            if (it.second) {
                ConstructorIo.trackInputFocus(it.first)
                d("Fired focus event for term: ${it.first}")
            }
        }, {
            d("Error firing input focus event")
        }))
    }

    override fun detachView() {
        disposables.clear()
    }

    fun getSuggestions(text: String) {
        mvpView.loading()
        if (preferencesHelper.apiKey.isEmpty()) {
            mvpView.onError(IllegalStateException("api key is null, please init library with api key using ConstructorIo.init"))
            return
        }
        disposables.add(ConstructorIo.getAutocompleteResults(text).io2ui().subscribe { data ->
            data.onValue {
                it.let {
                    mvpView.showSuggestions(it, preferencesHelper.groupsShownForFirstTerm)
                }
            }
            data.onError {
                    if (it is NoSuchElementException) {
                        d("throwing invalid api key error")
                        mvpView.onError(IllegalStateException("invalid api key"))

                    } else {
                        mvpView.onError(it)
                    }
            }
        })
    }

}
