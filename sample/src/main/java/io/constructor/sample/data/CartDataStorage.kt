package io.constructor.sample.data

import android.content.Context
import io.constructor.data.model.search.SearchResponseInner
import java.io.*

class CartDataStorage(private val context: Context) {

    private var cartFile: File = File(context.filesDir, "cart")

    init {
        if (!cartFile.exists()) {
            cartFile.createNewFile()
        }
    }

    fun addToCart(item: SearchResponseInner, quantity: Int = 1) {
        readAndWrite {
            if (it.containsKey(item.result.id)) {
                it[item.result.id] = it[item.result.id]!!.first to (it[item.result.id]!!.second + quantity)
            } else {
                it[item.result.id] = item to quantity
            }
        }
    }

    fun removeFromCart(item: SearchResponseInner) {
        readAndWrite {
            if (it.containsKey(item.result.id)) {
                if (it.get(item.result.id)!!.second == 1) {
                    it.remove(item.result.id)
                } else {
                    it[item.result.id] = it[item.result.id]!!.first to (it[item.result.id]!!.second - 1)
                }
            }
        }
    }

    fun removeAll() {
        readAndWrite {
            it.clear()
        }
    }

    fun getCartContent(): LinkedHashMap<String, Pair<SearchResponseInner, Int>> {
        var searchResult: LinkedHashMap<String, Pair<SearchResponseInner, Int>> = linkedMapOf()
        readAndWrite {
            searchResult = it
        }
        return searchResult
    }

    fun readAndWrite(action: (LinkedHashMap<String, Pair<SearchResponseInner, Int>>) -> Unit) {
        var cartItems: LinkedHashMap<String, Pair<SearchResponseInner, Int>> = linkedMapOf()
        try {
            val input = ObjectInputStream(FileInputStream(cartFile))
            cartItems = input.readObject() as LinkedHashMap<String, Pair<SearchResponseInner, Int>>
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