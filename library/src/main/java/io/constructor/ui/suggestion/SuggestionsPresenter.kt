package io.constructor.ui.suggestion

import io.constructor.core.ConstructorIo
import io.constructor.data.local.PreferencesHelper
import io.constructor.data.model.Suggestion
import io.constructor.features.base.BasePresenter
import io.constructor.injection.ConfigPersistent
import io.constructor.util.d
import io.constructor.util.rx.scheduler.SchedulerUtils
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ConfigPersistent
class SuggestionsPresenter @Inject
constructor(private val preferencesHelper: PreferencesHelper) : BasePresenter<SuggestionsView>() {

    protected var disposables: CompositeDisposable = CompositeDisposable()

    override fun attachView(mvpView: SuggestionsView) {
        super.attachView(mvpView)
        disposables.add(mvpView.queryChanged().debounce(300, TimeUnit.MILLISECONDS).compose(SchedulerUtils.ioToMain<String>()).subscribe({ query ->
            getSuggestions(query)
        }, {error ->
            error.printStackTrace()
        }))
    }

    override fun detachView() {
        disposables.clear()
    }

    fun getSuggestions(text: String) {
        mvpView.loading()
        if (preferencesHelper.getToken().isEmpty()) {
            mvpView.onError(IllegalStateException("token is null, please init library with token using ConstructorIo.init"))
            return
        }
        disposables.add(ConstructorIo.getAutocompleteResults(text).compose(SchedulerUtils.ioToMain<List<Suggestion>>()).subscribe({ suggestions ->
            mvpView.showSuggestions(suggestions)
        }, { error ->
            run {
                if (error is NoSuchElementException) {
                    d("throwing invalid token error")
                    mvpView.onError(IllegalStateException("invalid token"))

                } else {
                    mvpView.onError(error)
                }

            }
        }))
    }

}