package io.constructor.sample.feature.checkout

import io.constructor.core.ConstructorIo
import io.constructor.sample.common.BasePresenter
import io.constructor.sample.data.CartDataStorage
import io.constructor.sample.extensions.price
import kotlin.random.Random

class CheckoutPresenter(view: CheckoutView, private val cartStorage: CartDataStorage) : BasePresenter<CheckoutView>(view) {

    fun checkout() {
        val total = cartStorage.getCartContent().map { it.value }.sumByDouble {
            (it.first.data.price() * it.second)
        }
        ConstructorIo.trackPurchase(arrayOf("item-001", "item-002", "item-003"), total, "ORD" + Random.nextInt(0, 10000))
    }

}