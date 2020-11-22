package io.constructor.sample.feature.checkout

import android.os.Bundle
import io.constructor.data.model.common.Result
import io.constructor.sample.R
import io.constructor.sample.common.BaseActivity
import io.constructor.sample.di.DependencyProvider
import kotlinx.android.synthetic.main.activity_home.*

class CheckoutActivity : BaseActivity<CheckoutPresenter>(), CheckoutView {

    override fun initPresenter(): CheckoutPresenter {
        return CheckoutPresenter(this, DependencyProvider.provideCartStorage(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        toolbar.title = getString(R.string.checkout)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        presenter.checkout()
    }

    override fun renderContent(content: LinkedHashMap<String, Pair<Result, Int>>) {
    }

}
