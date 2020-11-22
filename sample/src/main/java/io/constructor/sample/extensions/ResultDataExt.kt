package io.constructor.sample.extensions

import io.constructor.data.model.common.ResultData

fun ResultData.price(): Double? {
    val price = metadata["price"]
    if (price is String) {
        return (metadata["price"] as String).trim('$').toDouble()
    } else if (price is Double) {
        return price
    }
    return null
}

fun ResultData.priceFormatted(): String? {
    val price = metadata["price"]
    if (price is String) {
        return price
    } else if (price is Double) {
        return price.toString()
    }
    return null
}