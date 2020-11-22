package io.constructor.sample.extensions

import io.constructor.data.model.common.ResultData

fun ResultData.price(): Double {
    val price = metadata?.get("price")
    if (price is String) {
        return price.trim('$').toDouble()
    } else if (price is Double) {
        return price
    }
    return 0.00
}

fun ResultData.priceFormatted(): String {
    val price = metadata?.get("price")
    if (price is String) {
        return price
    } else if (price is Double) {
        return price.toString()
    }
    return "0.00"
}