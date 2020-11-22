package io.constructor.sample.data

import android.content.Context
import io.constructor.data.model.common.Result
import java.io.*

class CartDataStorage(private val context: Context) {

    private var cartFile: File = File(context.filesDir, "cart")

    init {
        if (!cartFile.exists()) {
            cartFile.createNewFile()
        }
    }

    fun addToCart(item: Result, quantity: Int = 1) {
        readAndWrite {
            val key = item.data.id!!;
            if (it.containsKey(key)) {
                it[key] = it[key]!!.first to (it[key]!!.second + quantity)
            } else {
                it[key] = item to quantity
            }
        }
    }

    fun removeFromCart(item: Result) {
        readAndWrite {
            val key = item.data.id!!;
            if (it.containsKey(key)) {
                if (it.get(key)!!.second == 1) {
                    it.remove(key)
                } else {
                    it[key] = it[key]!!.first to (it[key]!!.second - 1)
                }
            }
        }
    }

    fun removeAll() {
        readAndWrite {
            it.clear()
        }
    }

    fun getCartContent(): LinkedHashMap<String, Pair<Result, Int>> {
        var searchResult: LinkedHashMap<String, Pair<Result, Int>> = linkedMapOf()
        readAndWrite {
            searchResult = it
        }
        return searchResult
    }

    fun readAndWrite(action: (LinkedHashMap<String, Pair<Result, Int>>) -> Unit) {
        var cartItems: LinkedHashMap<String, Pair<Result, Int>> = linkedMapOf()
        try {
            val input = ObjectInputStream(FileInputStream(cartFile))
            cartItems = input.readObject() as LinkedHashMap<String, Pair<Result, Int>>
            input.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        action.invoke(cartItems)
        val output = ObjectOutputStream(FileOutputStream(cartFile))
        output.writeObject(cartItems)
        output.close()
    }
}