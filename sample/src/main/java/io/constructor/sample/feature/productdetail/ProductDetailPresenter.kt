package io.constructor.sample.feature.productdetail

import io.constructor.core.ConstructorIo
import io.constructor.data.model.search.SearchResponseInner
import io.constructor.sample.common.BasePresenter
import io.constructor.sample.data.CartDataStorage
import io.constructor.sample.extensions.price

class ProductDetailPresenter(view: ProductDetailView, private val cartDataStorage: CartDataStorage) : BasePresenter<ProductDetailView>(view) {

    fun addToCart(item: SearchResponseInner) {
        ConstructorIo.trackConversion(item.value, item.result.id, item.result.price())
        cartDataStorage.addToCart(item)
    }

}