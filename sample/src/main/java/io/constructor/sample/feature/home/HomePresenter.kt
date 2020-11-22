package io.constructor.sample.feature.home

import io.constructor.core.ConstructorIo
import io.constructor.sample.common.BasePresenter
import io.constructor.sample.extensions.io2ui
import io.constructor.sample.extensions.plusAssign
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class HomePresenter(view: HomeView) : BasePresenter<HomeView>(view) {

    private val queryString = PublishSubject.create<String>()

    fun onCreate() {
        compositeDisposable += queryString.debounce(300, TimeUnit.MILLISECONDS).flatMap {
            ConstructorIo.getAutocompleteResults(it)
        }.io2ui().subscribe {
            it.onValue {
                it?.let {
                    view.renderAutocompleteData(it)
                }
            }
        }
    }

    fun onQuery(s: String) {
        queryString.onNext(s)
    }
}