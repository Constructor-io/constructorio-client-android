package io.constructor.sample.feature.cart

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import io.constructor.data.model.common.Result
import io.constructor.sample.R
import io.constructor.sample.common.BaseActivity
import io.constructor.sample.di.DependencyProvider
import io.constructor.sample.feature.checkout.CheckoutActivity
import io.constructor.sample.feature.checkout.CheckoutView
import io.constructor.sample.feature.productdetail.ProductDetailActivity
//import kotlinx.android.synthetic.main.activity_cart.*
//import kotlinx.android.synthetic.main.activity_home.toolbar

class CartActivity : BaseActivity<CartPresenter>(), CheckoutView {

    private lateinit var adapter: CartContentAdapter

    override fun initPresenter(): CartPresenter {
        return CartPresenter(this, DependencyProvider.provideCartStorage(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
//        toolbar.title = getString(R.string.cart_content)
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
//        toolbar.setNavigationOnClickListener { onBackPressed() }
//        toolbar.inflateMenu(R.menu.cart)
//        toolbar.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.removeAll -> {
//                    presenter.removeCartContents()
//                    true
//                }
//                else -> false
//
//            }
//        }
        presenter.loadCartContents()
//        checkout.setOnClickListener {
//            startActivity(Intent(this, CheckoutActivity::class.java))
//        }
    }

    override fun renderContent(content: LinkedHashMap<String, Pair<Result, Int>>) {
//        cartContent.layoutManager = LinearLayoutManager(this)
        adapter = CartContentAdapter({
//            ProductDetailActivity.start(this, it.first)
        }, {
            presenter.removeFromCart(it)
        })  {
            presenter.addToCart(it)
        }
        adapter.setData(content)
//        cartContent.adapter = adapter
    }

}
