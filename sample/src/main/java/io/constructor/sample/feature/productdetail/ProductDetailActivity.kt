package io.constructor.sample.feature.productdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.bumptech.glide.Glide
import io.constructor.data.model.search.SearchResponseInner
import io.constructor.sample.R
import io.constructor.sample.common.BaseActivity
import io.constructor.sample.di.DependencyProvider
import io.constructor.sample.extensions.priceFormatted
import kotlinx.android.synthetic.main.activity_product_detail.*

class ProductDetailActivity : BaseActivity<ProductDetailPresenter>(), ProductDetailView {

    private val item by lazy {
        intent.getSerializableExtra(EXTRA_ITEM) as SearchResponseInner
    }

    override fun initPresenter(): ProductDetailPresenter {
        return ProductDetailPresenter(this, DependencyProvider.provideCartStorage(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        toolbar.inflateMenu(R.menu.product_detail)
        toolbar.title = item.value
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.addToCart -> {
                    presenter.addToCart(item)
                    Snackbar.make(rootContent, getString(R.string.item_added_to_cart), Snackbar.LENGTH_SHORT).show()
                    true
                }
                else -> {
                    false
                }
            }
        }
        Glide.with(image).load(item.result.imageUrl).into(image)
        price.text = item.result.priceFormatted()
        descriptionTV.text = item.result.description
    }

    companion object {

        const val EXTRA_ITEM = "item"

        fun start(context: Context, item: SearchResponseInner) {
            context.startActivity(Intent(context, ProductDetailActivity::class.java).apply {
                putExtra(EXTRA_ITEM, item)
            })
        }
    }

}
