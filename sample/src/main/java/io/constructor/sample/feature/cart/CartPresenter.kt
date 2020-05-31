package io.constructor.sample.feature.cart

import io.constructor.data.model.search.SearchResult
import io.constructor.sample.common.BasePresenter
import io.constructor.sample.data.CartDataStorage
import io.constructor.sample.feature.checkout.CheckoutView

class CartPresenter(view: CheckoutView, private val cartStorage: CartDataStorage) : BasePresenter<CheckoutView>(view) {

    fun loadCartContents() {
        val content = cartStorage.getCartContent()
        view.renderContent(content)
    }

    fun removeFromCart(item: SearchResult) {
        cartStorage.removeFromCart(item)
        loadCartContents()
    }

    fun addToCart(it: SearchResult) {
        cartStorage.addToCart(it)
        loadCartContents()
    }

    fun removeCartContents() {
        cartStorage.removeAll()
        loadCartContents()
    }

}