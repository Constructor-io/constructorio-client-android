package io.constructor.sample.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity<T : BasePresenter<*>> : AppCompatActivity() {

    protected lateinit var presenter: T

    abstract fun initPresenter(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = initPresenter()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
