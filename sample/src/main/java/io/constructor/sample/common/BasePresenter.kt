package io.constructor.sample.common

import io.reactivex.disposables.CompositeDisposable

/**
 * @suppress
 */
open class BasePresenter<V : BaseView>(protected var view: V) {

    protected val compositeDisposable = CompositeDisposable()

    fun onDestroy() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }
}